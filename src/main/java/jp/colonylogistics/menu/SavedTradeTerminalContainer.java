package jp.colonylogistics.menu;

import jp.colonylogistics.buildingstate.LogisticsBuildingKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;

/**
 * Two-slot setup inventory for a Trade Terminal menu.
 *
 * <p>Phase 17.9.12 makes this inventory menu-local instead of persisting it to
 * SavedData. In multiplayer, a shared terminal setup buffer lets one player
 * interact with another player's pending escrow. The actual escrow is now
 * copied into the saved player-trade contract only when the server accepts the
 * Create Trade action from the player's currently open terminal menu.</p>
 */
public final class SavedTradeTerminalContainer extends SimpleContainer {
    public static final int SLOT_REQUEST = 0;
    public static final int SLOT_REWARD = 1;

    /**
     * Signature kept so older call sites remain source-compatible; the state is
     * intentionally not loaded from or written to the world anymore.
     */
    public SavedTradeTerminalContainer(ServerLevel level, LogisticsBuildingKey key) {
        super(2);
    }
}
