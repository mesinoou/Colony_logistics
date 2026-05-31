package jp.colonylogistics.menu;

import jp.colonylogistics.contract.LogisticsContract;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;

/**
 * Snapshot row for an accepted container freight job that can be spawned from a Dock.
 */
public record DockContractRow(
        UUID contractId,
        int originColonyId,
        int destinationColonyId,
        String cargoName,
        String containerText,
        int rewardAmount,
        String currencyId,
        BlockPos suggestedCorePos,
        int requiredContainerCount,
        int spawnedContainerCount,
        int deliveredContainerCount
) {
    public static DockContractRow fromContract(LogisticsContract contract, BlockPos suggestedCorePos) {
        String cargoName = contract.freightSpec()
                .flatMap(spec -> spec.cargo().stream().findFirst())
                .map(cargo -> cargo.cargoId().toString())
                .orElse("unknown");
        String containerText = contract.freightSpec()
                .map(spec -> spec.containerRequirement().size().name()
                        + " / " + spec.containerRequirement().weightClass().name())
                .orElse("-");
        return new DockContractRow(
                contract.id(),
                contract.originColonyId(),
                contract.destinationColonyId(),
                cargoName,
                containerText,
                contract.reward().currencyAmount(),
                contract.reward().currencyItemId().toString(),
                suggestedCorePos,
                contract.effectiveContainerCount(),
                contract.spawnedContainerCount(),
                contract.deliveredContainerCount()
        );
    }

    public String progressText() {
        return requiredContainerCount <= 1 ? "" : deliveredContainerCount + "/" + requiredContainerCount + " delivered, " + spawnedContainerCount + "/" + requiredContainerCount + " spawned";
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(contractId);
        buf.writeInt(originColonyId);
        buf.writeInt(destinationColonyId);
        buf.writeUtf(cargoName);
        buf.writeUtf(containerText);
        buf.writeInt(rewardAmount);
        buf.writeUtf(currencyId);
        buf.writeBlockPos(suggestedCorePos);
        buf.writeVarInt(requiredContainerCount);
        buf.writeVarInt(spawnedContainerCount);
        buf.writeVarInt(deliveredContainerCount);
    }

    public static DockContractRow read(RegistryFriendlyByteBuf buf) {
        return new DockContractRow(
                buf.readUUID(),
                buf.readInt(),
                buf.readInt(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readBlockPos(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }
}
