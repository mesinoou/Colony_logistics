package jp.colonylogistics.trade;

import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.RewardSpec;
import jp.colonylogistics.currency.CurrencyService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

/**
 * Escrowed player-created trade contract.
 *
 * <p>Phase 13 stored the requested template and escrow reward as full {@link ItemStack}s.
 * Phase 17.9 keeps the NBT shape for compatibility, but newly-created player
 * trades should now escrow only the configured currency item. Currency payouts
 * are exposed as {@link RewardSpec} so Trade Terminal and generated freight use
 * the same payment path.</p>
 */
public record PlayerTradeContract(
        UUID id,
        ContractStatus status,
        UUID creatorPlayer,
        Optional<UUID> deliveredBy,
        int colonyId,
        BlockPos terminalPos,
        ItemStack requestedTemplate,
        ItemStack escrowedReward,
        ItemMatchMode matchMode,
        long createdGameTime,
        long expiresGameTime
) {
    public PlayerTradeContract {
        if (requestedTemplate == null || requestedTemplate.isEmpty()) {
            throw new IllegalArgumentException("requestedTemplate must not be empty");
        }
        if (escrowedReward == null || escrowedReward.isEmpty()) {
            throw new IllegalArgumentException("escrowedReward must not be empty");
        }
        if (matchMode == null) {
            matchMode = ItemMatchMode.ITEM_AND_COMPONENTS;
        }
        requestedTemplate = requestedTemplate.copy();
        escrowedReward = escrowedReward.copy();
    }

    public ResourceLocation requestedItemId() {
        return BuiltInRegistries.ITEM.getKey(requestedTemplate.getItem());
    }

    public int requestedCount() {
        return requestedTemplate.getCount();
    }

    public ResourceLocation rewardItemId() {
        return BuiltInRegistries.ITEM.getKey(escrowedReward.getItem());
    }

    public int rewardCount() {
        return escrowedReward.getCount();
    }

    public RewardSpec rewardSpec() {
        return new CurrencyService()
                .currencyRewardFromStack(escrowedReward)
                .orElse(new RewardSpec(rewardItemId(), rewardCount()));
    }

    public ItemStack requestedTemplateCopy() {
        return requestedTemplate.copy();
    }

    public ItemStack escrowedRewardCopy() {
        return escrowedReward.copy();
    }

    public boolean matchesDelivery(ItemStack offered) {
        return matchMode.matches(offered, requestedTemplate);
    }

    public PlayerTradeContract withStatus(ContractStatus next) {
        return new PlayerTradeContract(
                id, next, creatorPlayer, deliveredBy, colonyId, terminalPos,
                requestedTemplate, escrowedReward, matchMode,
                createdGameTime, expiresGameTime
        );
    }

    public PlayerTradeContract cancel() {
        return withStatus(ContractStatus.CANCELLED);
    }

    public PlayerTradeContract complete(UUID playerId) {
        return new PlayerTradeContract(
                id, ContractStatus.COMPLETED, creatorPlayer, Optional.of(playerId), colonyId, terminalPos,
                requestedTemplate, escrowedReward, matchMode,
                createdGameTime, expiresGameTime
        );
    }
}
