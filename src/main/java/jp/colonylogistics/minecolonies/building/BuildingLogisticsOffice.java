package jp.colonylogistics.minecolonies.building;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.DefaultBuildingInstance;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import jp.colonylogistics.minecolonies.sync.ColonyLogisticsMineColoniesSync;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * MineColonies building instance for the Logistics Office.
 *
 * It has no worker. Its role is to unlock logistics features for a colony and
 * expose its built level to the contract/freight systems.
 */
public class BuildingLogisticsOffice extends DefaultBuildingInstance {
    private int logisticsSyncTick;

    public BuildingLogisticsOffice(IColony colony, BlockPos pos) {
        super(colony, pos, ModMineColoniesBuildings.LOGISTICS_OFFICE_ID, 5);
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        ColonyLogisticsMineColoniesSync.syncOffice(this);
    }

    @Override
    public void onColonyTick(IColony colony) {
        super.onColonyTick(colony);
        if (++logisticsSyncTick % 100 == 0) {
            ColonyLogisticsMineColoniesSync.syncOffice(this);
        }
    }

    @Override
    public void onUpgradeComplete(@Nullable Blueprint blueprint, int newLevel) {
        super.onUpgradeComplete(blueprint, newLevel);
        ColonyLogisticsMineColoniesSync.syncOffice(this);
    }

    @Override
    public void onDestroyed() {
        ColonyLogisticsMineColoniesSync.removeOffice(this);
        super.onDestroyed();
    }
}
