package jp.colonylogistics;

import jp.colonylogistics.blueprint.BlueprintPackInstaller;
import jp.colonylogistics.colony.LogisticsResolverRegistry;
import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.event.ModGameEvents;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import jp.colonylogistics.minecolonies.compat.MineColoniesBlockEntityTypeCompat;
import jp.colonylogistics.minecolonies.resolver.MineColoniesBuildingResolver;
import jp.colonylogistics.network.ModNetwork;
import jp.colonylogistics.registry.ModBlockEntities;
import jp.colonylogistics.registry.ModBlocks;
import jp.colonylogistics.registry.ModItems;
import jp.colonylogistics.registry.ModCreativeTabs;
import jp.colonylogistics.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ColonyLogistics.MOD_ID)
public final class ColonyLogistics {
    public static final String MOD_ID = "colonylogistics";

    public ColonyLogistics(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ColonyLogisticsConfig.SPEC);
        BlueprintPackInstaller.installBundledPack();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModNetwork.register(modEventBus);
        ModMineColoniesBuildings.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        LogisticsResolverRegistry.set(MineColoniesBuildingResolver.INSTANCE);
        ModGameEvents.register();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MineColoniesBlockEntityTypeCompat::patchColonyBuildingValidBlocks);
    }
}
