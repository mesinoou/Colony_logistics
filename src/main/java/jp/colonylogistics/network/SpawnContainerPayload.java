package jp.colonylogistics.network;

import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.debug.MultiplayerDebugLog;
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
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** C2S request sent by the Container Dock Spawn button. */
public record SpawnContainerPayload(UUID contractId, BlockPos dockPos, BlockPos corePos) implements CustomPacketPayload {
    public static final Type<SpawnContainerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "spawn_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnContainerPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SpawnContainerPayload decode(RegistryFriendlyByteBuf buf) {
            return new SpawnContainerPayload(buf.readUUID(), buf.readBlockPos(), buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SpawnContainerPayload payload) {
            buf.writeUUID(payload.contractId());
            buf.writeBlockPos(payload.dockPos());
            buf.writeBlockPos(payload.corePos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpawnContainerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            MultiplayerDebugLog.networkReceived(player, "SpawnContainerPayload", payload.dockPos(), "contract=" + payload.contractId() + " suggestedCore=" + MultiplayerDebugLog.pos(payload.corePos()));
            if (!player.blockPosition().closerThan(payload.dockPos(), 12.0D)) {
                MultiplayerDebugLog.networkRejected(player, "SpawnContainerPayload", payload.dockPos(), "TOO_FAR", "contract=" + payload.contractId());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.dock.too_far_from_dock"));
                player.closeContainer();
                return;
            }

            if (!(player.containerMenu instanceof ContainerDockMenu menu) || !menu.dockPos().equals(payload.dockPos())) {
                MultiplayerDebugLog.networkRejected(player, "SpawnContainerPayload", payload.dockPos(), "MENU_MISMATCH", "contract=" + payload.contractId() + " menu=" + player.containerMenu.getClass().getName());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_building"));
                player.closeContainer();
                return;
            }

            if (!player.serverLevel().getBlockState(payload.dockPos()).is(ModBlocks.CONTAINER_DOCK.get())) {
                MultiplayerDebugLog.networkRejected(player, "SpawnContainerPayload", payload.dockPos(), "NOT_CONTAINER_DOCK", "contract=" + payload.contractId() + " block=" + player.serverLevel().getBlockState(payload.dockPos()).getBlock());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_building"));
                player.closeContainer();
                return;
            }

            ContainerDockService.SpawnResult result = new ContainerDockService().spawnForAcceptedContract(player, payload.dockPos(), payload.contractId(), payload.corePos());
            MultiplayerDebugLog.networkResult(player, "SpawnContainerPayload", payload.dockPos(), result.name(), "contract=" + payload.contractId() + " suggestedCore=" + MultiplayerDebugLog.pos(payload.corePos()));
            SafeSystemChat.send(player, ContainerDockService.spawnResultMessage(result));
            player.closeContainer();
        });
    }
}
