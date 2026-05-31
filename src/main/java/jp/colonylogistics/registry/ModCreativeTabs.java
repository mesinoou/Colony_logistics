package jp.colonylogistics.registry;

import jp.colonylogistics.ColonyLogistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the Colony Logistics creative tab for all player-facing mod items. */
public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ColonyLogistics.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COLONY_LOGISTICS = CREATIVE_TABS.register(
            "colony_logistics",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.colonylogistics"))
                    .icon(() -> new ItemStack(ModItems.LOGISTICS_OFFICE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LOGISTICS_OFFICE.get());
                        output.accept(ModItems.CONTAINER_DOCK.get());
                        output.accept(ModItems.TRADE_TERMINAL.get());
                        output.accept(ModItems.FREIGHT_PARCEL.get());
                    })
                    .build()
    );

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }

    private ModCreativeTabs() {}
}
