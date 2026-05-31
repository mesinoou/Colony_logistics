package jp.colonylogistics.buildingstate;

import jp.colonylogistics.dock.DockMode;
import net.minecraft.nbt.CompoundTag;

/** Saved only for old worlds; Container Docks are always BOTH now. */
public record DockRuntimeState(DockMode mode) {
    public static DockRuntimeState DEFAULT = new DockRuntimeState(DockMode.BOTH);

    public DockRuntimeState withMode(DockMode mode) {
        return DEFAULT;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Mode", DockMode.BOTH.name());
        return tag;
    }

    public static DockRuntimeState load(CompoundTag tag) {
        return DEFAULT;
    }
}
