package jp.colonylogistics.network;

import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.board.FreightBoardSnapshots;
import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.menu.FreightBoardMenu;
import jp.colonylogistics.registry.ModBlocks;
import jp.colonylogistics.service.ContractService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

/** C2S request sent by the Logistics Office board Cancel button for assigned generated freight. */
public record CancelFreightPayload(BlockPos officePos, UUID contractId) implements CustomPacketPayload {
    public static final Type<CancelFreightPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "cancel_freight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelFreightPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CancelFreightPayload decode(RegistryFriendlyByteBuf buf) {
            return new CancelFreightPayload(buf.readBlockPos(), buf.readUUID());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CancelFreightPayload payload) {
            buf.writeBlockPos(payload.officePos());
            buf.writeUUID(payload.contractId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CancelFreightPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ServerLevel level = player.serverLevel();
            MultiplayerDebugLog.networkReceived(player, "CancelFreightPayload", payload.officePos(), "contract=" + payload.contractId());

            if (!player.blockPosition().closerThan(payload.officePos(), 16.0D)) {
                MultiplayerDebugLog.networkRejected(player, "CancelFreightPayload", payload.officePos(), "TOO_FAR", "contract=" + payload.contractId());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.too_far"));
                player.closeContainer();
                return;
            }

            if (!(player.containerMenu instanceof FreightBoardMenu menu) || !menu.boardPos().equals(payload.officePos())) {
                MultiplayerDebugLog.networkRejected(player, "CancelFreightPayload", payload.officePos(), "MENU_MISMATCH", "contract=" + payload.contractId() + " menu=" + player.containerMenu.getClass().getName());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.minecolonies_tab.invalid_building"));
                player.closeContainer();
                return;
            }

            if (!level.getBlockState(payload.officePos()).is(ModBlocks.LOGISTICS_OFFICE.get())) {
                MultiplayerDebugLog.networkRejected(player, "CancelFreightPayload", payload.officePos(), "NOT_LOGISTICS_OFFICE", "contract=" + payload.contractId() + " block=" + level.getBlockState(payload.officePos()).getBlock());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.accept_requires_office"));
                player.closeContainer();
                return;
            }

            ResolvedLogisticsBuilding office = ResolvedLogisticsBuilding.resolve(level, payload.officePos());
            if (!office.usable() || office.colonyId() < 0) {
                MultiplayerDebugLog.networkRejected(player, "CancelFreightPayload", payload.officePos(), "INACTIVE_OFFICE", "contract=" + payload.contractId() + " colony=" + office.colonyId());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.logistics_office.inactive"));
                player.closeContainer();
                return;
            }

            LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
            Optional<LogisticsContract> optionalContract = data.contract(payload.contractId());
            if (optionalContract.isEmpty()) {
                MultiplayerDebugLog.networkRejected(player, "CancelFreightPayload", payload.officePos(), "UNKNOWN_CONTRACT", "contract=" + payload.contractId());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_unknown"));
                player.closeContainer();
                return;
            }

            LogisticsContract contract = optionalContract.get();
            if (!FreightBoardSnapshots.isRelatedToColony(contract, office.colonyId())) {
                MultiplayerDebugLog.networkRejected(player, "CancelFreightPayload", payload.officePos(), "WRONG_COLONY", MultiplayerDebugLog.contractSummary(contract) + " officeColony=" + office.colonyId());
                SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.not_office_colony"));
                player.closeContainer();
                return;
            }

            boolean cancelled = new ContractService().cancelAcceptedFreight(player, payload.contractId());
            MultiplayerDebugLog.networkResult(player, "CancelFreightPayload", payload.officePos(), cancelled ? "SUCCESS" : "SERVICE_REJECTED", MultiplayerDebugLog.contractSummary(contract));
            player.closeContainer();
        });
    }
}
