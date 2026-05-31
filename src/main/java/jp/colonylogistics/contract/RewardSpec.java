package jp.colonylogistics.contract;

import net.minecraft.resources.ResourceLocation;

public record RewardSpec(
        ResourceLocation currencyItemId,
        int currencyAmount
) {
    public RewardSpec {
        if (currencyAmount < 0) throw new IllegalArgumentException("currencyAmount must be >= 0");
    }
}
