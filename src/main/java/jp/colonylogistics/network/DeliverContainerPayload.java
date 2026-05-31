package jp.colonylogistics.network;

import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import jp.colonylogistics.menu.ContainerDockMenu;
import jp.colonylogistics.registry.ModBlocks;
import jp.colonylogistics.service.ContainerDockService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** C2S request sent by the Container Dock Deliver button. */
public record DeliverContainerPayload(BlockPos dockPos, BlockPos corePos) implements CustomPacketPayload {
    public static final Type<DeliverContainerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "deliver_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeliverContainerPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DeliverContainerPayload decode(RegistryFriendlyByteBuf buf) {
            return new DeliverContainerPayload(buf.readBlockPos(), buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DeliverContainerPayload payload) {
            buf.writeBlockPos(payload.dockPos());
            buf.writeBlockPos(payload.corePos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeliverContainerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            MultiplayerDebugLog.networkReceived(player, "DeliverContainerPayload", payload.dockPos(), "core=" + MultiplayerDebugLog.pos(payload.corePos()));
            if (!player.blockPosition().closerThan(payload.dockPos(), 12.0D)) {
                MultiplayerDebugLog.networkRejected(player, "DeliverContainerPayload", payload.dockPos(), "TOO_FAR", "core=" + MultiplayerDebugLog.pos(payload.corePos()));
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.dock.too_far_from_dock"));
                player.closeContainer();
                return;
            }

            if (!(player.containerMenu instanceof ContainerDockMenu menu) || !menu.dockPos().equals(payload.dockPos())) {
                MultiplayerDebugLog.networkRejected(player, "DeliverContainerPayload", payload.dockPos(), "MENU_MISMATCH", "core=" + MultiplayerDebugLog.pos(payload.corePos()) + " menu=" + player.containerMenu.getClass().getName());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_building"));
                player.closeContainer();
                return;
            }

            if (!player.serverLevel().getBlockState(payload.dockPos()).is(ModBlocks.CONTAINER_DOCK.get())) {
                MultiplayerDebugLog.networkRejected(player, "DeliverContainerPayload", payload.dockPos(), "NOT_CONTAINER_DOCK", "core=" + MultiplayerDebugLog.pos(payload.corePos()) + " block=" + player.serverLevel().getBlockState(payload.dockPos()).getBlock());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_building"));
                player.closeContainer();
                return;
            }

            BlockEntity containerEntity = player.serverLevel().getBlockEntity(payload.corePos());
            if (!(containerEntity instanceof FreightContainerCoreBlockEntity container)) {
                MultiplayerDebugLog.networkRejected(player, "DeliverContainerPayload", payload.dockPos(), "INVALID_CONTAINER_CORE", "core=" + MultiplayerDebugLog.pos(payload.corePos()) + " blockEntity=" + (containerEntity == null ? "null" : containerEntity.getClass().getName()));
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.dock.invalid_container"));
                player.closeContainer();
                return;
            }

            ContainerDockService.DeliveryResult result = new ContainerDockService().deliverContainer(player, payload.dockPos(), container);
            MultiplayerDebugLog.networkResult(player, "DeliverContainerPayload", payload.dockPos(), result.name(), "core=" + MultiplayerDebugLog.pos(payload.corePos()));
            SafeSystemChat.send(player, ContainerDockService.deliveryResultMessage(result));
            player.closeContainer();
        });
    }
}
