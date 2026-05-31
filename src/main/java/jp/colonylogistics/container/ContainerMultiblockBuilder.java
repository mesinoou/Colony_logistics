package jp.colonylogistics.container;

import jp.colonylogistics.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class ContainerMultiblockBuilder {
    public boolean hasSpace(ServerLevel level, BlockPos corePos, ContainerSize size) {
        return hasSpace(level, corePos, size, Direction.SOUTH);
    }

    public boolean hasSpace(ServerLevel level, BlockPos corePos, ContainerSize size, Direction facing) {
        for (BlockPos pos : positions(corePos, size, facing)) {
            if (!level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public boolean place(ServerLevel level, BlockPos corePos, ContainerManifest manifest) {
        ContainerSize size = manifest.size();
        ContainerWeightClass weight = manifest.weightClass();

        BlockState part = ModBlocks.FREIGHT_CONTAINER_PART.get().defaultBlockState()
                .setValue(FreightContainerPartBlock.WEIGHT_CLASS, weight);
        BlockState core = ModBlocks.FREIGHT_CONTAINER_CORE.get().defaultBlockState()
                .setValue(FreightContainerCoreBlock.WEIGHT_CLASS, weight);

        boolean allPlaced = true;
        for (BlockPos pos : positions(corePos, size, manifest.facing())) {
            boolean placed = level.setBlock(pos, pos.equals(corePos) ? core : part, 3);
            allPlaced = allPlaced && placed;
        }
        if (level.getBlockEntity(corePos) instanceof FreightContainerCoreBlockEntity be) {
            be.setManifest(manifest);
            return allPlaced && level.getBlockState(corePos).is(ModBlocks.FREIGHT_CONTAINER_CORE.get());
        }
        return false;
    }

    public boolean remove(ServerLevel level, BlockPos corePos, ContainerManifest manifest) {
        boolean removedAny = false;
        for (BlockPos pos : positions(corePos, manifest.size(), manifest.facing())) {
            if (level.getBlockState(pos).is(ModBlocks.FREIGHT_CONTAINER_CORE.get())
                    || level.getBlockState(pos).is(ModBlocks.FREIGHT_CONTAINER_PART.get())) {
                level.removeBlock(pos, false);
                removedAny = true;
            }
        }
        return removedAny;
    }

    public java.util.Optional<FreightContainerCoreBlockEntity> findCoreNear(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (level.getBlockEntity(pos) instanceof FreightContainerCoreBlockEntity be) {
                return java.util.Optional.of(be);
            }
        }
        return java.util.Optional.empty();
    }

    private Iterable<BlockPos> positions(BlockPos corePos, ContainerSize size, Direction facing) {
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();
        int hx = size.width() / 2;
        int hy = size.height() / 2;
        int hz = size.depth() / 2;
        Direction forward = horizontal(facing);
        Direction right = forward.getCounterClockWise();
        for (int x = -hx; x <= hx; x++) {
            for (int y = -hy; y <= hy; y++) {
                for (int z = -hz; z <= hz; z++) {
                    /*
                     * The public footprint is depth x width x height = 3 x 7 x 3.
                     * For the production Dock mock-up, the 7-wide axis must run
                     * along the Dock cargo lane while the 3-deep axis spans across
                     * the apron. Therefore width (x loop) maps to forward and
                     * depth (z loop) maps to right.
                     */
                    positions.add(corePos.offset(
                            forward.getStepX() * x + right.getStepX() * z,
                            y,
                            forward.getStepZ() * x + right.getStepZ() * z
                    ));
                }
            }
        }
        return positions;
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
}
