package jp.colonylogistics.network;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.menu.SavedTradeTerminalContainer;
import jp.colonylogistics.menu.TradeTerminalMenu;
import jp.colonylogistics.registry.ModBlocks;
import jp.colonylogistics.service.PlayerTradeService;
import jp.colonylogistics.trade.ItemMatchMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CreatePlayerTradePayload(BlockPos terminalPos, ItemMatchMode matchMode, int requestedCount) implements CustomPacketPayload {
    public static final Type<CreatePlayerTradePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "create_player_trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreatePlayerTradePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CreatePlayerTradePayload decode(RegistryFriendlyByteBuf buf) {
            return new CreatePlayerTradePayload(buf.readBlockPos(), ItemMatchMode.byOrdinal(buf.readVarInt()), buf.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CreatePlayerTradePayload payload) {
            buf.writeBlockPos(payload.terminalPos());
            buf.writeVarInt(payload.matchMode().ordinal());
            buf.writeVarInt(payload.requestedCount());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CreatePlayerTradePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MultiplayerDebugLog.networkReceived(player, "CreatePlayerTradePayload", payload.terminalPos(), "match=" + payload.matchMode() + " requestedCount=" + payload.requestedCount());
            if (!player.blockPosition().closerThan(payload.terminalPos(), 16.0D)) {
                MultiplayerDebugLog.networkRejected(player, "CreatePlayerTradePayload", payload.terminalPos(), "TOO_FAR", "match=" + payload.matchMode() + " requestedCount=" + payload.requestedCount());
                SafeSystemChat.send(player, "You are too far from this logistics building.");
                player.closeContainer();
                return;
            }
            if (!(player.containerMenu instanceof TradeTerminalMenu menu) || !menu.terminalPos().equals(payload.terminalPos())) {
                MultiplayerDebugLog.networkRejected(player, "CreatePlayerTradePayload", payload.terminalPos(), "MENU_MISMATCH", "menu=" + player.containerMenu.getClass().getName());
                SafeSystemChat.send(player, "This Colony Logistics tab does not match the selected building.");
                player.closeContainer();
                return;
            }
            if (!player.serverLevel().getBlockState(payload.terminalPos()).is(ModBlocks.TRADE_TERMINAL.get())) {
                MultiplayerDebugLog.networkRejected(player, "CreatePlayerTradePayload", payload.terminalPos(), "NOT_TRADE_TERMINAL", "block=" + player.serverLevel().getBlockState(payload.terminalPos()).getBlock());
                SafeSystemChat.send(player, "This Colony Logistics tab does not match the selected building.");
                player.closeContainer();
                return;
            }
            net.minecraft.world.item.ItemStack request = menu.setupInventory().getItem(SavedTradeTerminalContainer.SLOT_REQUEST);
            net.minecraft.world.item.ItemStack reward = menu.setupInventory().getItem(SavedTradeTerminalContainer.SLOT_REWARD);
            PlayerTradeService.CreateResult result = new PlayerTradeService().createTrade(
                    player,
                    payload.terminalPos(),
                    payload.matchMode(),
                    payload.requestedCount(),
                    request,
                    reward
            );
            MultiplayerDebugLog.networkResult(player, "CreatePlayerTradePayload", payload.terminalPos(), result.name(), "request=" + MultiplayerDebugLog.stackSummary(request) + " reward=" + MultiplayerDebugLog.stackSummary(reward) + " match=" + payload.matchMode() + " requestedCount=" + payload.requestedCount());
            if (result == PlayerTradeService.CreateResult.SUCCESS) {
                menu.setupInventory().setItem(SavedTradeTerminalContainer.SLOT_REWARD, net.minecraft.world.item.ItemStack.EMPTY);
            }
            player.closeContainer();
        });
    }
}
