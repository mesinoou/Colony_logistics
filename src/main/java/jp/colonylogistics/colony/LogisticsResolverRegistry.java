package jp.colonylogistics.colony;

import java.util.Objects;

/**
 * Single swap point for MineColonies integration.
 *
 * The concrete resolver should be installed by the MineColonies registration slice once
 * the exact 1.1.1300 building classes are connected. Keeping this indirection prevents
 * contract/container code from importing MineColonies internals.
 */
public final class LogisticsResolverRegistry {
    private static MineColoniesLogisticsResolver resolver = NoopMineColoniesLogisticsResolver.INSTANCE;

    public static MineColoniesLogisticsResolver get() {
        return resolver;
    }

    public static void set(MineColoniesLogisticsResolver next) {
        resolver = Objects.requireNonNull(next, "resolver");
    }

    private LogisticsResolverRegistry() {}
}
