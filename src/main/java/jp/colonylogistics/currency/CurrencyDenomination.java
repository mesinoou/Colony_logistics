package jp.colonylogistics.currency;

import net.minecraft.resources.ResourceLocation;

/** A currency item whose stack count represents a configurable base-coin value. */
public record CurrencyDenomination(ResourceLocation itemId, int baseValue) {
    public CurrencyDenomination {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
        if (baseValue <= 0) {
            throw new IllegalArgumentException("baseValue must be > 0");
        }
    }
}
