package jp.colonylogistics.service;

import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.contract.RewardSpec;
import jp.colonylogistics.currency.CurrencyService;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.trade.ItemMatchMode;
import jp.colonylogistics.trade.PlayerTradeContract;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

/** Server-side validation for player-created escrow trades. */
public final class PlayerTradeService {
    public CreateResult createTrade(ServerPlayer player, BlockPos terminalPos, ItemMatchMode matchMode, int requestedCount, ItemStack requestInput, ItemStack rewardInput) {
        ServerLevel level = player.serverLevel();
        ResolvedLogisticsBuilding terminal = ResolvedLogisticsBuilding.resolve(level, terminalPos);
        if (!terminal.usable()) {
            MultiplayerDebugLog.playerTradeRejected(player, "create", "new", CreateResult.INACTIVE_TERMINAL.name(), terminalPos, "");
            SafeSystemChat.send(player, "This Trade Terminal is not active yet.");
            return CreateResult.INACTIVE_TERMINAL;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        ItemStack request = requestInput.copy();
        ItemStack reward = rewardInput.copy();
        int clampedRequestedCount = Math.max(1, Math.min(64, requestedCount));
        if (request.isEmpty() || reward.isEmpty()) {
            MultiplayerDebugLog.playerTradeRejected(player, "create", "new", CreateResult.MISSING_ITEMS.name(), terminalPos, "request=" + MultiplayerDebugLog.stackSummary(request) + " reward=" + MultiplayerDebugLog.stackSummary(reward));
            SafeSystemChat.send(player, "Place a request sample and an escrow reward before creating a trade.");
            return CreateResult.MISSING_ITEMS;
        }

        CurrencyService currencyService = new CurrencyService();
        ItemStack escrowReward = reward.copy();
        if (ColonyLogisticsConfig.playerTradeRewardsMustBeCurrency()) {
            Optional<RewardSpec> rewardSpec = currencyService.currencyRewardFromStack(reward);
            if (rewardSpec.isEmpty()) {
                MultiplayerDebugLog.playerTradeRejected(player, "create", "new", CreateResult.REWARD_NOT_CURRENCY.name(), terminalPos, "reward=" + MultiplayerDebugLog.stackSummary(reward));
                SafeSystemChat.send(player, "Escrow rewards must use a configured currency coin only. Base currency: " + SafeSystemChat.sanitize(currencyService.defaultRewardCurrencyItemId()));
                return CreateResult.REWARD_NOT_CURRENCY;
            }
            if (currencyService.payableReward(rewardSpec.get()).isEmpty()) {
                MultiplayerDebugLog.playerTradeRejected(player, "create", "new", CreateResult.INVALID_REWARD.name(), terminalPos, "reward=" + MultiplayerDebugLog.stackSummary(reward));
                SafeSystemChat.send(player, "The escrowed reward item is not valid.");
                return CreateResult.INVALID_REWARD;
            }
            // Keep the actual escrow stack so gold/diamond coin deposits remain visible in history.
            // rewardSpec() converts it back into base-coin value at payout/refund time.
        }

        long openForTerminal = data.openPlayerTradesForTerminal(terminalPos).count();
        if (openForTerminal >= 8) {
            MultiplayerDebugLog.playerTradeRejected(player, "create", "new", CreateResult.TOO_MANY_OPEN.name(), terminalPos, "openForTerminal=" + openForTerminal);
            SafeSystemChat.send(player, "This terminal already has too many open trades.");
            return CreateResult.TOO_MANY_OPEN;
        }

        PlayerTradeContract trade = new PlayerTradeContract(
                UUID.randomUUID(),
                ContractStatus.OPEN,
                player.getUUID(),
                Optional.empty(),
                terminal.colonyId(),
                terminalPos.immutable(),
                requestedTemplate(request, clampedRequestedCount),
                escrowReward,
                matchMode,
                level.getGameTime(),
                0L
        );
        data.putPlayerTrade(trade);
        MultiplayerDebugLog.playerTradeAction(player, "create", CreateResult.SUCCESS.name(), terminalPos, trade, "");
        SafeSystemChat.send(player, "Created player trade: " + SafeSystemChat.sanitize(trade.id()));
        return CreateResult.SUCCESS;
    }


    private ItemStack requestedTemplate(ItemStack sample, int requestedCount) {
        ItemStack template = sample.copy();
        template.setCount(requestedCount);
        return template;
    }

    public DeliverResult deliverTrade(ServerPlayer player, UUID contractId, BlockPos terminalPos) {
        ServerLevel level = player.serverLevel();
        ResolvedLogisticsBuilding terminal = ResolvedLogisticsBuilding.resolve(level, terminalPos);
        if (!terminal.usable()) {
            MultiplayerDebugLog.playerTradeRejected(player, "deliver", contractId.toString(), DeliverResult.INACTIVE_TERMINAL.name(), terminalPos, "");
            SafeSystemChat.send(player, "This Trade Terminal is not active yet.");
            return DeliverResult.INACTIVE_TERMINAL;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<PlayerTradeContract> optionalTrade = data.playerTrade(contractId);
        if (optionalTrade.isEmpty()) {
            MultiplayerDebugLog.playerTradeRejected(player, "deliver", contractId.toString(), DeliverResult.UNKNOWN_TRADE.name(), terminalPos, "");
            SafeSystemChat.send(player, "Unknown player trade.");
            return DeliverResult.UNKNOWN_TRADE;
        }

        PlayerTradeContract trade = optionalTrade.get();
        if (trade.status() != ContractStatus.OPEN) {
            MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.NOT_OPEN.name(), terminalPos, trade, "");
            SafeSystemChat.send(player, "That player trade is no longer open.");
            return DeliverResult.NOT_OPEN;
        }
        if (!trade.terminalPos().equals(terminalPos)) {
            MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.WRONG_TERMINAL.name(), terminalPos, trade, "actualTerminal=" + MultiplayerDebugLog.pos(trade.terminalPos()));
            SafeSystemChat.send(player, "That trade belongs to another terminal.");
            return DeliverResult.WRONG_TERMINAL;
        }
        if (trade.creatorPlayer().equals(player.getUUID()) && !ColonyLogisticsConfig.allowSelfDeliveryForTesting()) {
            MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.SELF_DELIVERY.name(), terminalPos, trade, "");
            SafeSystemChat.send(player, "You cannot deliver your own trade.");
            return DeliverResult.SELF_DELIVERY;
        }
        if (!hasMatchingStackCount(player, trade.requestedTemplate(), trade.requestedCount(), trade.matchMode())) {
            MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.MISSING_DELIVERY_ITEMS.name(), terminalPos, trade, "");
            SafeSystemChat.send(player, "Missing delivery items: " + trade.requestedCount() + " x " + SafeSystemChat.sanitize(trade.requestedTemplate().getHoverName().getString()));
            return DeliverResult.MISSING_DELIVERY_ITEMS;
        }

        CurrencyService currencyService = new CurrencyService();
        Optional<RewardSpec> payableReward = currencyService.payableReward(trade.rewardSpec());
        if (payableReward.isEmpty()) {
            MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.INVALID_REWARD.name(), terminalPos, trade, "reward=" + trade.rewardItemId());
            SafeSystemChat.send(player, "Reward currency is unavailable, so the payout could not be made: " + SafeSystemChat.sanitize(trade.rewardItemId()));
            data.replacePlayerTrade(trade.withStatus(ContractStatus.FAILED));
            return DeliverResult.INVALID_REWARD;
        }

        if (trade.creatorPlayer().equals(player.getUUID())) {
            SafeSystemChat.send(player, "Testing mode: delivered your own trade.");
        }
        consumeMatchingStackCount(player, trade.requestedTemplate(), trade.requestedCount(), trade.matchMode());
        CurrencyService.PaymentResult payment = currencyService.payResolvedToPlayer(player, payableReward.get());
        if (!payment.paid()) {
            MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.INVALID_REWARD.name(), terminalPos, trade, "payment=" + payment.displayText());
            SafeSystemChat.send(player, "Reward currency is unavailable, so the payout could not be made: " + SafeSystemChat.sanitize(payableReward.get().currencyItemId()));
            data.replacePlayerTrade(trade.withStatus(ContractStatus.FAILED));
            return DeliverResult.INVALID_REWARD;
        }
        PlayerTradeContract completed = trade.complete(player.getUUID());
        data.replacePlayerTrade(completed);
        MultiplayerDebugLog.playerTradeAction(player, "deliver", DeliverResult.SUCCESS.name(), terminalPos, completed, "payment=" + payment.displayText());
        SafeSystemChat.send(player, "Trade delivered. Reward paid: " + SafeSystemChat.sanitize(payment.displayText()));
        return DeliverResult.SUCCESS;
    }

    public CancelResult cancelTrade(ServerPlayer player, UUID contractId, BlockPos terminalPos) {
        ServerLevel level = player.serverLevel();
        ResolvedLogisticsBuilding terminal = ResolvedLogisticsBuilding.resolve(level, terminalPos);
        if (!terminal.usable()) {
            MultiplayerDebugLog.playerTradeRejected(player, "cancel", contractId.toString(), CancelResult.INACTIVE_TERMINAL.name(), terminalPos, "");
            SafeSystemChat.send(player, "This Trade Terminal is not active yet.");
            return CancelResult.INACTIVE_TERMINAL;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<PlayerTradeContract> optionalTrade = data.playerTrade(contractId);
        if (optionalTrade.isEmpty()) {
            MultiplayerDebugLog.playerTradeRejected(player, "cancel", contractId.toString(), CancelResult.UNKNOWN_TRADE.name(), terminalPos, "");
            SafeSystemChat.send(player, "Unknown player trade.");
            return CancelResult.UNKNOWN_TRADE;
        }

        PlayerTradeContract trade = optionalTrade.get();
        if (trade.status() != ContractStatus.OPEN) {
            MultiplayerDebugLog.playerTradeAction(player, "cancel", CancelResult.NOT_OPEN.name(), terminalPos, trade, "");
            SafeSystemChat.send(player, "That player trade can no longer be cancelled.");
            return CancelResult.NOT_OPEN;
        }
        if (!trade.terminalPos().equals(terminalPos)) {
            MultiplayerDebugLog.playerTradeAction(player, "cancel", CancelResult.WRONG_TERMINAL.name(), terminalPos, trade, "actualTerminal=" + MultiplayerDebugLog.pos(trade.terminalPos()));
            SafeSystemChat.send(player, "That trade belongs to another terminal.");
            return CancelResult.WRONG_TERMINAL;
        }
        if (!trade.creatorPlayer().equals(player.getUUID())) {
            MultiplayerDebugLog.playerTradeAction(player, "cancel", CancelResult.NOT_OWNER.name(), terminalPos, trade, "");
            SafeSystemChat.send(player, "Only the trade creator can cancel this trade.");
            return CancelResult.NOT_OWNER;
        }

        CurrencyService currencyService = new CurrencyService();
        Optional<RewardSpec> payableRefund = currencyService.payableReward(trade.rewardSpec());
        if (payableRefund.isEmpty()) {
            MultiplayerDebugLog.playerTradeAction(player, "cancel", CancelResult.INVALID_REWARD.name(), terminalPos, trade, "reward=" + trade.rewardItemId());
            SafeSystemChat.send(player, "Reward currency is unavailable, so the payout could not be made: " + SafeSystemChat.sanitize(trade.rewardItemId()));
            data.replacePlayerTrade(trade.withStatus(ContractStatus.FAILED));
            return CancelResult.INVALID_REWARD;
        }

        CurrencyService.PaymentResult payment = currencyService.payResolvedToPlayer(player, payableRefund.get());
        if (!payment.paid()) {
            MultiplayerDebugLog.playerTradeAction(player, "cancel", CancelResult.INVALID_REWARD.name(), terminalPos, trade, "payment=" + payment.displayText());
            SafeSystemChat.send(player, "Reward currency is unavailable, so the refund could not be made: " + SafeSystemChat.sanitize(payableRefund.get().currencyItemId()));
            data.replacePlayerTrade(trade.withStatus(ContractStatus.FAILED));
            return CancelResult.INVALID_REWARD;
        }
        PlayerTradeContract cancelled = trade.cancel();
        data.replacePlayerTrade(cancelled);
        MultiplayerDebugLog.playerTradeAction(player, "cancel", CancelResult.SUCCESS.name(), terminalPos, cancelled, "refund=" + payment.displayText());
        SafeSystemChat.send(player, "Trade cancelled. Escrow refund: " + SafeSystemChat.sanitize(payment.displayText()));
        return CancelResult.SUCCESS;
    }

    private boolean hasMatchingStackCount(ServerPlayer player, ItemStack template, int required, ItemMatchMode mode) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && mode.matches(stack, template)) {
                count += stack.getCount();
                if (count >= required) return true;
            }
        }
        return false;
    }

    private void consumeMatchingStackCount(ServerPlayer player, ItemStack template, int required, ItemMatchMode mode) {
        int remaining = required;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !mode.matches(stack, template)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
    }

    public enum CreateResult { SUCCESS, INACTIVE_TERMINAL, MISSING_ITEMS, TOO_MANY_OPEN, REWARD_NOT_CURRENCY, INVALID_REWARD }
    public enum DeliverResult { SUCCESS, INACTIVE_TERMINAL, UNKNOWN_TRADE, NOT_OPEN, WRONG_TERMINAL, SELF_DELIVERY, MISSING_DELIVERY_ITEMS, INVALID_REWARD }
    public enum CancelResult { SUCCESS, INACTIVE_TERMINAL, UNKNOWN_TRADE, NOT_OPEN, WRONG_TERMINAL, NOT_OWNER, INVALID_REWARD }
}
