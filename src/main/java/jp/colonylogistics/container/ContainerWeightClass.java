package jp.colonylogistics.container;

import net.minecraft.util.StringRepresentable;

public enum ContainerWeightClass implements StringRepresentable {
    EMPTY("empty", 0.25D),
    LIGHT("light", 1.0D),
    MEDIUM("medium", 2.0D),
    HEAVY("heavy", 4.0D),
    SUPER_HEAVY("super_heavy", 8.0D),
    EXTREME("extreme", 16.0D);

    private final String serializedName;
    private final double massPerBlock;

    ContainerWeightClass(String serializedName, double massPerBlock) {
        this.serializedName = serializedName;
        this.massPerBlock = massPerBlock;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public double massPerBlock() {
        return massPerBlock;
    }
}
