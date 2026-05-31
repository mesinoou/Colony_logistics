package jp.colonylogistics.chat;

import jp.colonylogistics.ColonyLogistics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends deliberately simple system-chat payloads.
 *
 * <p>During multiplayer beta testing, one collaborator hit a client-side
 * DecoderException while decoding {@code clientbound/minecraft:system_chat}
 * after using the Trade Terminal. The safest server-side mitigation is to keep
 * actionable Trade Terminal notifications as literal text only, with every
 * dynamic value converted to plain strings before the Component is created.</p>
 */
public final class SafeSystemChat {
    private static final String PREFIX = "[Colony Logistics] ";

    public static void send(ServerPlayer player, String message) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.sendSystemMessage(Component.literal(PREFIX + sanitize(message)));
    }

    /**
     * Converts any translatable / styled / argument-bearing Component into a plain
     * literal before it is sent as system chat. This avoids 1.21.x network encoder
     * failures such as "This value needs to be parsed as component" when a
     * translatable Component contains raw string arguments like resource IDs.
     */
    public static void send(ServerPlayer player, Component unsafeComponent) {
        if (player == null || unsafeComponent == null) {
            return;
        }
        String plainText;
        try {
            plainText = unsafeComponent.getString();
        } catch (RuntimeException ex) {
            plainText = "Message unavailable.";
        }
        send(player, plainText);
    }

    public static String sanitize(Object value) {
        if (value == null) {
            return "-";
        }
        String raw = String.valueOf(value);
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') {
                out.append(' ');
            } else if (!Character.isISOControl(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private SafeSystemChat() {}
}
