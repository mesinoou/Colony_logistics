package jp.colonylogistics.client.screen;

import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.menu.FreightBoardRow;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/** Client-only formatting helpers for compact GUI labels. */
final class ClientDisplayNames {
    private ClientDisplayNames() {}

    static String itemName(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw) || "unknown".equalsIgnoreCase(raw)) {
            return raw == null ? "" : raw;
        }
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return compactResource(raw);
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item != Items.AIR || "minecraft:air".equals(raw)) {
            return item.getDescription().getString();
        }
        String cargo = virtualCargoName(id);
        if (cargo != null) {
            return cargo;
        }
        return compactResource(raw);
    }

    static String itemPhrase(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw.trim();
        int x = text.indexOf("x ");
        if (x > 0) {
            String count = text.substring(0, x).trim();
            String id = text.substring(x + 2).trim();
            if (count.chars().allMatch(Character::isDigit)) {
                return count + "x " + itemName(id);
            }
        }
        return itemName(text);
    }

    static String deliveryUnit(DeliveryUnitType type) {
        return translateOrFallback("screen.colonylogistics.delivery_unit." + type.name().toLowerCase(Locale.ROOT), compactToken(type.name()));
    }

    static String source(FreightBoardRow row) {
        if (row.isPlayerTrade()) {
            return translateOrFallback("screen.colonylogistics.source.player_trade", "Trade");
        }
        return deliveryUnit(row.deliveryUnitType());
    }

    static String containerText(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw)) {
            return "-";
        }
        String[] parts = raw.split("/");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            String token = part.trim();
            if (out.length() > 0) out.append(" / ");
            out.append(containerToken(token));
        }
        return out.toString();
    }

    static String containerSize(String raw) {
        return translateOrFallback("screen.colonylogistics.container_size." + raw.toLowerCase(Locale.ROOT), compactToken(raw));
    }

    static String containerWeight(String raw) {
        return translateOrFallback("screen.colonylogistics.weight_class." + raw.toLowerCase(Locale.ROOT), compactToken(raw));
    }

    static String statusToken(String raw) {
        return translateOrFallback("screen.colonylogistics.status_token." + raw.toLowerCase(Locale.ROOT), compactToken(raw));
    }

    private static String containerToken(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        String size = translateOrFallback("screen.colonylogistics.container_size." + lower, null);
        if (size != null) return size;
        String weight = translateOrFallback("screen.colonylogistics.weight_class." + lower, null);
        if (weight != null) return weight;
        return compactToken(token);
    }

    private static String virtualCargoName(ResourceLocation id) {
        if (!"colonylogistics".equals(id.getNamespace())) {
            return null;
        }
        String key = "screen.colonylogistics.cargo." + id.getPath();
        String translated = Component.translatable(key).getString();
        return translated.equals(key) ? null : translated;
    }

    private static String translateOrFallback(String key, String fallback) {
        String translated = Component.translatable(key).getString();
        return translated.equals(key) ? fallback : translated;
    }

    private static String compactToken(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String text = raw.toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] words = text.split(" ");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private static String compactResource(String raw) {
        int colon = raw.indexOf(':');
        String path = colon >= 0 ? raw.substring(colon + 1) : raw;
        int slash = path.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < path.length()) {
            path = path.substring(slash + 1);
        }
        return compactToken(path);
    }
}
