package jp.colonylogistics.container;

import jp.colonylogistics.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        Direction facing = horizontal(manifest.facing());

        BlockState part = ModBlocks.FREIGHT_CONTAINER_PART.get().defaultBlockState()
                .setValue(FreightContainerPartBlock.WEIGHT_CLASS, weight);
        BlockState core = ModBlocks.FREIGHT_CONTAINER_CORE.get().defaultBlockState()
                .setValue(FreightContainerCoreBlock.WEIGHT_CLASS, weight);

        boolean allPlaced = true;
        for (BlockPos pos : positions(corePos, size, facing)) {
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
        return removeWithResult(level, corePos, manifest).removedAny();
    }

    public RemovalResult removeWithResult(ServerLevel level, BlockPos corePos, ContainerManifest manifest) {
        Direction manifestFacing = horizontal(manifest.facing());
        List<RemovalPlan> plans = scanRemovalPlans(level, corePos, manifest.size());
        RemovalPlan selected = selectBestRemovalPlan(plans, manifestFacing)
                .orElseGet(() -> scanRemovalPlan(level, corePos, manifest.size(), manifestFacing));
        int manifestFacingCount = containerBlockCountForFacing(plans, manifestFacing);
        int rotatedFacingCount = containerBlockCountForFacing(plans, manifestFacing.getClockWise());
        Set<BlockPos> connected = collectConnectedContainerBlocks(level, corePos, manifest.size(), selected.positions().size() + 1);
        boolean useConnected = !connected.isEmpty()
                && connected.size() <= selected.positions().size()
                && connected.size() >= selected.containerBlockCount();
        List<BlockPos> removalPositions = useConnected ? new ArrayList<>(connected) : selected.positions();
        int beforeCount = useConnected ? connected.size() : selected.containerBlockCount();
        String strategy = useConnected ? "connected" : "oriented";

        int removedCount = 0;
        for (BlockPos pos : removalPositions) {
            if (isContainerBlock(level.getBlockState(pos))) {
                level.removeBlock(pos, false);
                if (!isContainerBlock(level.getBlockState(pos))) {
                    removedCount++;
                }
            }
        }
        List<BlockPos> remaining = removalPositions.stream()
                .filter(pos -> isContainerBlock(level.getBlockState(pos)))
                .map(BlockPos::immutable)
                .toList();
        return new RemovalResult(
                removedCount > 0,
                removedCount,
                selected.positions().size(),
                beforeCount,
                remaining.size(),
                selected.facing(),
                manifestFacing,
                manifestFacingCount,
                rotatedFacingCount,
                connected.size(),
                strategy,
                remaining
        );
    }

    public Optional<FreightContainerCoreBlockEntity> findCoreForContainerBlock(ServerLevel level, BlockPos containerPos) {
        if (level.getBlockEntity(containerPos) instanceof FreightContainerCoreBlockEntity core) {
            return Optional.of(core);
        }
        if (!isContainerBlock(level.getBlockState(containerPos))) {
            return Optional.empty();
        }

        int radius = ContainerSize.realSizes().stream()
                .mapToInt(size -> Math.max(size.width(), Math.max(size.height(), size.depth())))
                .max()
                .orElse(7);
        return BlockPos.betweenClosedStream(
                        containerPos.offset(-radius, -radius, -radius),
                        containerPos.offset(radius, radius, radius)
                )
                .map(BlockPos::immutable)
                .filter(pos -> level.getBlockEntity(pos) instanceof FreightContainerCoreBlockEntity)
                .map(pos -> (FreightContainerCoreBlockEntity) level.getBlockEntity(pos))
                .filter(core -> core.manifest().isPresent())
                .filter(core -> containsContainerBlock(level, core, containerPos))
                .min(Comparator.comparingInt(core -> core.getBlockPos().distManhattan(containerPos)));
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

    private RemovalPlan selectedRemovalPlan(ServerLevel level, BlockPos corePos, ContainerManifest manifest) {
        Direction manifestFacing = horizontal(manifest.facing());
        List<RemovalPlan> plans = scanRemovalPlans(level, corePos, manifest.size());
        return selectBestRemovalPlan(plans, manifestFacing)
                .orElseGet(() -> scanRemovalPlan(level, corePos, manifest.size(), manifestFacing));
    }

    private boolean containsContainerBlock(ServerLevel level, FreightContainerCoreBlockEntity core, BlockPos containerPos) {
        ContainerManifest manifest = core.manifest().orElse(null);
        if (manifest == null) {
            return false;
        }
        Set<BlockPos> connected = collectConnectedContainerBlocks(level, core.getBlockPos(), manifest.size(), manifest.size().volume() + 1);
        if (connected.contains(containerPos)) {
            return true;
        }
        return selectedRemovalPlan(level, core.getBlockPos(), manifest).positions().contains(containerPos);
    }

    private List<RemovalPlan> scanRemovalPlans(ServerLevel level, BlockPos corePos, ContainerSize size) {
        List<RemovalPlan> plans = new ArrayList<>();
        for (Direction facing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            plans.add(scanRemovalPlan(level, corePos, size, facing));
        }
        return plans;
    }

    private RemovalPlan scanRemovalPlan(ServerLevel level, BlockPos corePos, ContainerSize size, Direction facing) {
        List<BlockPos> positions = positions(corePos, size, facing);
        int containerBlockCount = (int) positions.stream()
                .filter(pos -> isContainerBlock(level.getBlockState(pos)))
                .count();
        return new RemovalPlan(horizontal(facing), positions, containerBlockCount);
    }

    private int containerBlockCountForFacing(List<RemovalPlan> plans, Direction facing) {
        Direction target = horizontal(facing);
        return plans.stream()
                .filter(plan -> plan.facing() == target)
                .findFirst()
                .map(RemovalPlan::containerBlockCount)
                .orElse(0);
    }

    private Optional<RemovalPlan> selectBestRemovalPlan(List<RemovalPlan> plans, Direction manifestFacing) {
        return plans.stream()
                .max(Comparator
                        .comparingInt(RemovalPlan::containerBlockCount)
                        .thenComparingInt(plan -> plan.facing() == horizontal(manifestFacing) ? 1 : 0));
    }

    private Set<BlockPos> collectConnectedContainerBlocks(ServerLevel level, BlockPos corePos, ContainerSize size, int maxBlocks) {
        Set<BlockPos> connected = new LinkedHashSet<>();
        if (!isContainerBlock(level.getBlockState(corePos))) {
            return connected;
        }

        int horizontalRadius = Math.max(size.width(), size.depth());
        int verticalRadius = Math.max(1, size.height());
        BlockPos min = corePos.offset(-horizontalRadius, -verticalRadius, -horizontalRadius);
        BlockPos max = corePos.offset(horizontalRadius, verticalRadius, horizontalRadius);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        connected.add(corePos.immutable());
        queue.add(corePos.immutable());

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction).immutable();
                if (connected.contains(next) || !inside(next, min, max) || !isContainerBlock(level.getBlockState(next))) {
                    continue;
                }
                connected.add(next);
                if (connected.size() > maxBlocks) {
                    return connected;
                }
                queue.add(next);
            }
        }
        return connected;
    }

    private List<BlockPos> positions(BlockPos corePos, ContainerSize size, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
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

    private static boolean isContainerBlock(BlockState state) {
        return state.is(ModBlocks.FREIGHT_CONTAINER_CORE.get())
                || state.is(ModBlocks.FREIGHT_CONTAINER_PART.get());
    }

    private static boolean inside(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
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

    private record RemovalPlan(Direction facing, List<BlockPos> positions, int containerBlockCount) {
    }

    public record RemovalResult(
            boolean removedAny,
            int removedCount,
            int expectedCount,
            int selectedBeforeCount,
            int remainingCount,
            Direction selectedFacing,
            Direction manifestFacing,
            int manifestFacingCount,
            int rotatedFacingCount,
            int connectedCount,
            String strategy,
            List<BlockPos> remainingPositions
    ) {
        public String debugSummary() {
            return "removal=removed " + removedCount + "/" + expectedCount
                    + " strategy=" + strategy
                    + " selectedBefore=" + selectedBeforeCount
                    + " connected=" + connectedCount
                    + " remaining=" + remainingCount
                    + " selectedFacing=" + selectedFacing
                    + " manifestFacing=" + manifestFacing
                    + " manifestFacingCount=" + manifestFacingCount
                    + " rotatedFacingCount=" + rotatedFacingCount
                    + " leftovers=" + remainingPositions.stream()
                    .limit(8)
                    .map(BlockPos::toShortString)
                    .toList();
        }
    }
}
