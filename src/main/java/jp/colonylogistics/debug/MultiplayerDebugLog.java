package jp.colonylogistics.debug;

import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.container.ContainerManifest;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.freight.FreightJobSpec;
import jp.colonylogistics.trade.PlayerTradeContract;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side logging helpers for multiplayer beta testing.
 *
 * <p>These logs intentionally focus on player-triggered network actions and the
 * resulting contract lifecycle changes. They are gated by common config so a
 * production server can turn them off without code changes.</p>
 */
public final class MultiplayerDebugLog {
    private static final Logger LOGGER = LoggerFactory.getLogger("ColonyLogistics/Multiplayer");

    public static void networkReceived(ServerPlayer player, String packet, BlockPos targetPos, String details) {
        if (!ColonyLogisticsConfig.debugMultiplayerNetworkLogging()) return;
        LOGGER.info("[CL-MP][network][recv] packet={} player={} uuid={} dim={} playerPos={} targetPos={} {}",
                packet,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(player.blockPosition()),
                pos(targetPos),
                safe(details));
    }

    public static void networkRejected(ServerPlayer player, String packet, BlockPos targetPos, String reason, String details) {
        if (!ColonyLogisticsConfig.debugMultiplayerNetworkLogging()) return;
        LOGGER.warn("[CL-MP][network][reject] packet={} reason={} player={} uuid={} dim={} playerPos={} targetPos={} {}",
                packet,
                reason,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(player.blockPosition()),
                pos(targetPos),
                safe(details));
    }

    public static void networkResult(ServerPlayer player, String packet, BlockPos targetPos, String result, String details) {
        if (!ColonyLogisticsConfig.debugMultiplayerNetworkLogging()) return;
        LOGGER.info("[CL-MP][network][result] packet={} result={} player={} uuid={} dim={} playerPos={} targetPos={} {}",
                packet,
                result,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(player.blockPosition()),
                pos(targetPos),
                safe(details));
    }

    public static void contractGenerated(ServerLevel level, LogisticsContract contract, String source) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.info("[CL-MP][contract][generated] source={} dim={} {}",
                source,
                dim(level),
                contractSummary(contract));
    }

    public static void contractGenerationSkipped(ServerLevel level, String source, String reason, String details) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.warn("[CL-MP][contract][generation-skip] source={} reason={} dim={} {}",
                source,
                reason,
                dim(level),
                safe(details));
    }

    public static void contractAction(ServerPlayer player, String action, LogisticsContract contract, String result, String details) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.info("[CL-MP][contract][{}] result={} player={} uuid={} dim={} playerPos={} {} {}",
                action,
                result,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(player.blockPosition()),
                contractSummary(contract),
                safe(details));
    }

    public static void contractRejected(ServerPlayer player, String action, String contractId, String reason, String details) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.warn("[CL-MP][contract][{}][reject] reason={} contract={} player={} uuid={} dim={} playerPos={} {}",
                action,
                reason,
                contractId,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(player.blockPosition()),
                safe(details));
    }

    public static void containerAction(ServerPlayer player, String action, String result, BlockPos dockPos, BlockPos corePos, String details) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.info("[CL-MP][container][{}] result={} player={} uuid={} dim={} dockPos={} corePos={} {}",
                action,
                result,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(dockPos),
                pos(corePos),
                safe(details));
    }

    public static void playerTradeAction(ServerPlayer player, String action, String result, BlockPos terminalPos, PlayerTradeContract trade, String details) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.info("[CL-MP][trade][{}] result={} player={} uuid={} dim={} terminalPos={} {} {}",
                action,
                result,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(terminalPos),
                tradeSummary(trade),
                safe(details));
    }

    public static void playerTradeRejected(ServerPlayer player, String action, String tradeId, String reason, BlockPos terminalPos, String details) {
        if (!ColonyLogisticsConfig.debugContractLifecycleLogging()) return;
        LOGGER.warn("[CL-MP][trade][{}][reject] reason={} trade={} player={} uuid={} dim={} terminalPos={} {}",
                action,
                reason,
                tradeId,
                playerName(player),
                player.getUUID(),
                dim(player.serverLevel()),
                pos(terminalPos),
                safe(details));
    }

    public static String contractSummary(LogisticsContract contract) {
        String freight = contract.freightSpec()
                .map(MultiplayerDebugLog::freightSummary)
                .orElse("freight=-");
        return "contract=" + contract.id()
                + " type=" + contract.type()
                + " status=" + contract.status()
                + " originColony=" + contract.originColonyId()
                + " destColony=" + contract.destinationColonyId()
                + " originPos=" + contract.originDockPos().map(MultiplayerDebugLog::pos).orElse("-")
                + " destPos=" + contract.destinationDockPos().map(MultiplayerDebugLog::pos).orElse("-")
                + " assigned=" + contract.assignedPlayer().map(Object::toString).orElse("-")
                + " reward=" + contract.reward().currencyAmount() + "x" + contract.reward().currencyItemId()
                + " containers=" + contract.deliveredContainerCount() + "/" + contract.spawnedContainerCount() + "/" + contract.requiredContainerCount()
                + " " + freight;
    }

    public static String manifestSummary(ContainerManifest manifest) {
        return "manifest=" + manifest.containerId()
                + " contract=" + manifest.contractId()
                + " originColony=" + manifest.originColonyId()
                + " destColony=" + manifest.destinationColonyId()
                + " assigned=" + manifest.assignedPlayer()
                + " originDock=" + pos(manifest.originDockPos())
                + " destDock=" + pos(manifest.destinationDockPos())
                + " size=" + manifest.size()
                + " weight=" + manifest.weightClass()
                + " batch=" + manifest.batchIndex() + "/" + manifest.batchCount()
                + " sealed=" + manifest.sealed();
    }

    public static String tradeSummary(PlayerTradeContract trade) {
        return "trade=" + trade.id()
                + " status=" + trade.status()
                + " creator=" + trade.creatorPlayer()
                + " fulfiller=" + trade.deliveredBy().map(Object::toString).orElse("-")
                + " colony=" + trade.colonyId()
                + " request=" + stackSummary(trade.requestedTemplate())
                + " requestedCount=" + trade.requestedCount()
                + " reward=" + stackSummary(trade.escrowedReward())
                + " match=" + trade.matchMode();
    }

    public static String stackSummary(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "empty";
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stack.getCount() + "x" + itemId;
    }

    public static String pos(BlockPos pos) {
        return pos == null ? "-" : pos.toShortString();
    }

    private static String freightSummary(FreightJobSpec spec) {
        String cargo = spec.cargo().stream()
                .findFirst()
                .map(c -> c.cargoId() + "x" + c.amount())
                .orElse("unknown");
        return "freight=" + spec.deliveryUnitType()
                + " cargo=" + cargo
                + " difficulty=" + spec.difficulty()
                + " carrierLevel=" + spec.requiredCarrierLevel()
                + " container=" + spec.containerRequirement().size() + "/" + spec.containerRequirement().weightClass()
                + " deadlines=" + spec.pickupDeadline() + "/" + spec.deliveryDeadline();
    }

    private static String dim(ServerLevel level) {
        return level.dimension().location().toString();
    }

    private static String playerName(ServerPlayer player) {
        return player.getGameProfile().getName();
    }

    private static String safe(String details) {
        return details == null || details.isBlank() ? "" : details;
    }

    private MultiplayerDebugLog() {}
}
