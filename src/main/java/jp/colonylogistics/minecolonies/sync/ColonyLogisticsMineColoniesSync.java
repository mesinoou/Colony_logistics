package jp.colonylogistics.minecolonies.sync;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import jp.colonylogistics.buildingstate.LogisticsBuildingKey;
import jp.colonylogistics.colony.ColonyLogisticsState;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Mirrors MineColonies building state into the mod's world-level logistics data.
 * MineColonies remains the source of truth; Colony Logistics stores per-building
 * runtime state in SavedData keyed by dimension + hut position.
 */
public final class ColonyLogisticsMineColoniesSync {
    public static void syncOffice(IBuilding building) {
        IColony colony = building.getColony();
        Level level = colony.getWorld();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(serverLevel);
        ColonyLogisticsState state = data.colonyState(colony.getID());
        int effectiveLevel = building.isBuilt() ? building.getBuildingLevel() : 0;
        state.setLogisticsOffice(building.getPosition(), effectiveLevel);
        data.setDirty();
    }

    public static void removeOffice(IBuilding building) {
        IColony colony = building.getColony();
        Level level = colony.getWorld();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(serverLevel);
        ColonyLogisticsState state = data.colonyState(colony.getID());
        if (state.logisticsOfficePos().equals(building.getPosition())) {
            state.setLogisticsOffice(BlockPos.ZERO, 0);
            state.clearDocks();
            data.setDirty();
        }
    }

    public static void syncDock(IBuilding building) {
        IColony colony = building.getColony();
        Level level = colony.getWorld();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(serverLevel);
        ColonyLogisticsState state = data.colonyState(colony.getID());
        boolean officeBuilt = state.logisticsOfficeLevel() > 0;
        boolean registered = false;
        if (building.isBuilt() && officeBuilt) {
            registered = state.registerDock(building.getPosition());
            data.dockState(LogisticsBuildingKey.of(serverLevel, building.getPosition()));
        }

        if (registered) {
            data.setDirty();
        }
    }

    public static void removeDock(IBuilding building) {
        IColony colony = building.getColony();
        Level level = colony.getWorld();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(serverLevel);
        ColonyLogisticsState state = data.colonyState(colony.getID());
        state.unregisterDock(building.getPosition());
        data.setDirty();
    }

    public static void syncTerminal(IBuilding building) {
        IColony colony = building.getColony();
        Level level = colony.getWorld();
        if (level instanceof ServerLevel serverLevel) {
            LogisticsMarketSavedData.get(serverLevel).terminalState(LogisticsBuildingKey.of(serverLevel, building.getPosition()));
        }
    }

    public static void removeTerminal(IBuilding building) {
        // Terminal runtime state is retained for now to avoid losing escrow/input
        // data accidentally during blueprint repair. A retention policy can prune it later.
    }

    private ColonyLogisticsMineColoniesSync() {}
}
