package jp.colonylogistics.minecolonies.module;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.minecolonies.client.ColonyLogisticsMineColoniesTabWindow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Client-side MineColonies module view for Colony Logistics tabs. */
public class ColonyLogisticsBuildingModuleView implements IBuildingModuleView {
    private final ColonyLogisticsBuildingModuleKind kind;
    private IBuildingView buildingView;
    private IColonyView colonyView;
    private BuildingEntry.ModuleProducer producer;

    public ColonyLogisticsBuildingModuleView(final ColonyLogisticsBuildingModuleKind kind) {
        this.kind = kind;
    }

    public ColonyLogisticsBuildingModuleKind kind() {
        return kind;
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf) {
        // This tab only opens the existing Colony Logistics menus. It does not
        // need extra MineColonies module sync data.
    }

    @Override
    public IBuildingModuleView setBuildingView(final IBuildingView buildingView) {
        this.buildingView = buildingView;
        return this;
    }

    @Override
    public BOWindow getWindow() {
        return new ColonyLogisticsMineColoniesTabWindow(this);
    }

    @Override
    public ResourceLocation getIconResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(ColonyLogistics.MOD_ID, "textures/gui/modules/logistics.png");
    }

    @Nullable
    @Override
    public Component getDesc() {
        return Component.translatable(kind.descriptionKey());
    }

    @Override
    public IBuildingModuleView setColonyView(final IColonyView colonyView) {
        this.colonyView = colonyView;
        return this;
    }

    @Override
    public IColonyView getColony() {
        return colonyView;
    }

    @Override
    public IBuildingView getBuildingView() {
        return buildingView;
    }

    @Override
    public IBuildingModuleView setProducer(final BuildingEntry.ModuleProducer moduleSet) {
        this.producer = moduleSet;
        return this;
    }

    @Override
    public BuildingEntry.ModuleProducer getProducer() {
        return producer;
    }
}
