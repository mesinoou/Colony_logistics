package jp.colonylogistics.currency;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemCurrencyAdapter implements CurrencyAdapter {
    private final ResourceLocation currencyItemId;

    public ItemCurrencyAdapter(ResourceLocation currencyItemId) {
        this.currencyItemId = currencyItemId;
    }

    @Override
    public boolean isAvailable() {
        return BuiltInRegistries.ITEM.containsKey(currencyItemId) && BuiltInRegistries.ITEM.get(currencyItemId) != Items.AIR;
    }

    @Override
    public ResourceLocation currencyItemId() {
        return currencyItemId;
    }

    @Override
    public ItemStack createCurrencyStack(int amount) {
        Item item = BuiltInRegistries.ITEM.get(currencyItemId);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(0, amount));
    }
}
