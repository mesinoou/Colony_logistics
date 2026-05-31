package jp.colonylogistics.minecolonies.resolver;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.blocks.AbstractBlockHut;
import jp.colonylogistics.colony.MineColoniesLogisticsResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

/**
 * Concrete resolver backed by MineColonies 1.21.1 APIs.
 */
public final class MineColoniesBuildingResolver implements MineColoniesLogisticsResolver {
    public static final MineColoniesBuildingResolver INSTANCE = new MineColoniesBuildingResolver();

    private MineColoniesBuildingResolver() {}

    @Override
    public Optional<ResolvedColonyBuilding> resolveBuilding(ServerLevel level, BlockPos pos) {
        IBuilding building = IColonyManager.getInstance().getBuilding(level, pos);
        if (building == null) {
            IColony colonyAtPos = IColonyManager.getInstance().getColonyByPosFromWorld(level, pos);
            if (colonyAtPos == null) {
                return Optional.empty();
            }
            return Optional.of(new ResolvedColonyBuilding(
                    colonyAtPos.getID(),
                    pos.immutable(),
                    0,
                    false,
                    "unresolved",
                    resolveCargoForward(level, null, pos)
            ));
        }

        IColony colony = building.getColony();
        String buildingId = building.getBuildingType() == null
                ? "unknown"
                : building.getBuildingType().getRegistryName().toString();

        return Optional.of(new ResolvedColonyBuilding(
                colony.getID(),
                building.getPosition(),
                building.getBuildingLevel(),
                building.isBuilt(),
                buildingId,
                resolveCargoForward(level, building, building.getPosition())
        ));
    }

    /**
     * Resolve the world direction of the blueprint-local +Z cargo lane.
     *
     * <p>Phase 17.8.3 used MineColonies' cached rotation as the primary signal.
     * In practice that can be -1 while the hut block itself already has the
     * Build Tool-rotated FACING state, which made all candidates rotate to the
     * wrong side. Prefer the actual hut block state: our blueprints keep the
     * approach/control face on local north, so the cargo apron is behind the hut
     * block, i.e. opposite the hut FACING direction. The reflective MineColonies
     * rotation path remains as a fallback for edge cases where the state cannot
     * be read.</p>
     */
    private static Direction resolveCargoForward(ServerLevel level, IBuilding building, BlockPos hutPos) {
        Optional<Direction> fromState = resolveCargoForwardFromBlockState(level, hutPos);
        if (fromState.isPresent()) {
            return fromState.get();
        }

        Direction fromRotation = resolveCargoForwardFromBuildingRotation(building);
        return fromRotation == null ? Direction.SOUTH : fromRotation;
    }

    private static Optional<Direction> resolveCargoForwardFromBlockState(ServerLevel level, BlockPos hutPos) {
        if (level == null || hutPos == null || !level.isLoaded(hutPos)) {
            return Optional.empty();
        }
        try {
            BlockState state = level.getBlockState(hutPos);
            if (state.hasProperty(AbstractBlockHut.FACING)) {
                Direction hutFacing = state.getValue(AbstractBlockHut.FACING);
                if (hutFacing.getAxis().isHorizontal()) {
                    return Optional.of(hutFacing.getOpposite());
                }
            }
        } catch (RuntimeException ignored) {
            // Fall through to the MineColonies building rotation fallback.
        }
        return Optional.empty();
    }

    private static Direction resolveCargoForwardFromBuildingRotation(IBuilding building) {
        if (building == null) {
            return null;
        }
        Object rotationMirror = invokeNoArg(building, "getRotationMirror", "rotationMirror");
        Object rotation = rotationMirror == null
                ? invokeNoArg(building, "getRotation", "rotation")
                : invokeNoArg(rotationMirror, "getRotation", "rotation");
        int steps = rotationSteps(rotation != null ? rotation : rotationMirror);
        if (steps < 0) {
            return null;
        }
        return switch (Math.floorMod(steps, 4)) {
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.EAST;
            default -> Direction.SOUTH;
        };
    }

    private static Object invokeNoArg(Object target, String... methodNames) {
        if (target == null) {
            return null;
        }
        for (String methodName : methodNames) {
            Method method = findNoArgMethod(target.getClass(), methodName);
            if (method == null) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Try the next known method name.
            }
        }
        return null;
    }

    private static Method findNoArgMethod(Class<?> type, String methodName) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                if (method.getParameterCount() == 0) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the superclass.
            }
            current = current.getSuperclass();
        }
        try {
            Method method = type.getMethod(methodName);
            return method.getParameterCount() == 0 ? method : null;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static int rotationSteps(Object rotationLike) {
        if (rotationLike instanceof Number number) {
            int value = number.intValue();
            return value < 0 ? -1 : Math.floorMod(value, 4);
        }
        if (rotationLike == null) {
            return -1;
        }
        String text = rotationLike.toString().toUpperCase(Locale.ROOT);
        if (text.contains("COUNTERCLOCKWISE_90") || text.contains("COUNTER_CLOCKWISE_90") || text.contains("CCW_90")) {
            return 3;
        }
        if (text.contains("CLOCKWISE_180") || text.contains("CW_180") || text.contains("ROT_180")) {
            return 2;
        }
        if (text.contains("CLOCKWISE_90") || text.contains("CW_90") || text.contains("ROT_90")) {
            return 1;
        }
        return 0;
    }
}
