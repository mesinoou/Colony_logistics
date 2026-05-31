package jp.colonylogistics.physics;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Sable-aware distance boundary.
 *
 * Default vanilla implementation is kept for debugging. Normal gameplay should
 * use DeliveryRangeResolvers.current(), which is Sable Companion-backed.
 */
public interface DeliveryRangeResolver {
    double distanceSquared(Level level, Vec3 a, Vec3 b);

    DeliveryRangeResolver VANILLA = (level, a, b) -> a.distanceToSqr(b);
}
