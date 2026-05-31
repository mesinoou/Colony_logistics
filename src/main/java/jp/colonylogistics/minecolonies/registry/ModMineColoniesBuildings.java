package jp.colonylogistics.minecolonies.registry;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.core.colony.buildings.views.EmptyView;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.minecolonies.building.BuildingContainerDock;
import jp.colonylogistics.minecolonies.building.BuildingLogisticsOffice;
import jp.colonylogistics.minecolonies.building.BuildingTradeTerminal;
import jp.colonylogistics.minecolonies.module.ColonyLogisticsBuildingModule;
import jp.colonylogistics.minecolonies.module.ColonyLogisticsBuildingModuleKind;
import jp.colonylogistics.minecolonies.module.ColonyLogisticsBuildingModuleView;
import jp.colonylogistics.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * MineColonies building registry entries for Colony Logistics.
 *
 * MineColonies exposes its buildings through a custom registry named
 * {@code minecolonies:buildings}. Each building also registers a module tab so
 * Colony Logistics screens are reached from MineColonies' normal hut window.
 */
public final class ModMineColoniesBuildings {
    public static final String LOGISTICS_OFFICE_ID = "logistics_office";
    public static final String CONTAINER_DOCK_ID = "container_dock";
    public static final String TRADE_TERMINAL_ID = "trade_terminal";

    private static final BuildingEntry.ModuleProducer LOGISTICS_OFFICE_UI_MODULE = moduleProducer(
            "logistics_office_ui",
            ColonyLogisticsBuildingModuleKind.LOGISTICS_OFFICE
    );
    private static final BuildingEntry.ModuleProducer CONTAINER_DOCK_UI_MODULE = moduleProducer(
            "container_dock_ui",
            ColonyLogisticsBuildingModuleKind.CONTAINER_DOCK
    );
    private static final BuildingEntry.ModuleProducer TRADE_TERMINAL_UI_MODULE = moduleProducer(
            "trade_terminal_ui",
            ColonyLogisticsBuildingModuleKind.TRADE_TERMINAL
    );

    private static final DeferredRegister<BuildingEntry> BUILDINGS = DeferredRegister.create(
            ResourceLocation.fromNamespaceAndPath("minecolonies", "buildings"),
            ColonyLogistics.MOD_ID
    );

    public static final DeferredHolder<BuildingEntry, BuildingEntry> LOGISTICS_OFFICE = BUILDINGS.register(
            LOGISTICS_OFFICE_ID,
            () -> new BuildingEntry.Builder()
                    .setBuildingBlock(ModBlocks.LOGISTICS_OFFICE.get())
                    .setBuildingProducer(BuildingLogisticsOffice::new)
                    .setBuildingViewProducer(() -> EmptyView::new)
                    .addBuildingModuleProducer(LOGISTICS_OFFICE_UI_MODULE)
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, LOGISTICS_OFFICE_ID))
                    .createBuildingEntry()
    );

    public static final DeferredHolder<BuildingEntry, BuildingEntry> TRADE_TERMINAL = BUILDINGS.register(
            TRADE_TERMINAL_ID,
            () -> new BuildingEntry.Builder()
                    .setBuildingBlock(ModBlocks.TRADE_TERMINAL.get())
                    .setBuildingProducer(BuildingTradeTerminal::new)
                    .setBuildingViewProducer(() -> EmptyView::new)
                    .addBuildingModuleProducer(TRADE_TERMINAL_UI_MODULE)
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, TRADE_TERMINAL_ID))
                    .createBuildingEntry()
    );

    public static final DeferredHolder<BuildingEntry, BuildingEntry> CONTAINER_DOCK = BUILDINGS.register(
            CONTAINER_DOCK_ID,
            () -> new BuildingEntry.Builder()
                    .setBuildingBlock(ModBlocks.CONTAINER_DOCK.get())
                    .setBuildingProducer(BuildingContainerDock::new)
                    .setBuildingViewProducer(() -> EmptyView::new)
                    .addBuildingModuleProducer(CONTAINER_DOCK_UI_MODULE)
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, CONTAINER_DOCK_ID))
                    .createBuildingEntry()
    );

    public static void register(IEventBus bus) {
        BUILDINGS.register(bus);
    }

    private static BuildingEntry.ModuleProducer moduleProducer(final String id, final ColonyLogisticsBuildingModuleKind kind) {
        // MineColonies 1.21.1 keeps ModuleProducer parameters raw in bytecode/source.
        // Give the nested supplier an explicit type so javac does not infer the
        // outer Supplier#get return as Object and reject the inner lambda.
        final Supplier<ColonyLogisticsBuildingModule> serverModuleProducer = ColonyLogisticsBuildingModule::new;
        final Supplier<Supplier<IBuildingModuleView>> viewModuleProducer =
                () -> (Supplier<IBuildingModuleView>) () -> new ColonyLogisticsBuildingModuleView(kind);

        return new BuildingEntry.ModuleProducer(
                ColonyLogistics.MOD_ID + ":" + id,
                serverModuleProducer,
                viewModuleProducer
        );
    }

    private ModMineColoniesBuildings() {}
}
