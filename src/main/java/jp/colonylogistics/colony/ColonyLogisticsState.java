package jp.colonylogistics.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class ColonyLogisticsState {
    private final int colonyId;
    private BlockPos logisticsOfficePos = BlockPos.ZERO;
    private int logisticsOfficeLevel;
    private final List<BlockPos> dockPositions = new ArrayList<>();
    private int activeGeneratedJobs;
    private int activeContainerJobs;
    private int openPlayerTradeContracts;

    public ColonyLogisticsState(int colonyId) {
        this.colonyId = colonyId;
    }

    public int colonyId() { return colonyId; }
    public BlockPos logisticsOfficePos() { return logisticsOfficePos; }
    public int logisticsOfficeLevel() { return logisticsOfficeLevel; }
    public List<BlockPos> dockPositions() { return List.copyOf(dockPositions); }

    public BlockPos primaryDockPos() {
        return dockPositions.isEmpty() ? logisticsOfficePos : dockPositions.get(0);
    }

    public boolean hasDock() {
        return !dockPositions.isEmpty();
    }
    public int activeGeneratedJobs() { return activeGeneratedJobs; }
    public int activeContainerJobs() { return activeContainerJobs; }
    public int openPlayerTradeContracts() { return openPlayerTradeContracts; }

    public ColonyLogisticsLimits limits() {
        return ColonyLogisticsLimits.forBuildingLevel(logisticsOfficeLevel);
    }

    public void setLogisticsOffice(BlockPos pos, int level) {
        this.logisticsOfficePos = pos.immutable();
        this.logisticsOfficeLevel = Math.max(0, Math.min(5, level));
    }

    public boolean registerDock(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        if (dockPositions.contains(immutable)) return true;
        if (dockPositions.size() >= limits().maxContainerDocks()) return false;
        dockPositions.add(immutable);
        return true;
    }

    public void unregisterDock(BlockPos pos) {
        dockPositions.remove(pos);
    }

    public void clearDocks() {
        dockPositions.clear();
    }

    public boolean canStartContainerJob() {
        return limits().containerFreightEnabled()
                && activeContainerJobs < limits().maxActiveContainerJobs();
    }

    public void incrementActiveContainerJobs() { activeContainerJobs++; }
    public void decrementActiveContainerJobs() { activeContainerJobs = Math.max(0, activeContainerJobs - 1); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ColonyId", colonyId);
        tag.putLong("LogisticsOfficePos", logisticsOfficePos.asLong());
        tag.putInt("LogisticsOfficeLevel", logisticsOfficeLevel);
        tag.putInt("ActiveGeneratedJobs", activeGeneratedJobs);
        tag.putInt("ActiveContainerJobs", activeContainerJobs);
        tag.putInt("OpenPlayerTradeContracts", openPlayerTradeContracts);

        ListTag docks = new ListTag();
        for (BlockPos dock : dockPositions) {
            CompoundTag dockTag = new CompoundTag();
            dockTag.putLong("Pos", dock.asLong());
            docks.add(dockTag);
        }
        tag.put("Docks", docks);
        return tag;
    }

    public static ColonyLogisticsState load(CompoundTag tag) {
        ColonyLogisticsState state = new ColonyLogisticsState(tag.getInt("ColonyId"));
        state.logisticsOfficePos = BlockPos.of(tag.getLong("LogisticsOfficePos"));
        state.logisticsOfficeLevel = tag.getInt("LogisticsOfficeLevel");
        state.activeGeneratedJobs = tag.getInt("ActiveGeneratedJobs");
        state.activeContainerJobs = tag.getInt("ActiveContainerJobs");
        state.openPlayerTradeContracts = tag.getInt("OpenPlayerTradeContracts");
        ListTag docks = tag.getList("Docks", Tag.TAG_COMPOUND);
        for (int i = 0; i < docks.size(); i++) {
            state.dockPositions.add(BlockPos.of(docks.getCompound(i).getLong("Pos")));
        }
        return state;
    }
}
