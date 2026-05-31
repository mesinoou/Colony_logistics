package jp.colonylogistics.menu;

import jp.colonylogistics.registry.ModMenus;
import jp.colonylogistics.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only Freight Board menu.
 *
 * <p>The menu does not expose item slots. It only synchronizes a snapshot of open
 * contracts at menu-open time. Accepting a job is handled by a dedicated C2S payload; this menu remains
 * a read-only snapshot and the server revalidates the selected contract.</p>
 */
public class FreightBoardMenu extends AbstractContainerMenu {
    /**
     * Maximum rows sent in the open-menu snapshot. Rendering is scrollable, so this is a
     * packet safety cap rather than the number of visible rows.
     */
    public static final int MAX_ROWS = 200; // Snapshot cap only; visible rows are in FreightBoardScreen.

    private final ContainerLevelAccess access;
    private final BlockPos boardPos;
    private final List<FreightBoardRow> rows;

    public FreightBoardMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBlockPos(), readRows(buf));
    }

    private FreightBoardMenu(int containerId, Inventory inventory, BlockPos boardPos, List<FreightBoardRow> rows) {
        super(ModMenus.FREIGHT_BOARD.get(), containerId);
        this.boardPos = boardPos;
        this.rows = List.copyOf(rows);
        this.access = ContainerLevelAccess.create(inventory.player.level(), boardPos);
    }

    public static FreightBoardMenu server(int containerId, Inventory inventory, BlockPos boardPos, List<FreightBoardRow> rows) {
        return new FreightBoardMenu(containerId, inventory, boardPos, rows);
    }

    public static void writeSnapshot(RegistryFriendlyByteBuf buf, BlockPos boardPos, List<FreightBoardRow> rows) {
        buf.writeBlockPos(boardPos);
        int count = Math.min(MAX_ROWS, rows.size());
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            rows.get(i).write(buf);
        }
    }

    private static List<FreightBoardRow> readRows(RegistryFriendlyByteBuf buf) {
        int count = Math.min(MAX_ROWS, buf.readVarInt());
        List<FreightBoardRow> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(FreightBoardRow.read(buf));
        }
        return rows;
    }

    public List<FreightBoardRow> rows() {
        return rows;
    }

    public BlockPos boardPos() {
        return boardPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.LOGISTICS_OFFICE.get());
    }
}
