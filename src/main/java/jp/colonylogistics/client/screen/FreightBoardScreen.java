package jp.colonylogistics.client.screen;

import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.menu.FreightBoardMenu;
import jp.colonylogistics.menu.FreightBoardRow;
import jp.colonylogistics.network.AcceptFreightPayload;
import jp.colonylogistics.network.CancelFreightPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Freight Board UI with wider columns, wrapped details, scrolling, and row tooltips. */
public class FreightBoardScreen extends AbstractContainerScreen<FreightBoardMenu> {
    /*
     * PHASE 17.4 MANUAL LAYOUT GUIDE
     * This screen is now the Logistics Office freight view.
     *
     * Coordinate model:
     * - All constants below are relative to the GUI's left/top corner, not screen pixels.
     * - imageWidth/imageHeight in the constructor define the outer dark background.
     * - TAB_Y controls the filter buttons at the top.
     * - HEADER_Y is the column title row.
     * - ROW_START_Y is the first contract row.
     * - DETAILS_Y is derived from ROW_START_Y, ROW_HEIGHT, and VISIBLE_ROWS.
     *
     * Safe manual tuning:
     * - If rows overlap vertically: increase ROW_HEIGHT or reduce VISIBLE_ROWS.
     * - If the details panel overlaps rows: DETAILS_Y updates automatically from row values.
     * - If text overlaps horizontally: move CARGO_X/STATUS_X/ASSIGNEE_X/REWARD_X.
     * - If the accept button overlaps reward text: reduce REWARD_X or increase imageWidth.
     * - If the GUI is too wide: reduce imageWidth first, then shift columns left.
     */
    private static final int ROW_HEIGHT = 18;       // Pixel height per visible contract row.
    private static final int VISIBLE_ROWS = 7;      // Visible rows before mouse-wheel scrolling.
    private static final int TAB_Y = 22;            // Filter tab button Y.
    private static final int HEADER_Y = 47;         // Column header Y.
    private static final int ROW_START_Y = 62;      // First contract row Y.
    private static final int DETAILS_Y = ROW_START_Y + VISIBLE_ROWS * ROW_HEIGHT + 8; // Details panel top.
    private static final int ROUTE_X = 10;          // Route column X.
    private static final int CARGO_X = 78;          // Cargo/contract kind column X.
    private static final int STATUS_X = 250;        // Status column X.
    private static final int ASSIGNEE_X = 300;      // Carrier/assignee column X.
    private static final int REWARD_X = 340;        // Reward column X.
    private static final int ACTION_W = 40;         // Accept/terminal button width.

    private static BoardFilter rememberedFilter = BoardFilter.ALL;
    private static int rememberedScrollOffset = 0;
    private static UUID rememberedSelectedContractId;

    private BoardFilter activeFilter = rememberedFilter;
    private int scrollOffset = rememberedScrollOffset;
    private UUID selectedContractId = rememberedSelectedContractId;

    public FreightBoardScreen(FreightBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Outer GUI size. Width 500 is the current compromise between readability and not being too wide.
        // If you change imageWidth, also review column X constants above and filter count text X below.
        this.imageWidth = 470;
        this.imageHeight = 258;

        // Hide the vanilla inventory label because this menu has no player inventory slots.
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        rebuildFreightBoardWidgets();
    }

    private void rebuildFreightBoardWidgets() {
        clampScrollOffset();
        ensureSelectedRowExists();
        rememberState();
        clearWidgets();
        addFilterTabs();
        addAcceptButtons();
    }

    private void addFilterTabs() {
        int x = leftPos + 8;
        int y = topPos + TAB_Y;
        for (BoardFilter filter : BoardFilter.values()) {
            Button tab = Button.builder(filter.label(), ignored -> {
                        if (activeFilter != filter) {
                            activeFilter = filter;
                            scrollOffset = 0;
                            selectedContractId = null;
                            rebuildFreightBoardWidgets();
                        }
                    }).bounds(x, y, filter.width(), 18).build();
            tab.active = activeFilter != filter;
            addRenderableWidget(tab);
            x += filter.width() + 4;
        }
    }

    private void addAcceptButtons() {
        List<FreightBoardRow> rows = filteredRows();
        int x = leftPos + imageWidth - ACTION_W - 10;
        int y = topPos + ROW_START_Y;
        int visibleCount = Math.min(VISIBLE_ROWS, Math.max(0, rows.size() - scrollOffset));
        for (int i = 0; i < visibleCount; i++) {
            FreightBoardRow row = rows.get(scrollOffset + i);
            int buttonY = y + i * ROW_HEIGHT - 4;
            Component label = actionLabel(row);
            Button button = Button.builder(label, ignored -> performRowAction(row)).bounds(x, buttonY, ACTION_W, 18).build();
            button.active = row.canAccept() || row.canCancel();
            addRenderableWidget(button);
        }
    }

    private Component actionLabel(FreightBoardRow row) {
        if (row.canCancel()) {
            return Component.translatable("screen.colonylogistics.freight_board.cancel_short");
        }
        if (row.isPlayerTrade()) {
            return Component.translatable("screen.colonylogistics.freight_board.trade_terminal");
        }
        return Component.translatable("screen.colonylogistics.freight_board.accept");
    }

    private void performRowAction(FreightBoardRow row) {
        if (row.canCancel()) {
            PacketDistributor.sendToServer(new CancelFreightPayload(menu.boardPos(), row.contractId()));
        } else if (row.canAccept()) {
            PacketDistributor.sendToServer(new AcceptFreightPayload(menu.boardPos(), row.contractId()));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xE0101010);
        drawPanel(graphics, 8, HEADER_Y - 4, imageWidth - 16, DETAILS_Y - HEADER_Y + 2, 0x60202020);
        drawPanel(graphics, 8, DETAILS_Y - 4, imageWidth - 16, imageHeight - DETAILS_Y, 0x60303030);
        renderScrollbar(graphics);
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

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFFFFF, false);
        List<FreightBoardRow> rows = filteredRows();
        int total = menu.rows().size();
        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.filter_count", rows.size(), total), 376, 8, 0xAAAAAA, false);

        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.route"), ROUTE_X, HEADER_Y, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.cargo"), CARGO_X, HEADER_Y, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.status"), STATUS_X, HEADER_Y, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.assignee"), ASSIGNEE_X, HEADER_Y, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.reward"), REWARD_X, HEADER_Y, 0xD0D0D0, false);

        if (rows.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.empty_filtered"), 10, ROW_START_Y, 0xAAAAAA, false);
            renderSelectedDetails(graphics, Optional.empty());
            return;
        }

        clampScrollOffset();
        ensureSelectedRowExists();
        int from = scrollOffset + 1;
        int to = Math.min(rows.size(), scrollOffset + VISIBLE_ROWS);
        graphics.drawString(font, Component.literal(from + "-" + to + " / " + rows.size()), 428, 49, 0xAAAAAA, false);
        if (rows.size() > VISIBLE_ROWS) graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.scroll_hint"), 354, DETAILS_Y - 12, 0x888888, false);

        int y = ROW_START_Y;
        int visibleCount = Math.min(VISIBLE_ROWS, Math.max(0, rows.size() - scrollOffset));
        for (int i = 0; i < visibleCount; i++) {
            FreightBoardRow row = rows.get(scrollOffset + i);
            if (row.contractId().equals(selectedContractId)) graphics.fill(8, y - 3, imageWidth - ACTION_W - 20, y + 12, 0x50FFFFFF);

            String route = row.isPlayerTrade()
                    ? Component.translatable("screen.colonylogistics.freight_board.route.player_trade", row.originColonyId()).getString()
                    : row.originColonyId() + " -> " + row.destinationColonyId();
            String cargo = row.isPlayerTrade()
                    ? ClientDisplayNames.itemPhrase(row.cargoName())
                    : ClientDisplayNames.deliveryUnit(row.deliveryUnitType()) + " " + ClientDisplayNames.itemName(row.cargoName());
            if (!"-".equals(row.containerText())) cargo = cargo + " [" + ClientDisplayNames.containerText(row.containerText()) + "]";
            String reward = row.rewardAmount() + " " + ClientDisplayNames.itemName(row.currencyId());
            int color = (row.canAccept() || row.canCancel()) ? 0xFFFFFF : statusColor(row.status());
            graphics.drawString(font, trim(route, 10), ROUTE_X, y, color, false);
            graphics.drawString(font, trim(cargo, 29), CARGO_X, y, color, false);
            graphics.drawString(font, trim(statusComponent(row.status()).getString(), 10), STATUS_X, y, color, false);
            graphics.drawString(font, trim(assigneeComponent(row).getString(), 6), ASSIGNEE_X, y, color, false);
            graphics.drawString(font, trim(reward, 11), REWARD_X, y, color, false);
            y += ROW_HEIGHT;
        }
        renderSelectedDetails(graphics, selectedRow());
    }

    private void renderSelectedDetails(GuiGraphics graphics, Optional<FreightBoardRow> selected) {
        int y = DETAILS_Y;
        graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.details"), 10, y, 0xFFFFFF, false);
        y += 12;
        if (selected.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.colonylogistics.freight_board.details.empty"), 10, y, 0xAAAAAA, false);
            return;
        }
        FreightBoardRow row = selected.get();
        String source = ClientDisplayNames.source(row);
        String route = row.originColonyId() + " -> " + row.destinationColonyId();
        String reward = row.rewardAmount() + " " + ClientDisplayNames.itemName(row.currencyId());
        List<String> lines = new ArrayList<>();
        lines.add(Component.translatable("screen.colonylogistics.freight_board.details.id", row.shortContractId(), source, statusComponent(row.status()).getString()).getString());
        lines.add(Component.translatable("screen.colonylogistics.freight_board.details.route", route, row.originPositionText() + " -> " + row.destinationPositionText()).getString());
        lines.add(Component.translatable("screen.colonylogistics.freight_board.details.cargo", ClientDisplayNames.itemPhrase(row.cargoName()), "-".equals(row.containerText()) ? "-" : ClientDisplayNames.containerText(row.containerText())).getString());
        lines.add(Component.translatable("screen.colonylogistics.freight_board.details.reward", reward, row.difficulty(), deadlineText(row.expiresGameTime())).getString());
        lines.add(Component.translatable("screen.colonylogistics.freight_board.details.assignee", assigneeComponent(row).getString()).getString());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (String wrapped : wrapToWidth(line, imageWidth - 24, 2)) {
                graphics.drawString(font, wrapped, 10, y, i == 0 ? statusColor(row.status()) : 0xD0D0D0, false);
                y += 9;
            }
        }
    }

    private void renderScrollbar(GuiGraphics graphics) {
        List<FreightBoardRow> rows = filteredRows();
        if (rows.size() <= VISIBLE_ROWS) return;
        int trackX = leftPos + imageWidth - 14;
        int trackTop = topPos + ROW_START_Y - 2;
        int trackHeight = VISIBLE_ROWS * ROW_HEIGHT;
        graphics.fill(trackX, trackTop, trackX + 4, trackTop + trackHeight, 0x60303030);
        int maxOffset = maxScrollOffset();
        int thumbHeight = Math.max(18, trackHeight * VISIBLE_ROWS / rows.size());
        int travel = Math.max(1, trackHeight - thumbHeight);
        int thumbTop = trackTop + travel * scrollOffset / Math.max(1, maxOffset);
        graphics.fill(trackX, thumbTop, trackX + 4, thumbTop + thumbHeight, 0xC0C0C0C0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selectVisibleRow(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean selectVisibleRow(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        if (localX < 8 || localX > imageWidth - ACTION_W - 16 || localY < ROW_START_Y || localY >= ROW_START_Y + VISIBLE_ROWS * ROW_HEIGHT) return false;
        int index = scrollOffset + (localY - ROW_START_Y) / ROW_HEIGHT;
        List<FreightBoardRow> rows = filteredRows();
        if (index < 0 || index >= rows.size()) return false;
        selectedContractId = rows.get(index).contractId();
        rememberState();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (filteredRows().size() <= VISIBLE_ROWS) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        int oldOffset = scrollOffset;
        if (scrollY > 0.0D) scrollOffset--; else if (scrollY < 0.0D) scrollOffset++;
        clampScrollOffset();
        if (oldOffset != scrollOffset) {
            rememberState();
            rebuildFreightBoardWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private List<FreightBoardRow> filteredRows() { return menu.rows().stream().filter(activeFilter::matches).toList(); }
    private int maxScrollOffset() { return Math.max(0, filteredRows().size() - VISIBLE_ROWS); }
    private void clampScrollOffset() { scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset())); }
    private Optional<FreightBoardRow> selectedRow() { return filteredRows().stream().filter(row -> row.contractId().equals(selectedContractId)).findFirst(); }

    private void ensureSelectedRowExists() {
        List<FreightBoardRow> rows = filteredRows();
        if (rows.isEmpty()) { selectedContractId = null; return; }
        boolean selectedStillVisible = selectedContractId != null && rows.stream().anyMatch(row -> row.contractId().equals(selectedContractId));
        if (!selectedStillVisible) selectedContractId = rows.get(Math.min(scrollOffset, rows.size() - 1)).contractId();
    }

    private void rememberState() {
        rememberedFilter = activeFilter;
        rememberedScrollOffset = scrollOffset;
        rememberedSelectedContractId = selectedContractId;
    }

    private String deadlineText(long gameTime) {
        return gameTime <= 0L ? Component.translatable("screen.colonylogistics.freight_board.no_deadline").getString() : Long.toString(gameTime);
    }

    private Component statusComponent(ContractStatus status) { return Component.translatable("screen.colonylogistics.freight_board.status." + status.name().toLowerCase(Locale.ROOT)); }

    private Component assigneeComponent(FreightBoardRow row) {
        String key = switch (row.assigneeText()) { case "SELF" -> "self"; case "OTHER" -> "other"; default -> "none"; };
        return Component.translatable("screen.colonylogistics.freight_board.assignee." + key);
    }

    private int statusColor(ContractStatus status) {
        return switch (status) { case COMPLETED -> 0x80FF80; case FAILED, EXPIRED, CANCELLED -> 0xFF8080; case ACCEPTED, PICKED_UP, DELIVERED -> 0xFFE080; default -> 0xFFFFFF; };
    }

    private String trim(String value, int max) {
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 3)) + "...";
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
            if (fit == remaining.length()) { result.add(remaining); break; }
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

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        if (localX < 8 || localX > imageWidth - ACTION_W - 16 || localY < ROW_START_Y || localY >= ROW_START_Y + VISIBLE_ROWS * ROW_HEIGHT) return;
        int index = scrollOffset + (localY - ROW_START_Y) / ROW_HEIGHT;
        List<FreightBoardRow> rows = filteredRows();
        if (index < 0 || index >= rows.size()) return;
        FreightBoardRow row = rows.get(index);
        graphics.renderComponentTooltip(font, List.of(
                Component.translatable("screen.colonylogistics.freight_board.tooltip.contract", row.contractId().toString()),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.type_status", ClientDisplayNames.source(row), statusComponent(row.status()).getString()),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.route", row.originColonyId(), row.destinationColonyId()),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.position", row.originPositionText(), row.destinationPositionText()),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.cargo", ClientDisplayNames.itemPhrase(row.cargoName())),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.container", ClientDisplayNames.containerText(row.containerText())),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.reward", row.rewardAmount(), ClientDisplayNames.itemName(row.currencyId())),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.difficulty_deadline", row.difficulty(), deadlineText(row.expiresGameTime())),
                Component.translatable("screen.colonylogistics.freight_board.tooltip.assignee", assigneeComponent(row).getString())
        ), mouseX, mouseY);
    }

    /*
     * Filter tab widths are manual. If translated labels clip, increase only that tab's width.
     * The total tab width should remain below imageWidth - 16.
     */
    private enum BoardFilter {
        ALL("all", 42) { @Override boolean matches(FreightBoardRow row) { return true; } },
        OPEN("open", 48) { @Override boolean matches(FreightBoardRow row) { return row.status() == ContractStatus.OPEN; } },
        MINE("mine", 48) { @Override boolean matches(FreightBoardRow row) { return row.assignedToViewer() || row.activeForViewer(); } },
        INVENTORY("inventory", 68) { @Override boolean matches(FreightBoardRow row) { return row.isFreight() && row.deliveryUnitType() == DeliveryUnitType.INVENTORY_ITEM; } },
        CONTAINER("container", 76) { @Override boolean matches(FreightBoardRow row) { return row.isFreight() && row.deliveryUnitType() == DeliveryUnitType.CONTAINER_MULTIBLOCK; } },
        TRADE("trade", 52) { @Override boolean matches(FreightBoardRow row) { return row.isPlayerTrade(); } },
        HISTORY("history", 60) { @Override boolean matches(FreightBoardRow row) { return row.status() == ContractStatus.COMPLETED || row.status() == ContractStatus.CANCELLED || row.status() == ContractStatus.EXPIRED || row.status() == ContractStatus.FAILED; } };

        private final String key;
        private final int width;
        BoardFilter(String key, int width) { this.key = key; this.width = width; }
        abstract boolean matches(FreightBoardRow row);
        Component label() { return Component.translatable("screen.colonylogistics.freight_board.filter." + key); }
        int width() { return width; }
    }
}
