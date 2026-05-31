package jp.colonylogistics.registry;

import jp.colonylogistics.ColonyLogistics;
import com.minecolonies.api.items.ItemBlockHut;
import jp.colonylogistics.item.FreightParcelItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ColonyLogistics.MOD_ID);

    public static final DeferredHolder<Item, FreightParcelItem> FREIGHT_PARCEL = ITEMS.register("freight_parcel",
            () -> new FreightParcelItem(new Item.Properties()));

    /** MineColonies hut item. Must be placed through the Build Tool and a matching blueprint. */
    public static final DeferredHolder<Item, ItemBlockHut> LOGISTICS_OFFICE = ITEMS.register("logistics_office",
            () -> new ItemBlockHut(ModBlocks.LOGISTICS_OFFICE.get(), new Item.Properties()));

    /** MineColonies hut item. Must be placed through the Build Tool and a matching blueprint. */
    public static final DeferredHolder<Item, ItemBlockHut> TRADE_TERMINAL = ITEMS.register("trade_terminal",
            () -> new ItemBlockHut(ModBlocks.TRADE_TERMINAL.get(), new Item.Properties()));

    /** MineColonies hut item. Must be placed through the Build Tool and a matching blueprint. */
    public static final DeferredHolder<Item, ItemBlockHut> CONTAINER_DOCK = ITEMS.register("container_dock",
            () -> new ItemBlockHut(ModBlocks.CONTAINER_DOCK.get(), new Item.Properties()));


    /** Admin/testing only: do not add to creative tabs or recipes. */
    public static final DeferredHolder<Item, BlockItem> FREIGHT_CONTAINER_CORE = ITEMS.register("freight_container_core",
            () -> new BlockItem(ModBlocks.FREIGHT_CONTAINER_CORE.get(), new Item.Properties()));

    /** Admin/testing only: do not add to creative tabs or recipes. */
    public static final DeferredHolder<Item, BlockItem> FREIGHT_CONTAINER_PART = ITEMS.register("freight_container_part",
            () -> new BlockItem(ModBlocks.FREIGHT_CONTAINER_PART.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
