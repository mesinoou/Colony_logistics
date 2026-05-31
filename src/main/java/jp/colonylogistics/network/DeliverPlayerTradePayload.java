package jp.colonylogistics.network;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.menu.TradeTerminalMenu;
import jp.colonylogistics.registry.ModBlocks;
import jp.colonylogistics.service.PlayerTradeService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record DeliverPlayerTradePayload(UUID contractId, BlockPos terminalPos) implements CustomPacketPayload {
    public static final Type<DeliverPlayerTradePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "deliver_player_trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeliverPlayerTradePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DeliverPlayerTradePayload decode(RegistryFriendlyByteBuf buf) {
            return new DeliverPlayerTradePayload(buf.readUUID(), buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DeliverPlayerTradePayload payload) {
            buf.writeUUID(payload.contractId());
            buf.writeBlockPos(payload.terminalPos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeliverPlayerTradePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MultiplayerDebugLog.networkReceived(player, "DeliverPlayerTradePayload", payload.terminalPos(), "trade=" + payload.contractId());
            if (!player.blockPosition().closerThan(payload.terminalPos(), 16.0D)) {
                MultiplayerDebugLog.networkRejected(player, "DeliverPlayerTradePayload", payload.terminalPos(), "TOO_FAR", "trade=" + payload.contractId());
                SafeSystemChat.send(player, "You are too far from this logistics building.");
                player.closeContainer();
                return;
            }
            if (!(player.containerMenu instanceof TradeTerminalMenu menu) || !menu.terminalPos().equals(payload.terminalPos())) {
                MultiplayerDebugLog.networkRejected(player, "DeliverPlayerTradePayload", payload.terminalPos(), "MENU_MISMATCH", "trade=" + payload.contractId() + " menu=" + player.containerMenu.getClass().getName());
                SafeSystemChat.send(player, "This Colony Logistics tab does not match the selected building.");
                player.closeContainer();
                return;
            }
            if (!player.serverLevel().getBlockState(payload.terminalPos()).is(ModBlocks.TRADE_TERMINAL.get())) {
                MultiplayerDebugLog.networkRejected(player, "DeliverPlayerTradePayload", payload.terminalPos(), "NOT_TRADE_TERMINAL", "trade=" + payload.contractId() + " block=" + player.serverLevel().getBlockState(payload.terminalPos()).getBlock());
                SafeSystemChat.send(player, "This Colony Logistics tab does not match the selected building.");
                player.closeContainer();
                return;
            }
            PlayerTradeService.DeliverResult result = new PlayerTradeService().deliverTrade(player, payload.contractId(), payload.terminalPos());
            MultiplayerDebugLog.networkResult(player, "DeliverPlayerTradePayload", payload.terminalPos(), result.name(), "trade=" + payload.contractId());
            player.closeContainer();
        });
    }
}
