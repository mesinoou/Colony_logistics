package jp.colonylogistics.minecolonies.client;

import com.minecolonies.core.client.gui.AbstractModuleWindow;
import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.minecolonies.module.ColonyLogisticsBuildingModuleView;
import jp.colonylogistics.network.OpenColonyLogisticsMenuPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/** MineColonies building-window tab that jumps into the legacy Colony Logistics menu. */
public class ColonyLogisticsMineColoniesTabWindow extends AbstractModuleWindow<ColonyLogisticsBuildingModuleView> {
    private static final ResourceLocation WINDOW = ResourceLocation.fromNamespaceAndPath(
            ColonyLogistics.MOD_ID,
            "gui/minecolonies/colony_logistics_tab.xml"
    );
    private static final String BUTTON_OPEN = "open_colony_logistics";

    public ColonyLogisticsMineColoniesTabWindow(final ColonyLogisticsBuildingModuleView moduleView) {
        super(moduleView, WINDOW);
        registerButton(BUTTON_OPEN, this::openColonyLogisticsMenu);
    }

    private void openColonyLogisticsMenu() {
        if (moduleView.getBuildingView() == null) {
            return;
        }
        PacketDistributor.sendToServer(new OpenColonyLogisticsMenuPayload(
                moduleView.getBuildingView().getID(),
                moduleView.kind().id()
        ));
    }
}
