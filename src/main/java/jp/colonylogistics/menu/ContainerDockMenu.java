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
 * First-pass Container Dock menu.
 *
 * <p>The menu synchronizes a snapshot of two lists:</p>
 * <ul>
 *     <li>Accepted container contracts assigned to the player that can be spawned here.</li>
 *     <li>Nearby sealed container cores that can be submitted for delivery here.</li>
 * </ul>
 *
 * <p>Button clicks are sent through dedicated C2S payloads. The snapshot is only
 * a convenience for display; the server validates all state again before spawning
 * or delivering containers.</p>
 */
public class ContainerDockMenu extends AbstractContainerMenu {
    /*
     * Snapshot caps. These are NOT visible row counts.
     * Visible rows are controlled by ContainerDockScreen.VISIBLE_*_ROWS.
     */
    public static final int MAX_CONTRACT_ROWS = 20;
    public static final int MAX_CONTAINER_ROWS = 20;

    private final ContainerLevelAccess access;
    private final BlockPos dockPos;
    private final int colonyId;
    private final int dockLevel;
    private final boolean usable;
    private final String mode;
    private final List<DockContractRow> contractRows;
    private final List<DockContainerRow> containerRows;

    public ContainerDockMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                inventory,
                buf.readBlockPos(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readUtf(),
                readContractRows(buf),
                readContainerRows(buf)
        );
    }

    private ContainerDockMenu(
            int containerId,
            Inventory inventory,
            BlockPos dockPos,
            int colonyId,
            int dockLevel,
            boolean usable,
            String mode,
            List<DockContractRow> contractRows,
            List<DockContainerRow> containerRows
    ) {
        super(ModMenus.CONTAINER_DOCK.get(), containerId);
        this.access = ContainerLevelAccess.create(inventory.player.level(), dockPos);
        this.dockPos = dockPos;
        this.colonyId = colonyId;
        this.dockLevel = dockLevel;
        this.usable = usable;
        this.mode = mode;
        this.contractRows = List.copyOf(contractRows);
        this.containerRows = List.copyOf(containerRows);
    }

    public static ContainerDockMenu server(
            int containerId,
            Inventory inventory,
            BlockPos dockPos,
            int colonyId,
            int dockLevel,
            boolean usable,
            String mode,
            List<DockContractRow> contractRows,
            List<DockContainerRow> containerRows
    ) {
        return new ContainerDockMenu(containerId, inventory, dockPos, colonyId, dockLevel, usable, mode, contractRows, containerRows);
    }

    public static void writeSnapshot(
            RegistryFriendlyByteBuf buf,
            BlockPos dockPos,
            int colonyId,
            int dockLevel,
            boolean usable,
            String mode,
            List<DockContractRow> contractRows,
            List<DockContainerRow> containerRows
    ) {
        buf.writeBlockPos(dockPos);
        buf.writeInt(colonyId);
        buf.writeInt(dockLevel);
        buf.writeBoolean(usable);
        buf.writeUtf(mode);

        int contractCount = Math.min(MAX_CONTRACT_ROWS, contractRows.size());
        buf.writeVarInt(contractCount);
        for (int i = 0; i < contractCount; i++) {
            contractRows.get(i).write(buf);
        }

        int containerCount = Math.min(MAX_CONTAINER_ROWS, containerRows.size());
        buf.writeVarInt(containerCount);
        for (int i = 0; i < containerCount; i++) {
            containerRows.get(i).write(buf);
        }
    }

    private static List<DockContractRow> readContractRows(RegistryFriendlyByteBuf buf) {
        int count = Math.min(MAX_CONTRACT_ROWS, buf.readVarInt());
        List<DockContractRow> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(DockContractRow.read(buf));
        }
        return rows;
    }

    private static List<DockContainerRow> readContainerRows(RegistryFriendlyByteBuf buf) {
        int count = Math.min(MAX_CONTAINER_ROWS, buf.readVarInt());
        List<DockContainerRow> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(DockContainerRow.read(buf));
        }
        return rows;
    }

    public BlockPos dockPos() {
        return dockPos;
    }

    public int colonyId() {
        return colonyId;
    }

    public int dockLevel() {
        return dockLevel;
    }

    public boolean usable() {
        return usable;
    }

    public String mode() {
        return mode;
    }

    public List<DockContractRow> contractRows() {
        return contractRows;
    }

    public List<DockContainerRow> containerRows() {
        return containerRows;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CONTAINER_DOCK.get());
    }
}
