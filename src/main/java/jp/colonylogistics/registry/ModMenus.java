package jp.colonylogistics.registry;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.menu.ContainerDockMenu;
import jp.colonylogistics.menu.FreightBoardMenu;
import jp.colonylogistics.menu.TradeTerminalMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ColonyLogistics.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TradeTerminalMenu>> TRADE_TERMINAL = MENUS.register(
            "trade_terminal",
            () -> IMenuTypeExtension.create(TradeTerminalMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<FreightBoardMenu>> FREIGHT_BOARD = MENUS.register(
            "freight_board",
            () -> IMenuTypeExtension.create(FreightBoardMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerDockMenu>> CONTAINER_DOCK = MENUS.register(
            "container_dock",
            () -> IMenuTypeExtension.create(ContainerDockMenu::new)
    );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    private ModMenus() {}
}
