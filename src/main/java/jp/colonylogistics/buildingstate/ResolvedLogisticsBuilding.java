package jp.colonylogistics.buildingstate;

import jp.colonylogistics.colony.LogisticsResolverRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public record ResolvedLogisticsBuilding(
        LogisticsBuildingKey key,
        int colonyId,
        int buildingLevel,
        boolean built,
        boolean usable,
        Direction cargoForward
) {
    public ResolvedLogisticsBuilding {
        if (cargoForward == null) {
            cargoForward = Direction.SOUTH;
        }
    }
    public static ResolvedLogisticsBuilding resolve(ServerLevel level, BlockPos pos) {
        LogisticsBuildingKey key = LogisticsBuildingKey.of(level, pos);
        return LogisticsResolverRegistry.get().resolveBuilding(level, pos)
                .map(resolved -> new ResolvedLogisticsBuilding(
                        key,
                        resolved.colonyId(),
                        resolved.buildingLevel(),
                        resolved.built(),
                        resolved.colonyId() >= 0 && resolved.built() && resolved.buildingLevel() > 0,
                        resolved.cargoForward()
                ))
                .orElseGet(() -> new ResolvedLogisticsBuilding(key, -1, 0, false, false, Direction.SOUTH));
    }
}
