package jp.colonylogistics.client;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.client.screen.ContainerDockScreen;
import jp.colonylogistics.client.screen.FreightBoardScreen;
import jp.colonylogistics.client.screen.TradeTerminalScreen;
import jp.colonylogistics.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ColonyLogistics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TRADE_TERMINAL.get(), TradeTerminalScreen::new);
        event.register(ModMenus.FREIGHT_BOARD.get(), FreightBoardScreen::new);
        event.register(ModMenus.CONTAINER_DOCK.get(), ContainerDockScreen::new);
    }

    private ClientModEvents() {}
}
