package jp.colonylogistics.menu;

import jp.colonylogistics.registry.ModMenus;
import jp.colonylogistics.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Menu for Trade Terminal escrow creation, delivery, and local history review. */
public class TradeTerminalMenu extends AbstractContainerMenu {
    /*
     * PHASE 17.4 MANUAL SLOT GUIDE
     *
     * This class controls real clickable item slots.
     * TradeTerminalScreen controls only the drawn panels/text/buttons.
     *
     * If you move any slot here, update the matching panel constants in
     * TradeTerminalScreen.java so the drawn frame still lines up with the slot.
     */
    public static final int MAX_OPEN_ROWS = 20;      // Rows synchronized to client; screen decides visible count.
    public static final int MAX_HISTORY_ROWS = 12;   // Rows synchronized to client; screen decides visible count.

    public static final int REQUEST_SLOT_X = 24;     // Request sample slot X inside GUI.
    public static final int REQUEST_SLOT_Y = 44;     // Request sample slot Y inside GUI.
    public static final int REWARD_SLOT_X = 76;      // Escrow reward slot X inside GUI.
    public static final int REWARD_SLOT_Y = 44;      // Escrow reward slot Y inside GUI.

    private final ContainerLevelAccess access;
    private final BlockPos terminalPos;
    private final int colonyId;
    private final int buildingLevel;
    private final boolean usable;
    private final List<TradeTerminalRow> openRows;
    private final List<TradeTerminalRow> historyRows;
    private final net.minecraft.world.Container setupInventory;

    public TradeTerminalMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), readRows(buf, MAX_OPEN_ROWS), readRows(buf, MAX_HISTORY_ROWS), new SimpleContainer(2));
    }

    private TradeTerminalMenu(int containerId, Inventory playerInventory, BlockPos terminalPos, int colonyId, int buildingLevel, boolean usable, List<TradeTerminalRow> openRows, List<TradeTerminalRow> historyRows, net.minecraft.world.Container setupInventory) {
        super(ModMenus.TRADE_TERMINAL.get(), containerId);
        this.terminalPos = terminalPos;
        this.colonyId = colonyId;
        this.buildingLevel = buildingLevel;
        this.usable = usable;
        this.openRows = List.copyOf(openRows);
        this.historyRows = List.copyOf(historyRows);
        this.setupInventory = setupInventory;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), terminalPos);

        addSlot(new TemplateSlot(setupInventory, SavedTradeTerminalContainer.SLOT_REQUEST, REQUEST_SLOT_X, REQUEST_SLOT_Y));
        addSlot(new Slot(setupInventory, SavedTradeTerminalContainer.SLOT_REWARD, REWARD_SLOT_X, REWARD_SLOT_Y));

        // Player inventory slot grid top-left. If changed, also adjust
        // TradeTerminalScreen.INVENTORY_PANEL_X/Y/W/H.
        int startX = 10;
        int startY = 180;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, startX + col * 18, startY + 58));
        }
    }

    public static TradeTerminalMenu server(int containerId, Inventory inventory, BlockPos terminalPos, int colonyId, int buildingLevel, boolean usable, List<TradeTerminalRow> openRows, List<TradeTerminalRow> historyRows, net.minecraft.world.Container setupInventory) {
        return new TradeTerminalMenu(containerId, inventory, terminalPos, colonyId, buildingLevel, usable, openRows, historyRows, setupInventory);
    }

    public static void writeSnapshot(RegistryFriendlyByteBuf buf, BlockPos terminalPos, int colonyId, int buildingLevel, boolean usable, List<TradeTerminalRow> openRows, List<TradeTerminalRow> historyRows) {
        buf.writeBlockPos(terminalPos);
        buf.writeVarInt(colonyId);
        buf.writeVarInt(buildingLevel);
        buf.writeBoolean(usable);
        writeRows(buf, openRows, MAX_OPEN_ROWS);
        writeRows(buf, historyRows, MAX_HISTORY_ROWS);
    }

    private static void writeRows(RegistryFriendlyByteBuf buf, List<TradeTerminalRow> rows, int maxRows) {
        int count = Math.min(maxRows, rows.size());
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            rows.get(i).write(buf);
        }
    }

    private static List<TradeTerminalRow> readRows(RegistryFriendlyByteBuf buf, int maxRows) {
        int count = Math.min(maxRows, buf.readVarInt());
        List<TradeTerminalRow> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(TradeTerminalRow.read(buf));
        }
        return rows;
    }

    public BlockPos terminalPos() { return terminalPos; }
    public int colonyId() { return colonyId; }
    public int buildingLevel() { return buildingLevel; }
    public boolean usable() { return usable; }
    public List<TradeTerminalRow> openRows() { return openRows; }
    public List<TradeTerminalRow> historyRows() { return historyRows; }
    public net.minecraft.world.Container setupInventory() { return setupInventory; }

    /** Backwards-compatible alias for older screen code while downstream patches migrate. */
    public List<TradeTerminalRow> rows() { return openRows; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 2) {
                if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            returnSetupItem(player, SavedTradeTerminalContainer.SLOT_REQUEST);
            returnSetupItem(player, SavedTradeTerminalContainer.SLOT_REWARD);
        }
    }

    private void returnSetupItem(Player player, int slotIndex) {
        ItemStack stack = setupInventory.getItem(slotIndex);
        if (stack.isEmpty()) {
            return;
        }
        ItemStack returned = stack.copy();
        setupInventory.setItem(slotIndex, ItemStack.EMPTY);
        if (!player.getInventory().add(returned)) {
            player.drop(returned, false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.TRADE_TERMINAL.get());
    }

    private static final class TemplateSlot extends Slot {
        private TemplateSlot(net.minecraft.world.Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }
}
