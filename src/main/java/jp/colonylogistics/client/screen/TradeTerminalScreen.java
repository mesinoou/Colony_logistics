package jp.colonylogistics.client.screen;

import jp.colonylogistics.menu.TradeTerminalMenu;
import jp.colonylogistics.menu.TradeTerminalRow;
import jp.colonylogistics.network.CancelPlayerTradePayload;
import jp.colonylogistics.network.CreatePlayerTradePayload;
import jp.colonylogistics.network.DeliverPlayerTradePayload;
import jp.colonylogistics.trade.ItemMatchMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Trade Terminal UI with scrollable trade panels and a correctly framed player inventory area. */
public class TradeTerminalScreen extends AbstractContainerScreen<TradeTerminalMenu> {
    /*
     * PHASE 17.4 MANUAL LAYOUT GUIDE
     *
     * Panel model:
     * - LEFT_PANEL_*: request sample slot, escrow reward slot, amount buttons, create buttons.
     * - OPEN_PANEL_*: open player trades and deliver/cancel buttons.
     * - HISTORY_PANEL_*: completed/cancelled trade history.
     * - INVENTORY_PANEL_*: only the drawn background behind player inventory slots.
     *
     * IMPORTANT:
     * - The actual item slot positions are in TradeTerminalMenu.java, not here.
     * - If you move INVENTORY_PANEL_X/Y, also move startX/startY in TradeTerminalMenu.java.
     * - If you move REQUEST/REWARD slots, change TradeTerminalMenu.REQUEST_SLOT_* too.
     *
     * Safe manual tuning:
     * - If trade rows overlap: increase OPEN_ROW_HEIGHT/HISTORY_ROW_HEIGHT.
     * - If text overlaps action buttons: increase OPEN_PANEL_W or move ACTION_X left/right.
     * - If inventory overlaps history: increase HISTORY_PANEL_Y/HISTORY_PANEL_H or INVENTORY_PANEL_Y.
     */
    private static final int OPEN_ROW_HEIGHT = 28;       // Height of one open-trade row.
    private static final int HISTORY_ROW_HEIGHT = 28;    // Height of one history row.
    private static final int VISIBLE_OPEN_ROWS = 3;      // Open trades visible before scroll.
    private static final int VISIBLE_HISTORY_ROWS = 2;   // History rows visible before scroll.
    private static final int REQUEST_COUNT_MIN = 1;
    private static final int REQUEST_COUNT_MAX = 64;

    private static final int LEFT_PANEL_X = 10;          // Setup panel X.
    private static final int LEFT_PANEL_Y = 22;          // Setup panel Y.
    private static final int LEFT_PANEL_W = 160;         // Setup panel width.
    private static final int LEFT_PANEL_H = 150;         // Setup panel height.
    private static final int OPEN_PANEL_X = 180;         // Open trades panel X.
    private static final int OPEN_PANEL_Y = 22;          // Open trades panel Y.
    private static final int OPEN_PANEL_W = 300;         // Open trades panel width.
    private static final int OPEN_PANEL_H = 116;         // Open trades panel height.
    private static final int HISTORY_PANEL_X = 180;      // History panel X.
    private static final int HISTORY_PANEL_Y = 142;      // History panel Y.
    private static final int HISTORY_PANEL_W = 300;      // History panel width.
    private static final int HISTORY_PANEL_H = 68;       // History panel height.
    private static final int INVENTORY_PANEL_X = 4;    // Drawn inventory background X.
    private static final int INVENTORY_PANEL_Y = 176;    // Drawn inventory background Y.
    private static final int INVENTORY_PANEL_W = 176;    // Drawn inventory background width.
    private static final int INVENTORY_PANEL_H = 84;     // Drawn inventory background height.
    private static final int OPEN_ROW_Y = OPEN_PANEL_Y + 18;
    private static final int HISTORY_ROW_Y = HISTORY_PANEL_Y + 18;
    private static final int ACTION_X = OPEN_PANEL_X + OPEN_PANEL_W - 95; // Deliver/cancel button X.

    private int requestedCount = 1;
    private int openScrollOffset = 0;
    private int historyScrollOffset = 0;

    public TradeTerminalScreen(TradeTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Outer GUI size. If you increase height for more inventory/history space,
        // also review INVENTORY_PANEL_Y and TradeTerminalMenu startY.
        this.imageWidth = 470;
        this.imageHeight = 306;

        // Hide vanilla inventory label; this screen draws custom section labels.
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        rebuildTradeTerminalButtons();
    }

    private void rebuildTradeTerminalButtons() {
        clearWidgets();
        clampScrollOffsets();

        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
                    requestedCount = Math.max(REQUEST_COUNT_MIN, requestedCount - 1);
                    rebuildTradeTerminalButtons();
                }).bounds(leftPos + 16, topPos + 86, 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
                    requestedCount = Math.min(REQUEST_COUNT_MAX, requestedCount + 1);
                    rebuildTradeTerminalButtons();
                }).bounds(leftPos + 102, topPos + 86, 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal("-10"), button -> {
                    requestedCount = Math.max(REQUEST_COUNT_MIN, requestedCount - 10);
                    rebuildTradeTerminalButtons();
                }).bounds(leftPos + 16, topPos + 108, 50, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+10"), button -> {
                    requestedCount = Math.min(REQUEST_COUNT_MAX, requestedCount + 10);
                    rebuildTradeTerminalButtons();
                }).bounds(leftPos + 76, topPos + 108, 50, 18).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.colonylogistics.trade_terminal.create_exact"), button -> createTrade(ItemMatchMode.ITEM_AND_COMPONENTS))
                .bounds(leftPos + 12, topPos + 132, 56, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.colonylogistics.trade_terminal.create_item_only"), button -> createTrade(ItemMatchMode.ITEM_ONLY))
                .bounds(leftPos + 74, topPos + 132, 56, 22).build());

        UUID self = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : new UUID(0L, 0L);
        List<TradeTerminalRow> rows = menu.openRows();
        int visible = Math.min(VISIBLE_OPEN_ROWS, Math.max(0, rows.size() - openScrollOffset));
        for (int i = 0; i < visible; i++) {
            TradeTerminalRow row = rows.get(openScrollOffset + i);
            int rowY = topPos + OPEN_ROW_Y + i * OPEN_ROW_HEIGHT + 2;
            if (row.creatorPlayer().equals(self)) {
                addRenderableWidget(Button.builder(Component.translatable("screen.colonylogistics.trade_terminal.deliver_short"), button ->
                                PacketDistributor.sendToServer(new DeliverPlayerTradePayload(row.contractId(), menu.terminalPos())))
                        .bounds(leftPos + ACTION_X, rowY, 42, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("screen.colonylogistics.trade_terminal.cancel_short"), button ->
                                PacketDistributor.sendToServer(new CancelPlayerTradePayload(row.contractId(), menu.terminalPos())))
                        .bounds(leftPos + ACTION_X + 46, rowY, 42, 18).build());
            } else {
                addRenderableWidget(Button.builder(Component.translatable("screen.colonylogistics.trade_terminal.deliver"), button ->
                                PacketDistributor.sendToServer(new DeliverPlayerTradePayload(row.contractId(), menu.terminalPos())))
                        .bounds(leftPos + ACTION_X + 46, rowY, 42, 18).build());
            }
        }
    }

    private void createTrade(ItemMatchMode mode) {
        PacketDistributor.sendToServer(new CreatePlayerTradePayload(menu.terminalPos(), mode, requestedCount));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xE0101010);
        drawPanel(graphics, LEFT_PANEL_X, LEFT_PANEL_Y, LEFT_PANEL_W, LEFT_PANEL_H, 0x60202020);
        drawPanel(graphics, OPEN_PANEL_X, OPEN_PANEL_Y, OPEN_PANEL_W, OPEN_PANEL_H, 0x60202020);
        drawPanel(graphics, HISTORY_PANEL_X, HISTORY_PANEL_Y, HISTORY_PANEL_W, HISTORY_PANEL_H, 0x60303030);
        drawPanel(graphics, INVENTORY_PANEL_X, INVENTORY_PANEL_Y, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 0x40202020);
        drawSlotFrame(graphics, TradeTerminalMenu.REQUEST_SLOT_X, TradeTerminalMenu.REQUEST_SLOT_Y);
        drawSlotFrame(graphics, TradeTerminalMenu.REWARD_SLOT_X, TradeTerminalMenu.REWARD_SLOT_Y);
        renderScrollbar(graphics, OPEN_PANEL_X + OPEN_PANEL_W - 8, OPEN_ROW_Y - 2, VISIBLE_OPEN_ROWS * OPEN_ROW_HEIGHT, menu.openRows().size(), openScrollOffset, VISIBLE_OPEN_ROWS);
        renderScrollbar(graphics, HISTORY_PANEL_X + HISTORY_PANEL_W - 8, HISTORY_ROW_Y - 2, VISIBLE_HISTORY_ROWS * HISTORY_ROW_HEIGHT, menu.historyRows().size(), historyScrollOffset, VISIBLE_HISTORY_ROWS);
    }

    private void drawPanel(GuiGraphics graphics, int relX, int relY, int w, int h, int fill) {
        int x = leftPos + relX;
        int y = topPos + relY;
        graphics.fill(x, y, x + w, y + h, fill);
        graphics.fill(x, y, x + w, y + 1, 0x90A0A0A0);
        graphics.fill(x, y + h - 1, x + w, y + h, 0x90202020);
        graphics.fill(x, y, x + 1, y + h, 0x90A0A0A0);
        graphics.fill(x + w - 1, y, x + w, y + h, 0x90202020);
    }

    private void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        int x = leftPos + slotX;
        int y = topPos + slotY;
        graphics.fill(x - 2, y - 2, x + 20, y + 20, 0xFF8A8A8A);
        graphics.fill(x - 1, y - 1, x + 19, y + 19, 0xFF202020);
        graphics.fill(x, y, x + 18, y + 18, 0xFF404040);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.status", menu.colonyId(), menu.buildingLevel()), 190, 8, 0xCFCFCF, false);
        if (!menu.usable()) graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.inactive"), 392, 8, 0xFF6666, false);

        graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.request_slot"), LEFT_PANEL_X + 8, LEFT_PANEL_Y + 10, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.reward_slot"), LEFT_PANEL_X + 60, LEFT_PANEL_Y + 10, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.request_count", requestedCount), LEFT_PANEL_X + 40, LEFT_PANEL_Y + 70, 0xFFFFFF, false);
        //graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.sample_return_hint"), LEFT_PANEL_X + 8, LEFT_PANEL_Y + 118, 0x888888, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.open_trades"), OPEN_PANEL_X + 8, OPEN_PANEL_Y + 8, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.history"), HISTORY_PANEL_X + 8, HISTORY_PANEL_Y + 8, 0xD0D0D0, false);
        renderCountHint(graphics, menu.openRows().size(), openScrollOffset, VISIBLE_OPEN_ROWS, OPEN_PANEL_X + OPEN_PANEL_W - 72, OPEN_PANEL_Y + 8);
        renderCountHint(graphics, menu.historyRows().size(), historyScrollOffset, VISIBLE_HISTORY_ROWS, HISTORY_PANEL_X + HISTORY_PANEL_W - 72, HISTORY_PANEL_Y + 8);

        UUID self = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : new UUID(0L, 0L);
        renderOpenRows(graphics, self);
        renderHistoryRows(graphics, self);
    }

    private void renderCountHint(GuiGraphics graphics, int total, int offset, int visibleRows, int x, int y) {
        if (total > visibleRows) {
            int from = offset + 1;
            int to = Math.min(total, offset + visibleRows);
            graphics.drawString(font, Component.literal(from + "-" + to + "/" + total), x, y, 0x888888, false);
        }
    }

    private void renderOpenRows(GuiGraphics graphics, UUID self) {
        List<TradeTerminalRow> rows = menu.openRows();
        if (rows.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.empty"), OPEN_PANEL_X + 8, OPEN_ROW_Y, 0xAAAAAA, false);
            return;
        }
        int y = OPEN_ROW_Y;
        int visible = Math.min(VISIBLE_OPEN_ROWS, Math.max(0, rows.size() - openScrollOffset));
        for (int i = 0; i < visible; i++) {
            drawTradeRow(graphics, rows.get(openScrollOffset + i), self, y, true, OPEN_PANEL_X + 8, OPEN_PANEL_W - 114);
            y += OPEN_ROW_HEIGHT;
        }
    }

    private void renderHistoryRows(GuiGraphics graphics, UUID self) {
        List<TradeTerminalRow> rows = menu.historyRows();
        if (rows.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.colonylogistics.trade_terminal.no_history"), HISTORY_PANEL_X + 8, HISTORY_ROW_Y, 0xAAAAAA, false);
            return;
        }
        int y = HISTORY_ROW_Y;
        int visible = Math.min(VISIBLE_HISTORY_ROWS, Math.max(0, rows.size() - historyScrollOffset));
        for (int i = 0; i < visible; i++) {
            drawTradeRow(graphics, rows.get(historyScrollOffset + i), self, y, false, HISTORY_PANEL_X + 8, HISTORY_PANEL_W - 16);
            y += HISTORY_ROW_HEIGHT;
        }
    }

    private void drawTradeRow(GuiGraphics graphics, TradeTerminalRow row, UUID self, int y, boolean open, int x, int textWidth) {
        boolean mine = row.creatorPlayer().equals(self);
        int color = switch (row.status()) {
            case COMPLETED -> 0x80FF80;
            case CANCELLED, FAILED, EXPIRED -> 0xFF8080;
            case ACCEPTED, PICKED_UP, DELIVERED -> 0xFFE080;
            default -> 0xFFFFFF;
        };
        graphics.fill(x - 4, y - 2, x + textWidth + (open ? 98 : 4), y + 24, 0x30101010);
        drawWrapped(graphics, row.requestedCount() + "x " + ClientDisplayNames.itemName(row.requestedItemId().toString()), x, y, textWidth, color, 1);
        drawWrapped(graphics, "=> " + row.rewardCount() + "x " + ClientDisplayNames.itemName(row.rewardItemId().toString()), x, y + 10, textWidth, 0xD8FFD8, 1);
        String tail = Component.translatable("screen.colonylogistics.trade_terminal.match_mode." + row.matchMode().name().toLowerCase(Locale.ROOT)).getString();
        if (open && mine) tail += " / " + Component.translatable("screen.colonylogistics.trade_terminal.mine").getString();
        if (!open) tail += " / " + Component.translatable("screen.colonylogistics.trade_terminal.status." + row.status().name().toLowerCase(Locale.ROOT)).getString();
        drawWrapped(graphics, tail, x + textWidth - 55, y + 10, open ? 88 : 100, 0xCFCFCF, 1);
    }

    private void renderScrollbar(GuiGraphics graphics, int relX, int relY, int h, int total, int offset, int visible) {
        if (total <= visible) return;
        int x = leftPos + relX;
        int y = topPos + relY;
        graphics.fill(x, y, x + 4, y + h, 0x60303030);
        int maxOffset = Math.max(1, total - visible);
        int thumbHeight = Math.max(18, h * visible / total);
        int travel = Math.max(1, h - thumbHeight);
        int thumbTop = y + travel * offset / maxOffset;
        graphics.fill(x, thumbTop, x + 4, thumbTop + thumbHeight, 0xC0C0C0C0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        if (inside(localX, localY, OPEN_PANEL_X, OPEN_ROW_Y - 4, OPEN_PANEL_W, VISIBLE_OPEN_ROWS * OPEN_ROW_HEIGHT + 8) && menu.openRows().size() > VISIBLE_OPEN_ROWS) {
            openScrollOffset += scrollY > 0 ? -1 : 1;
            clampScrollOffsets();
            rebuildTradeTerminalButtons();
            return true;
        }
        if (inside(localX, localY, HISTORY_PANEL_X, HISTORY_ROW_Y - 4, HISTORY_PANEL_W, VISIBLE_HISTORY_ROWS * HISTORY_ROW_HEIGHT + 8) && menu.historyRows().size() > VISIBLE_HISTORY_ROWS) {
            historyScrollOffset += scrollY > 0 ? -1 : 1;
            clampScrollOffsets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void clampScrollOffsets() {
        openScrollOffset = Math.max(0, Math.min(openScrollOffset, Math.max(0, menu.openRows().size() - VISIBLE_OPEN_ROWS)));
        historyScrollOffset = Math.max(0, Math.min(historyScrollOffset, Math.max(0, menu.historyRows().size() - VISIBLE_HISTORY_ROWS)));
    }

    private boolean inside(int x, int y, int relX, int relY, int w, int h) {
        return x >= relX && x < relX + w && y >= relY && y < relY + h;
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        if (inside(localX, localY, OPEN_PANEL_X, OPEN_ROW_Y - 4, OPEN_PANEL_W, VISIBLE_OPEN_ROWS * OPEN_ROW_HEIGHT + 8)) {
            int index = openScrollOffset + Math.max(0, localY - OPEN_ROW_Y) / OPEN_ROW_HEIGHT;
            if (index >= 0 && index < menu.openRows().size()) renderTradeTooltip(graphics, menu.openRows().get(index), mouseX, mouseY);
        } else if (inside(localX, localY, HISTORY_PANEL_X, HISTORY_ROW_Y - 4, HISTORY_PANEL_W, VISIBLE_HISTORY_ROWS * HISTORY_ROW_HEIGHT + 8)) {
            int index = historyScrollOffset + Math.max(0, localY - HISTORY_ROW_Y) / HISTORY_ROW_HEIGHT;
            if (index >= 0 && index < menu.historyRows().size()) renderTradeTooltip(graphics, menu.historyRows().get(index), mouseX, mouseY);
        }
    }

    private void renderTradeTooltip(GuiGraphics graphics, TradeTerminalRow row, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(font, List.of(
                Component.translatable("screen.colonylogistics.trade_terminal.tooltip.contract", row.contractId().toString()),
                Component.translatable("screen.colonylogistics.trade_terminal.tooltip.request", row.requestedCount(), ClientDisplayNames.itemName(row.requestedItemId().toString())),
                Component.translatable("screen.colonylogistics.trade_terminal.tooltip.reward", row.rewardCount(), ClientDisplayNames.itemName(row.rewardItemId().toString())),
                Component.translatable("screen.colonylogistics.trade_terminal.tooltip.mode", row.matchMode().name()),
                Component.translatable("screen.colonylogistics.trade_terminal.tooltip.status", row.status().name())
        ), mouseX, mouseY);
    }

    private void drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color, int maxLines) {
        List<String> lines = wrapToWidth(text, width, maxLines);
        for (int i = 0; i < lines.size(); i++) graphics.drawString(font, lines.get(i), x, y + i * 9, color, false);
    }

    private List<String> wrapToWidth(String text, int width, int maxLines) {
        List<String> result = new ArrayList<>();
        String remaining = text == null ? "" : text;
        while (!remaining.isEmpty() && result.size() < maxLines) {
            int fit = 0;
            int lastSpace = -1;
            while (fit < remaining.length() && font.width(remaining.substring(0, fit + 1)) <= width) {
                if (Character.isWhitespace(remaining.charAt(fit))) lastSpace = fit;
                fit++;
            }
            if (fit == remaining.length()) {
                result.add(remaining);
                break;
            }
            int split = lastSpace > 0 ? lastSpace : Math.max(1, fit - 1);
            String line = remaining.substring(0, split).trim();
            remaining = remaining.substring(split).trim();
            if (result.size() == maxLines - 1 && !remaining.isEmpty()) {
                while (!line.isEmpty() && font.width(line + "...") > width) line = line.substring(0, line.length() - 1);
                result.add(line + "...");
                remaining = "";
            } else result.add(line);
        }
        if (result.isEmpty()) result.add("");
        return result;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }
}
