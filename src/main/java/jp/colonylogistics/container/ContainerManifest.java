package jp.colonylogistics.container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.UUID;

public record ContainerManifest(
        UUID containerId,
        UUID contractId,
        int originColonyId,
        int destinationColonyId,
        UUID assignedPlayer,
        BlockPos originDockPos,
        BlockPos destinationDockPos,
        ContainerSize size,
        ContainerWeightClass weightClass,
        int gameplayWeight,
        boolean sealed,
        long createdGameTime,
        int batchIndex,
        int batchCount,
        Direction facing
) {
    public ContainerManifest {
        batchCount = Math.max(1, batchCount);
        batchIndex = Math.max(1, Math.min(batchIndex, batchCount));
        facing = horizontal(facing);
    }

    public ContainerManifest(
            UUID containerId,
            UUID contractId,
            int originColonyId,
            int destinationColonyId,
            UUID assignedPlayer,
            BlockPos originDockPos,
            BlockPos destinationDockPos,
            ContainerSize size,
            ContainerWeightClass weightClass,
            int gameplayWeight,
            boolean sealed,
            long createdGameTime,
            int batchIndex,
            int batchCount
    ) {
        this(containerId, contractId, originColonyId, destinationColonyId, assignedPlayer,
                originDockPos, destinationDockPos, size, weightClass, gameplayWeight, sealed,
                createdGameTime, batchIndex, batchCount, Direction.SOUTH);
    }

    private static Direction horizontal(Direction direction) {
        if (direction == null) {
            return Direction.SOUTH;
        }
        return switch (direction) {
            case NORTH, SOUTH, EAST, WEST -> direction;
            default -> Direction.SOUTH;
        };
    }

    public ContainerManifest withSealed(boolean sealed) {
        return new ContainerManifest(
                containerId, contractId, originColonyId, destinationColonyId, assignedPlayer,
                originDockPos, destinationDockPos, size, weightClass, gameplayWeight, sealed, createdGameTime,
                batchIndex, batchCount, facing
        );
    }

    public String batchText() {
        return batchCount <= 1 ? "" : batchIndex + "/" + batchCount;
    }
}
