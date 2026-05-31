package jp.colonylogistics.registry;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.container.FreightContainerCoreBlock;
import jp.colonylogistics.container.FreightContainerPartBlock;
import jp.colonylogistics.dock.ContainerDockBlock;
import jp.colonylogistics.dock.LogisticsOfficeBlock;
import jp.colonylogistics.terminal.TradeTerminalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ColonyLogistics.MOD_ID);

    public static final DeferredBlock<LogisticsOfficeBlock> LOGISTICS_OFFICE = BLOCKS.registerBlock(
            "logistics_office",
            LogisticsOfficeBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD)
    );

    public static final DeferredBlock<ContainerDockBlock> CONTAINER_DOCK = BLOCKS.registerBlock(
            "container_dock",
            ContainerDockBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
    );

    public static final DeferredBlock<TradeTerminalBlock> TRADE_TERMINAL = BLOCKS.registerBlock(
            "trade_terminal",
            TradeTerminalBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD)
    );


    public static final DeferredBlock<FreightContainerCoreBlock> FREIGHT_CONTAINER_CORE = BLOCKS.registerBlock(
            "freight_container_core",
            FreightContainerCoreBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
    );

    public static final DeferredBlock<FreightContainerPartBlock> FREIGHT_CONTAINER_PART = BLOCKS.registerBlock(
            "freight_container_part",
            FreightContainerPartBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
    );

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private ModBlocks() {}
}
