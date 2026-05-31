package jp.colonylogistics.physics;

/**
 * Central switch point for distance calculations.
 *
 * <p>Use the Sable Companion implementation by default. Sable Companion is a
 * safe Jar-in-Jar compatibility layer: without Sable installed it returns
 * vanilla-space answers, while Sable replaces the implementation at runtime.</p>
 */
public final class DeliveryRangeResolvers {
    private static DeliveryRangeResolver current = new SableCompanionDeliveryRangeResolver();

    public static DeliveryRangeResolver current() {
        return current;
    }

    public static void useVanillaForDebug() {
        current = DeliveryRangeResolver.VANILLA;
    }

    public static void useSableCompanion() {
        current = new SableCompanionDeliveryRangeResolver();
    }

    private DeliveryRangeResolvers() {}
}
