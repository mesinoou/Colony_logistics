package jp.colonylogistics.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Integration boundary for MineColonies.
 *
 * Keep all direct MineColonies API calls behind this interface so the rest of
 * the mod does not depend on unstable or version-specific building internals.
 */
public interface MineColoniesLogisticsResolver {
    Optional<ResolvedColonyBuilding> resolveBuilding(ServerLevel level, BlockPos pos);

    record ResolvedColonyBuilding(
            int colonyId,
            BlockPos buildingPos,
            int buildingLevel,
            boolean built,
            String buildingId,
            Direction cargoForward
    ) {
        public ResolvedColonyBuilding {
            if (cargoForward == null) {
                cargoForward = Direction.SOUTH;
            }
        }
    }
}
