package jp.colonylogistics.minecolonies.building;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.DefaultBuildingInstance;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import jp.colonylogistics.minecolonies.sync.ColonyLogisticsMineColoniesSync;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/** MineColonies building instance for the player trade terminal. */
public class BuildingTradeTerminal extends DefaultBuildingInstance {
    private int logisticsSyncTick;

    public BuildingTradeTerminal(IColony colony, BlockPos pos) {
        super(colony, pos, ModMineColoniesBuildings.TRADE_TERMINAL_ID, 5);
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        ColonyLogisticsMineColoniesSync.syncTerminal(this);
    }

    @Override
    public void onColonyTick(IColony colony) {
        super.onColonyTick(colony);
        if (++logisticsSyncTick % 100 == 0) {
            ColonyLogisticsMineColoniesSync.syncTerminal(this);
        }
    }

    @Override
    public void onUpgradeComplete(@Nullable Blueprint blueprint, int newLevel) {
        super.onUpgradeComplete(blueprint, newLevel);
        ColonyLogisticsMineColoniesSync.syncTerminal(this);
    }

    @Override
    public void onDestroyed() {
        ColonyLogisticsMineColoniesSync.removeTerminal(this);
        super.onDestroyed();
    }
}
