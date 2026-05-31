package jp.colonylogistics.physics;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Reflection-based Sable Companion resolver with vanilla fallback. */
public final class SableCompanionDeliveryRangeResolver implements DeliveryRangeResolver {
    private static final String[] COMPANION_CLASS_CANDIDATES = {
            "dev.ryanhcode.sable.companion.SableCompanion",
            "dev.ryanhcode.sable_companion.SableCompanion"
    };

    private final Object companionInstance;
    private final Method distanceMethod;

    public SableCompanionDeliveryRangeResolver() {
        Object instance = null;
        Method method = null;
        for (String className : COMPANION_CLASS_CANDIDATES) {
            try {
                Class<?> clazz = Class.forName(className);
                Field instanceField = clazz.getField("INSTANCE");
                Object candidate = instanceField.get(null);
                Method candidateMethod = clazz.getMethod("distanceSquaredWithSubLevels", Level.class, Vec3.class, Vec3.class);
                instance = candidate;
                method = candidateMethod;
                break;
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }
        this.companionInstance = instance;
        this.distanceMethod = method;
    }

    @Override
    public double distanceSquared(Level level, Vec3 a, Vec3 b) {
        if (companionInstance != null && distanceMethod != null) {
            try {
                Object result = distanceMethod.invoke(companionInstance, level, a, b);
                if (result instanceof Number number) return number.doubleValue();
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }
        return a.distanceToSqr(b);
    }
}
