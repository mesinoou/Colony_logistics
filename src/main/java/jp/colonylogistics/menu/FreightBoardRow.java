package jp.colonylogistics.menu;

import jp.colonylogistics.container.ContainerRequirement;
import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.freight.FreightJobSpec;
import jp.colonylogistics.freight.VirtualCargo;
import jp.colonylogistics.trade.PlayerTradeContract;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Optional;
import java.util.UUID;

/**
 * Compact immutable row sent from server to client when a Freight Board menu opens.
 *
 * <p>Phase 16.28 extends the snapshot with compact origin/destination position text so
 * the Freight Board can show a selected-row details pane without another server round trip.</p>
 */
public record FreightBoardRow(
        UUID contractId,
        String sourceType,
        DeliveryUnitType deliveryUnitType,
        ContractStatus status,
        int originColonyId,
        int destinationColonyId,
        String cargoName,
        String containerText,
        int rewardAmount,
        String currencyId,
        String difficulty,
        long expiresGameTime,
        String assigneeText,
        String originPositionText,
        String destinationPositionText
) {
    public boolean canAccept() {
        return isFreight() && status == ContractStatus.OPEN;
    }

    public boolean canCancel() {
        return isFreight() && assignedToViewer()
                && (status == ContractStatus.ACCEPTED || status == ContractStatus.PICKED_UP || status == ContractStatus.DELIVERED);
    }

    public boolean isFreight() {
        return "FREIGHT".equals(sourceType);
    }

    public boolean isPlayerTrade() {
        return "PLAYER_TRADE".equals(sourceType);
    }

    public boolean assignedToViewer() {
        return "SELF".equals(assigneeText);
    }

    public boolean assignedToOther() {
        return "OTHER".equals(assigneeText);
    }

    public boolean activeForViewer() {
        return assignedToViewer() && (status == ContractStatus.ACCEPTED || status == ContractStatus.PICKED_UP || status == ContractStatus.DELIVERED);
    }

    public String shortContractId() {
        String text = contractId.toString();
        return text.substring(0, Math.min(8, text.length()));
    }

    public static FreightBoardRow fromContract(LogisticsContract contract) {
        return fromContract(contract, Optional.empty());
    }

    public static FreightBoardRow fromContract(LogisticsContract contract, UUID viewerId) {
        return fromContract(contract, Optional.of(viewerId));
    }

    private static FreightBoardRow fromContract(LogisticsContract contract, Optional<UUID> viewerId) {
        Optional<FreightJobSpec> optionalSpec = contract.freightSpec();
        DeliveryUnitType type = optionalSpec.map(FreightJobSpec::deliveryUnitType).orElse(DeliveryUnitType.INVENTORY_ITEM);
        String cargoName = optionalSpec.flatMap(spec -> spec.cargo().stream().findFirst())
                .map(VirtualCargo::cargoId)
                .map(Object::toString)
                .orElse("unknown");
        String containerText = optionalSpec
                .map(FreightJobSpec::containerRequirement)
                .filter(ContainerRequirement::requiresContainer)
                .map(req -> req.size().name() + " / " + req.weightClass().name())
                .orElse("-");
        String difficulty = optionalSpec.map(FreightJobSpec::difficulty).map(Enum::name).orElse("UNKNOWN");
        return new FreightBoardRow(
                contract.id(),
                "FREIGHT",
                type,
                contract.status(),
                contract.originColonyId(),
                contract.destinationColonyId(),
                cargoName,
                containerText,
                contract.reward().currencyAmount(),
                contract.reward().currencyItemId().toString(),
                difficulty,
                contract.expiresGameTime(),
                assigneeText(contract, viewerId),
                contract.originDockPos().map(FreightBoardRow::posText).orElse("-"),
                contract.destinationDockPos().map(FreightBoardRow::posText).orElse("-")
        );
    }


    public static FreightBoardRow fromPlayerTrade(PlayerTradeContract contract, UUID viewerId) {
        String assignee = playerTradeAssigneeText(contract, viewerId);
        String cargo = contract.requestedCount() + "x " + contract.requestedItemId();
        String terminal = "terminal " + posText(contract.terminalPos());
        return new FreightBoardRow(
                contract.id(),
                "PLAYER_TRADE",
                DeliveryUnitType.INVENTORY_ITEM,
                contract.status(),
                contract.colonyId(),
                contract.colonyId(),
                cargo,
                "-",
                contract.rewardCount(),
                contract.rewardItemId().toString(),
                "TRADE",
                contract.expiresGameTime(),
                assignee,
                terminal,
                terminal
        );
    }

    private static String playerTradeAssigneeText(PlayerTradeContract contract, UUID viewerId) {
        if (contract.creatorPlayer().equals(viewerId)) {
            return "SELF";
        }
        if (contract.deliveredBy().filter(viewerId::equals).isPresent()) {
            return "SELF";
        }
        if (contract.deliveredBy().isPresent()) {
            return "OTHER";
        }
        return "NONE";
    }

    private static String assigneeText(LogisticsContract contract, Optional<UUID> viewerId) {
        if (contract.assignedPlayer().isEmpty()) {
            return "NONE";
        }
        if (viewerId.isPresent() && contract.assignedPlayer().get().equals(viewerId.get())) {
            return "SELF";
        }
        return "OTHER";
    }

    private static String posText(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(contractId);
        buf.writeUtf(sourceType);
        buf.writeUtf(deliveryUnitType.name());
        buf.writeUtf(status.name());
        buf.writeInt(originColonyId);
        buf.writeInt(destinationColonyId);
        buf.writeUtf(cargoName);
        buf.writeUtf(containerText);
        buf.writeInt(rewardAmount);
        buf.writeUtf(currencyId);
        buf.writeUtf(difficulty);
        buf.writeLong(expiresGameTime);
        buf.writeUtf(assigneeText);
        buf.writeUtf(originPositionText);
        buf.writeUtf(destinationPositionText);
    }

    public static FreightBoardRow read(RegistryFriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        String sourceType = buf.readUtf();
        DeliveryUnitType type;
        try {
            type = DeliveryUnitType.valueOf(buf.readUtf());
        } catch (IllegalArgumentException ex) {
            type = DeliveryUnitType.INVENTORY_ITEM;
        }
        ContractStatus status;
        try {
            status = ContractStatus.valueOf(buf.readUtf());
        } catch (IllegalArgumentException ex) {
            status = ContractStatus.OPEN;
        }
        return new FreightBoardRow(
                id,
                sourceType,
                type,
                status,
                buf.readInt(),
                buf.readInt(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readLong(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf()
        );
    }
}
