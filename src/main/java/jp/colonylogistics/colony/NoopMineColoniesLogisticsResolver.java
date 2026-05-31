package jp.colonylogistics.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Safe fallback used until the concrete MineColonies building registration is wired.
 * Returning empty keeps the rest of the logistics code testable in plain dev worlds.
 */
public final class NoopMineColoniesLogisticsResolver implements MineColoniesLogisticsResolver {
    public static final NoopMineColoniesLogisticsResolver INSTANCE = new NoopMineColoniesLogisticsResolver();

    private NoopMineColoniesLogisticsResolver() {}

    @Override
    public Optional<ResolvedColonyBuilding> resolveBuilding(ServerLevel level, BlockPos pos) {
        return Optional.empty();
    }
}
