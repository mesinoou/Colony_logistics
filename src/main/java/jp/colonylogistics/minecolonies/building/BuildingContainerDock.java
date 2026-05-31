package jp.colonylogistics.minecolonies.building;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.DefaultBuildingInstance;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import jp.colonylogistics.minecolonies.sync.ColonyLogisticsMineColoniesSync;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * MineColonies building instance for a physical Container Dock.
 *
 * The dock becomes usable only if its colony has a built Logistics Office whose
 * level allows another dock. The functional dock BlockEntity stores the cached
 * active/inactive state used by container spawning and delivery.
 */
public class BuildingContainerDock extends DefaultBuildingInstance {
    private int logisticsSyncTick;

    public BuildingContainerDock(IColony colony, BlockPos pos) {
        super(colony, pos, ModMineColoniesBuildings.CONTAINER_DOCK_ID, 5);
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        ColonyLogisticsMineColoniesSync.syncDock(this);
    }

    @Override
    public void onColonyTick(IColony colony) {
        super.onColonyTick(colony);
        if (++logisticsSyncTick % 100 == 0) {
            ColonyLogisticsMineColoniesSync.syncDock(this);
        }
    }

    @Override
    public void onUpgradeComplete(@Nullable Blueprint blueprint, int newLevel) {
        super.onUpgradeComplete(blueprint, newLevel);
        ColonyLogisticsMineColoniesSync.syncDock(this);
    }

    @Override
    public void onDestroyed() {
        ColonyLogisticsMineColoniesSync.removeDock(this);
        super.onDestroyed();
    }
}
