package jp.colonylogistics.event;

import jp.colonylogistics.command.LogisticsCommands;
import jp.colonylogistics.container.FreightContainerCoreBlock;
import jp.colonylogistics.container.FreightContainerPartBlock;
import jp.colonylogistics.market.FreightMarketTicker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class ModGameEvents {
    private static final FreightMarketTicker FREIGHT_MARKET_TICKER = new FreightMarketTicker();

    public static void register() {
        NeoForge.EVENT_BUS.register(ModGameEvents.class);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LogisticsCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        FREIGHT_MARKET_TICKER.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof FreightContainerCoreBlock
                || event.getState().getBlock() instanceof FreightContainerPartBlock) {
            if (!event.getPlayer().hasPermissions(2)) {
                event.setCanceled(true);
            }
        }
    }

    private ModGameEvents() {}
}
