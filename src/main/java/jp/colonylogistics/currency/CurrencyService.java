package jp.colonylogistics.currency;

import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.contract.RewardSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Resolves and pays the configured item-based currency.
 *
 * <p>Phase 17.9 centralizes all generated-freight and player-trade payouts here.
 * The integration remains item-id based only: Colony Logistics does not import
 * external economy or wallet APIs directly.</p>
 *
 * <p>Phase 17.9.1 removes the emerald development fallback. Old configs using
 * {@code tradepost:coin} or {@code minecraft:emerald} are normalized to the
 * configured base coin.</p>
 *
 * <p>Issue #2 migrates the default currency to Lightman's Currency coins while
 * keeping legacy Trade Post ids readable for old saves/configs.</p>
 */
public final class CurrencyService {
    public ResourceLocation defaultRewardCurrencyItemId() {
        ResourceLocation configured = ColonyLogisticsConfig.currencyBaseCoinItemId();
        if (isRegisteredItem(configured)) {
            return configured;
        }
        ResourceLocation legacyConfigured = ColonyLogisticsConfig.legacyTradePostCurrencyItemId();
        if (isRegisteredItem(legacyConfigured)) {
            return legacyConfigured;
        }
        if (ColonyLogisticsConfig.useFallbackCurrencyWhenMissing()) {
            ResourceLocation fallback = ColonyLogisticsConfig.fallbackCurrencyItemId();
            if (isRegisteredItem(fallback)) {
                return fallback;
            }
        }
        // Keep the configured currency in generated contract data and GUI rows
        // even when the dependency is missing, so the failure is visible instead
        // of silently presenting vanilla emeralds as the default reward.
        return configured;
    }

    public RewardSpec defaultReward(int amount) {
        return new RewardSpec(defaultRewardCurrencyItemId(), Math.max(0, amount));
    }

    /**
     * Generated freight is always stored as base-coin value. This also migrates
     * old open contracts that still contain the Phase 17.9 emerald development
     * fallback or an alternate registered denomination item.
     */
    public RewardSpec normalizeGeneratedReward(RewardSpec reward) {
        if (reward == null) {
            return defaultReward(0);
        }
        ResourceLocation active = defaultRewardCurrencyItemId();
        int legacyValue = ColonyLogisticsConfig.legacyCurrencyBaseValue(reward.currencyItemId());
        if (legacyValue > 0) {
            return new RewardSpec(active, safeMultiply(reward.currencyAmount(), legacyValue));
        }
        Optional<CurrencyDenomination> denomination = denominationForItem(reward.currencyItemId());
        if (denomination.isPresent()) {
            return new RewardSpec(active, safeMultiply(reward.currencyAmount(), denomination.get().baseValue()));
        }
        if (!active.equals(reward.currencyItemId())) {
            return new RewardSpec(active, reward.currencyAmount());
        }
        return reward;
    }

    /**
     * Returns the actual payable RewardSpec, applying only configured currency
     * rules before any contract state is mutated. For configured denominations,
     * the returned amount is always base-coin value.
     */
    public Optional<RewardSpec> payableReward(RewardSpec reward) {
        return payableReward(reward, reward.currencyAmount());
    }

    public Optional<RewardSpec> payableReward(RewardSpec reward, int amount) {
        int clampedAmount = Math.max(0, amount);
        ResourceLocation preferred = reward.currencyItemId();
        int legacyValue = ColonyLogisticsConfig.legacyCurrencyBaseValue(preferred);
        if (legacyValue > 0) {
            preferred = defaultRewardCurrencyItemId();
            clampedAmount = safeMultiply(clampedAmount, legacyValue);
        }

        if (ColonyLogisticsConfig.currencyExchangeEnabled()) {
            Optional<CurrencyDenomination> denomination = denominationForItem(preferred);
            if (denomination.isPresent()) {
                int baseAmount = safeMultiply(clampedAmount, denomination.get().baseValue());
                return canPayBaseCoinAmount(baseAmount) ? Optional.of(new RewardSpec(defaultRewardCurrencyItemId(), baseAmount)) : Optional.empty();
            }
        }

        if (clampedAmount <= 0) {
            return Optional.of(new RewardSpec(preferred, 0));
        }

        if (isRegisteredItem(preferred)) {
            return Optional.of(new RewardSpec(preferred, clampedAmount));
        }

        if (!ColonyLogisticsConfig.useFallbackCurrencyWhenMissing()) {
            return Optional.empty();
        }

        ResourceLocation fallback = ColonyLogisticsConfig.fallbackCurrencyItemId();
        if (isRegisteredItem(fallback)) {
            return Optional.of(new RewardSpec(fallback, clampedAmount));
        }
        return Optional.empty();
    }

    public boolean canPay(RewardSpec reward) {
        return payableReward(reward).isPresent();
    }

    /** Backwards-compatible boolean API kept for older call sites. */
    public boolean pay(ServerPlayer player, RewardSpec reward) {
        return payToPlayer(player, reward).paid();
    }

    public PaymentResult payToPlayer(ServerPlayer player, RewardSpec reward) {
        Optional<RewardSpec> payable = payableReward(reward);
        if (payable.isEmpty()) {
            return PaymentResult.failed(reward.currencyItemId(), reward.currencyAmount());
        }
        return payResolvedToPlayer(player, payable.get());
    }

    public PaymentResult payResolvedToPlayer(ServerPlayer player, RewardSpec payableReward) {
        int amount = Math.max(0, payableReward.currencyAmount());
        if (amount <= 0) {
            return PaymentResult.paid(payableReward.currencyItemId(), 0, "0 x " + payableReward.currencyItemId());
        }
        if (ColonyLogisticsConfig.currencyExchangeEnabled() && payableReward.currencyItemId().equals(defaultRewardCurrencyItemId())) {
            List<DenominationPayment> payments = decomposeBaseCoinAmount(amount);
            if (payments.isEmpty()) {
                return PaymentResult.failed(payableReward.currencyItemId(), amount);
            }
            for (DenominationPayment payment : payments) {
                giveItemStacks(player, payment.denomination().itemId(), payment.count());
            }
            return PaymentResult.paid(payableReward.currencyItemId(), amount, formatBreakdown(payments));
        }

        CurrencyAdapter adapter = new ItemCurrencyAdapter(payableReward.currencyItemId());
        if (!adapter.isAvailable()) {
            return PaymentResult.failed(payableReward.currencyItemId(), amount);
        }
        adapter.payToPlayer(player, amount);
        return PaymentResult.paid(adapter.currencyItemId(), amount, amount + " x " + adapter.currencyItemId());
    }

    /** Creates one or more stacks for a base-coin reward amount, using denominations when available. */
    public List<ItemStack> createCurrencyStacks(RewardSpec reward) {
        Optional<RewardSpec> payable = payableReward(reward);
        if (payable.isEmpty()) {
            return List.of();
        }
        RewardSpec spec = payable.get();
        if (spec.currencyAmount() <= 0) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        if (ColonyLogisticsConfig.currencyExchangeEnabled() && spec.currencyItemId().equals(defaultRewardCurrencyItemId())) {
            for (DenominationPayment payment : decomposeBaseCoinAmount(spec.currencyAmount())) {
                addChunkedStacks(stacks, payment.denomination().itemId(), payment.count());
            }
            return stacks;
        }
        addChunkedStacks(stacks, spec.currencyItemId(), spec.currencyAmount());
        return stacks;
    }

    /** Backwards-compatible API. Returns the first stack from {@link #createCurrencyStacks(RewardSpec)}. */
    public ItemStack createCurrencyStack(RewardSpec reward) {
        List<ItemStack> stacks = createCurrencyStacks(reward);
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
    }

    /**
     * Converts an escrow stack into base-coin value when it is a configured
     * currency denomination. Components/custom names are intentionally ignored
     * because currency rewards are amount + item-id based.
     */
    public Optional<RewardSpec> currencyRewardFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Optional<CurrencyDenomination> denomination = denominationForItem(itemId);
        if (denomination.isPresent()) {
            return Optional.of(new RewardSpec(defaultRewardCurrencyItemId(), safeMultiply(stack.getCount(), denomination.get().baseValue())));
        }
        return isAcceptedCurrencyItemId(itemId) ? Optional.of(new RewardSpec(itemId, stack.getCount())) : Optional.empty();
    }

    public boolean isAcceptedCurrencyStack(ItemStack stack) {
        return currencyRewardFromStack(stack).isPresent();
    }

    public boolean isAcceptedCurrencyItemId(ResourceLocation itemId) {
        if (itemId == null || !isRegisteredItem(itemId)) {
            return false;
        }
        if (itemId.equals(defaultRewardCurrencyItemId())) {
            return true;
        }
        return ColonyLogisticsConfig.currencyExchangeEnabled() && denominationForItem(itemId).isPresent();
    }

    /** Registered, accepted denominations sorted from largest to smallest. */
    public List<CurrencyDenomination> registeredDenominations() {
        List<CurrencyDenomination> out = new ArrayList<>();
        for (CurrencyDenomination denomination : configuredDenominations()) {
            if (isRegisteredItem(denomination.itemId())) {
                out.add(denomination);
            }
        }
        out.sort(Comparator.comparingInt(CurrencyDenomination::baseValue).reversed());
        return out;
    }

    private List<CurrencyDenomination> configuredDenominations() {
        ResourceLocation base = defaultRewardCurrencyItemId();
        List<CurrencyDenomination> denominations = new ArrayList<>();
        denominations.add(new CurrencyDenomination(base, 1));
        if (ColonyLogisticsConfig.currencyExchangeEnabled()) {
            addConfiguredDenomination(denominations, ColonyLogisticsConfig.currencyGoldCoinItemId(), ColonyLogisticsConfig.currencyGoldCoinValue());
            addConfiguredDenomination(denominations, ColonyLogisticsConfig.currencyDiamondCoinItemId(), ColonyLogisticsConfig.currencyDiamondCoinValue());
        }
        denominations.sort(Comparator.comparingInt(CurrencyDenomination::baseValue).reversed());
        return denominations;
    }

    private void addConfiguredDenomination(List<CurrencyDenomination> denominations, ResourceLocation itemId, int baseValue) {
        if (itemId == null || baseValue <= 0) return;
        for (CurrencyDenomination existing : denominations) {
            if (existing.itemId().equals(itemId)) {
                return;
            }
        }
        denominations.add(new CurrencyDenomination(itemId, baseValue));
    }

    private Optional<CurrencyDenomination> denominationForItem(ResourceLocation itemId) {
        if (itemId == null) return Optional.empty();
        for (CurrencyDenomination denomination : configuredDenominations()) {
            if (denomination.itemId().equals(itemId)) {
                return Optional.of(denomination);
            }
        }
        return Optional.empty();
    }

    private boolean canPayBaseCoinAmount(int baseAmount) {
        if (baseAmount <= 0) return true;
        return !decomposeBaseCoinAmount(baseAmount).isEmpty();
    }

    private List<DenominationPayment> decomposeBaseCoinAmount(int baseAmount) {
        int remaining = Math.max(0, baseAmount);
        List<DenominationPayment> payments = new ArrayList<>();
        for (CurrencyDenomination denomination : registeredDenominations()) {
            int value = Math.max(1, denomination.baseValue());
            int count = remaining / value;
            if (count <= 0) continue;
            payments.add(new DenominationPayment(denomination, count));
            remaining -= count * value;
        }
        if (remaining != 0) {
            return List.of();
        }
        return payments;
    }

    private String formatBreakdown(List<DenominationPayment> payments) {
        if (payments.isEmpty()) {
            return "0 x " + defaultRewardCurrencyItemId();
        }
        List<String> parts = new ArrayList<>();
        for (DenominationPayment payment : payments) {
            parts.add(payment.count() + " x " + payment.denomination().itemId());
        }
        return String.join(", ", parts);
    }

    private void giveItemStacks(ServerPlayer player, ResourceLocation itemId, int amount) {
        int remaining = Math.max(0, amount);
        while (remaining > 0) {
            ItemStack template = createStack(itemId, 1);
            if (template.isEmpty()) return;
            int maxStackSize = Math.max(1, template.getMaxStackSize());
            int chunk = Math.min(maxStackSize, remaining);
            ItemStack stack = createStack(itemId, chunk);
            if (stack.isEmpty()) return;
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
            remaining -= chunk;
        }
    }

    private void addChunkedStacks(List<ItemStack> stacks, ResourceLocation itemId, int amount) {
        int remaining = Math.max(0, amount);
        while (remaining > 0) {
            ItemStack template = createStack(itemId, 1);
            if (template.isEmpty()) return;
            int maxStackSize = Math.max(1, template.getMaxStackSize());
            int chunk = Math.min(maxStackSize, remaining);
            ItemStack stack = createStack(itemId, chunk);
            if (stack.isEmpty()) return;
            stacks.add(stack);
            remaining -= chunk;
        }
    }

    private ItemStack createStack(ResourceLocation itemId, int amount) {
        if (!isRegisteredItem(itemId)) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return new ItemStack(item, Math.max(0, amount));
    }

    private boolean isRegisteredItem(ResourceLocation itemId) {
        return itemId != null && BuiltInRegistries.ITEM.containsKey(itemId) && BuiltInRegistries.ITEM.get(itemId) != Items.AIR;
    }

    private int safeMultiply(int count, int value) {
        long product = (long) Math.max(0, count) * Math.max(1, value);
        return product > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }

    private record DenominationPayment(CurrencyDenomination denomination, int count) {}

    public record PaymentResult(boolean paid, ResourceLocation currencyItemId, int amount, String breakdown) {
        public static PaymentResult paid(ResourceLocation currencyItemId, int amount) {
            return paid(currencyItemId, amount, amount + " x " + currencyItemId);
        }

        public static PaymentResult paid(ResourceLocation currencyItemId, int amount, String breakdown) {
            return new PaymentResult(true, currencyItemId, Math.max(0, amount), breakdown == null ? "" : breakdown);
        }

        public static PaymentResult failed(ResourceLocation currencyItemId, int amount) {
            return new PaymentResult(false, currencyItemId, Math.max(0, amount), "");
        }

        public String displayText() {
            if (breakdown == null || breakdown.isBlank()) {
                return amount + " x " + currencyItemId;
            }
            if (breakdown.startsWith(amount + " x ")) {
                return breakdown;
            }
            return amount + " base (" + breakdown + ")";
        }
    }
}
