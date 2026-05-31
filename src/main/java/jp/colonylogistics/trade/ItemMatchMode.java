package jp.colonylogistics.trade;

import net.minecraft.world.item.ItemStack;

/**
 * Controls how strictly a delivered stack must match the requested template.
 */
public enum ItemMatchMode {
    /** Same item id only; damage/custom name/enchantments/data components are ignored. */
    ITEM_ONLY,

    /** Same item id and the same 1.21 Data Components. This is the safer default for escrow trades. */
    ITEM_AND_COMPONENTS;

    public boolean matches(ItemStack offered, ItemStack template) {
        if (offered.isEmpty() || template.isEmpty()) return false;
        return switch (this) {
            case ITEM_ONLY -> offered.is(template.getItem());
            case ITEM_AND_COMPONENTS -> ItemStack.isSameItemSameComponents(offered, template);
        };
    }

    public static ItemMatchMode byOrdinal(int ordinal) {
        ItemMatchMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) return ITEM_AND_COMPONENTS;
        return values[ordinal];
    }

    public static ItemMatchMode fromName(String name) {
        try {
            return ItemMatchMode.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return ITEM_AND_COMPONENTS;
        }
    }
}
