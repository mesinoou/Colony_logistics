package jp.colonylogistics.dock;

import net.minecraft.util.StringRepresentable;

import java.util.Optional;

/**
 * Container Docks are permanently bidirectional as of Phase 17.9.8.
 *
 * <p>The enum remains only so old SavedData and menu snapshots can keep their
 * wire shape. Any legacy EXPORT/IMPORT value is normalized to BOTH.</p>
 */
public enum DockMode implements StringRepresentable {
    BOTH("both");

    private final String serializedName;

    DockMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public boolean canExport() {
        return true;
    }

    public boolean canImport() {
        return true;
    }

    public static Optional<DockMode> fromSerializedName(String raw) {
        return Optional.of(BOTH);
    }
}
