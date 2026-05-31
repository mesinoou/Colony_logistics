package jp.colonylogistics.currency;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface CurrencyAdapter {
    boolean isAvailable();

    ResourceLocation currencyItemId();

    ItemStack createCurrencyStack(int amount);

    default boolean payToPlayer(ServerPlayer player, int amount) {
        int remaining = Math.max(0, amount);
        while (remaining > 0) {
            ItemStack template = createCurrencyStack(1);
            int maxStackSize = Math.max(1, template.getMaxStackSize());
            int chunk = Math.min(maxStackSize, remaining);
            ItemStack stack = createCurrencyStack(chunk);
            boolean inserted = player.getInventory().add(stack);
            if (!inserted && !stack.isEmpty()) {
                player.drop(stack, false);
            }
            remaining -= chunk;
        }
        return true;
    }
}
