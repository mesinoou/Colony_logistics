package jp.colonylogistics.minecolonies.module;

import java.util.Arrays;
import java.util.Optional;

/** Identifies which Colony Logistics screen a MineColonies building tab opens. */
public enum ColonyLogisticsBuildingModuleKind {
    LOGISTICS_OFFICE("logistics_office"),
    CONTAINER_DOCK("container_dock"),
    TRADE_TERMINAL("trade_terminal");

    private final String id;

    ColonyLogisticsBuildingModuleKind(final String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String descriptionKey() {
        return "minecolonies.tab.colonylogistics." + id;
    }

    public static Optional<ColonyLogisticsBuildingModuleKind> byId(final String id) {
        return Arrays.stream(values())
                .filter(kind -> kind.id.equals(id))
                .findFirst();
    }
}
