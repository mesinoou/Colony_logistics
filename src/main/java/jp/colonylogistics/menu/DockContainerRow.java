package jp.colonylogistics.menu;

import jp.colonylogistics.container.ContainerManifest;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;

/**
 * Snapshot row for a nearby sealed container that may be delivered at a Dock.
 *
 * <p>During the Create Aeronautics test phase this row intentionally carries
 * debug fields, so the Dock GUI can explain why a returned multiblock is or is
 * not currently deliverable.</p>
 */
public record DockContainerRow(
        UUID containerId,
        UUID contractId,
        int originColonyId,
        int destinationColonyId,
        String size,
        String weightClass,
        BlockPos corePos,
        boolean deliverable,
        String deliveryStatus,
        double distance,
        String contractStatus,
        String cargoName,
        String expectedContainerText,
        BlockPos originDockPos,
        BlockPos destinationDockPos,
        String issueHint,
        int batchIndex,
        int batchCount
) {
    public static DockContainerRow fromBlockEntity(FreightContainerCoreBlockEntity container) {
        ContainerManifest manifest = container.manifest().orElseThrow();
        return new DockContainerRow(
                manifest.containerId(),
                manifest.contractId(),
                manifest.originColonyId(),
                manifest.destinationColonyId(),
                manifest.size().name(),
                manifest.weightClass().name(),
                container.getBlockPos(),
                true,
                "OK",
                0.0D,
                "UNKNOWN",
                "unknown",
                manifest.size().name() + " / " + manifest.weightClass().name(),
                manifest.originDockPos(),
                manifest.destinationDockPos(),
                "Ready",
                manifest.batchIndex(),
                manifest.batchCount()
        );
    }

    public static DockContainerRow debug(
            FreightContainerCoreBlockEntity container,
            boolean deliverable,
            String deliveryStatus,
            double distance,
            String contractStatus,
            String cargoName,
            String expectedContainerText,
            String issueHint
    ) {
        ContainerManifest manifest = container.manifest().orElseThrow();
        return new DockContainerRow(
                manifest.containerId(),
                manifest.contractId(),
                manifest.originColonyId(),
                manifest.destinationColonyId(),
                manifest.size().name(),
                manifest.weightClass().name(),
                container.getBlockPos(),
                deliverable,
                deliveryStatus,
                distance,
                contractStatus,
                cargoName,
                expectedContainerText,
                manifest.originDockPos(),
                manifest.destinationDockPos(),
                issueHint,
                manifest.batchIndex(),
                manifest.batchCount()
        );
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(containerId);
        buf.writeUUID(contractId);
        buf.writeInt(originColonyId);
        buf.writeInt(destinationColonyId);
        buf.writeUtf(size);
        buf.writeUtf(weightClass);
        buf.writeBlockPos(corePos);
        buf.writeBoolean(deliverable);
        buf.writeUtf(deliveryStatus);
        buf.writeDouble(distance);
        buf.writeUtf(contractStatus);
        buf.writeUtf(cargoName);
        buf.writeUtf(expectedContainerText);
        buf.writeBlockPos(originDockPos);
        buf.writeBlockPos(destinationDockPos);
        buf.writeUtf(issueHint);
        buf.writeVarInt(batchIndex);
        buf.writeVarInt(batchCount);
    }

    public static DockContainerRow read(RegistryFriendlyByteBuf buf) {
        return new DockContainerRow(
                buf.readUUID(),
                buf.readUUID(),
                buf.readInt(),
                buf.readInt(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBlockPos(),
                buf.readBoolean(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBlockPos(),
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public String shortContractId() {
        String value = contractId.toString();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }

    public String shortContainerId() {
        String value = containerId.toString();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }

    public String batchText() {
        return batchCount <= 1 ? "" : batchIndex + "/" + batchCount;
    }
}
