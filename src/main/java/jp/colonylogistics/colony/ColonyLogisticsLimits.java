package jp.colonylogistics.colony;

import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.container.ContainerSize;

public record ColonyLogisticsLimits(
        int maxOpenFreightJobs,
        int maxContainerDocks,
        int maxActiveContainerJobs,
        int maxPlayerTradeContracts,
        ContainerSize maxContainerSize,
        boolean inventoryFreightEnabled,
        boolean containerFreightEnabled
) {
    public static ColonyLogisticsLimits forBuildingLevel(int level) {
        int safeLevel = Math.max(0, Math.min(level, 5));
        ContainerSize maxContainerSize = ColonyLogisticsConfig.levelMaxContainerSize(safeLevel);
        boolean containerFreightEnabled = ColonyLogisticsConfig.levelContainerFreightEnabled(safeLevel)
                && maxContainerSize.isContainer()
                && ColonyLogisticsConfig.levelMaxContainerDocks(safeLevel) > 0;
        return new ColonyLogisticsLimits(
                ColonyLogisticsConfig.levelMaxOpenFreightJobs(safeLevel),
                ColonyLogisticsConfig.levelMaxContainerDocks(safeLevel),
                ColonyLogisticsConfig.levelMaxActiveContainerJobs(safeLevel),
                ColonyLogisticsConfig.levelMaxPlayerTradeContracts(safeLevel),
                maxContainerSize,
                ColonyLogisticsConfig.levelInventoryFreightEnabled(safeLevel),
                containerFreightEnabled
        );
    }
}
