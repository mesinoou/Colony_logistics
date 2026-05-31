package jp.colonylogistics.market;

import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.service.FreightMarketService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

/** Server-side market top-up loop used for test-play builds. */
public final class FreightMarketTicker {
    private final FreightMarketService freightMarketService = new FreightMarketService();

    public void tick(MinecraftServer server) {
        if (!ColonyLogisticsConfig.autoGenerateMarketJobs()) {
            return;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(overworld);
        long now = overworld.getGameTime();

        if (now >= data.nextMarketGenerationGameTime()) {
            int generated = 0;
            if (ColonyLogisticsConfig.autoGenerateInventoryJobs()) {
                generated += freightMarketService.ensureMinimumInventoryJobs(overworld);
            }
            if (ColonyLogisticsConfig.autoGenerateContainerJobs()) {
                generated += freightMarketService.ensureMinimumContainerJobs(overworld);
            }
            data.setNextMarketGenerationGameTime(now + ColonyLogisticsConfig.marketGenerationIntervalTicks());
            if (generated > 0) {
                data.setDirty();
            }
        }

        if (now >= data.nextMarketPurgeGameTime()) {
            data.purgeFinishedContracts();
            data.setNextMarketPurgeGameTime(now + ColonyLogisticsConfig.marketPurgeIntervalTicks());
        }
    }
}
