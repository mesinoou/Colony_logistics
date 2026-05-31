package jp.colonylogistics.freight;

import net.minecraft.resources.ResourceLocation;

public record VirtualCargo(
        ResourceLocation cargoId,
        int amount,
        int volume,
        int gameplayWeight,
        int value,
        int fragility
) {
    public VirtualCargo {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (volume < 0 || gameplayWeight < 0 || value < 0 || fragility < 0) {
            throw new IllegalArgumentException("cargo properties must be >= 0");
        }
    }
}
