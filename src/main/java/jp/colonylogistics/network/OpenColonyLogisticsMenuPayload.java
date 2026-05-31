package jp.colonylogistics.network;

import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.dock.ContainerDockBlock;
import jp.colonylogistics.dock.LogisticsOfficeBlock;
import jp.colonylogistics.minecolonies.module.ColonyLogisticsBuildingModuleKind;
import jp.colonylogistics.registry.ModBlocks;
import jp.colonylogistics.terminal.TradeTerminalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** C2S request from the MineColonies building tab into the matching Colony Logistics menu. */
public record OpenColonyLogisticsMenuPayload(BlockPos buildingPos, String buildingKind) implements CustomPacketPayload {
    public static final Type<OpenColonyLogisticsMenuPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "open_colony_logistics_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenColonyLogisticsMenuPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenColonyLogisticsMenuPayload decode(RegistryFriendlyByteBuf buf) {
            return new OpenColonyLogisticsMenuPayload(buf.readBlockPos(), buf.readUtf(64));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, OpenColonyLogisticsMenuPayload payload) {
            buf.writeBlockPos(payload.buildingPos());
            buf.writeUtf(payload.buildingKind(), 64);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenColonyLogisticsMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            MultiplayerDebugLog.networkReceived(player, "OpenColonyLogisticsMenuPayload", payload.buildingPos(), "kind=" + payload.buildingKind());

            if (!player.blockPosition().closerThan(payload.buildingPos(), 16.0D)) {
                MultiplayerDebugLog.networkRejected(player, "OpenColonyLogisticsMenuPayload", payload.buildingPos(), "TOO_FAR", "kind=" + payload.buildingKind());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.too_far"));
                return;
            }

            ServerLevel level = player.serverLevel();
            ColonyLogisticsBuildingModuleKind.byId(payload.buildingKind()).ifPresentOrElse(kind -> {
                InteractionResult result = switch (kind) {
                    case LOGISTICS_OFFICE -> level.getBlockState(payload.buildingPos()).is(ModBlocks.LOGISTICS_OFFICE.get())
                            ? LogisticsOfficeBlock.openLogisticsOfficeBoard(level, payload.buildingPos(), player)
                            : invalidBuilding(player, payload.buildingPos());
                    case CONTAINER_DOCK -> level.getBlockState(payload.buildingPos()).is(ModBlocks.CONTAINER_DOCK.get())
                            ? ContainerDockBlock.openContainerDockMenu(level, payload.buildingPos(), player)
                            : invalidBuilding(player, payload.buildingPos());
                    case TRADE_TERMINAL -> level.getBlockState(payload.buildingPos()).is(ModBlocks.TRADE_TERMINAL.get())
                            ? TradeTerminalBlock.openTradeTerminalMenu(level, payload.buildingPos(), player)
                            : invalidBuilding(player, payload.buildingPos());
                };
                MultiplayerDebugLog.networkResult(player, "OpenColonyLogisticsMenuPayload", payload.buildingPos(), result.toString(), "kind=" + payload.buildingKind());
                if (result == InteractionResult.FAIL) {
                    player.closeContainer();
                }
            }, () -> {
                MultiplayerDebugLog.networkRejected(player, "OpenColonyLogisticsMenuPayload", payload.buildingPos(), "INVALID_KIND", "kind=" + payload.buildingKind());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_kind", payload.buildingKind()));
            });
        });
    }

    private static InteractionResult invalidBuilding(final ServerPlayer player, final BlockPos buildingPos) {
        MultiplayerDebugLog.networkRejected(player, "OpenColonyLogisticsMenuPayload", buildingPos, "INVALID_BUILDING", "openedFromMineColoniesTab");
        SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_building"));
        return InteractionResult.FAIL;
    }
}
