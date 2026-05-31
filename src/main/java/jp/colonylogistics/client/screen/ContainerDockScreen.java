package jp.colonylogistics.client.screen;

import jp.colonylogistics.menu.ContainerDockMenu;
import jp.colonylogistics.menu.DockContainerRow;
import jp.colonylogistics.menu.DockContractRow;
import jp.colonylogistics.network.DeliverContainerPayload;
import jp.colonylogistics.network.SpawnContainerPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Container Dock UI with wide panels, per-panel scroll, and full-row tooltips. */
public class ContainerDockScreen extends AbstractContainerScreen<ContainerDockMenu> {
    /*
     * PHASE 17.4 MANUAL LAYOUT GUIDE
     *
     * Panel model:
     * - LEFT_* controls accepted container jobs and Spawn buttons.
     * - RIGHT_* controls nearby container diagnostics and Deliver buttons.
     * - ROW_START_Y is shared by both panels.
     * - CONTRACT_ROW_HEIGHT / CONTAINER_ROW_HEIGHT should be tall enough for the
     *   three short text lines drawn in each row.
     *
     * Safe manual tuning:
     * - If left/right panels overlap: increase RIGHT_X or reduce LEFT_W.
     * - If the right panel runs off-screen: reduce RIGHT_W or imageWidth.
     * - If Spawn/Deliver buttons overlap text: increase panel width or move button math in rebuildButtons().
     * - If vertical rows overlap: increase row height or reduce visible row count.
     * - If too few rows are visible: increase imageHeight and then VISIBLE_*_ROWS.
     */
    private static final int CONTRACT_ROW_HEIGHT = 30;    // Height for one accepted-contract row.
    private static final int CONTAINER_ROW_HEIGHT = 30;   // Height for one nearby-container row.
    private static final int VISIBLE_CONTRACT_ROWS = 5;   // Left panel visible rows before scroll.
    private static final int VISIBLE_CONTAINER_ROWS = 5;  // Right panel visible rows before scroll.
    private static final int ROW_START_Y = 58;            // First row Y for both panels.
    private static final int LEFT_X = 12;                 // Left panel text X.
    private static final int LEFT_W = 220;                // Left panel width including button area.
    private static final int RIGHT_X = 235;               // Right panel text X.
    private static final int RIGHT_W = 240;               // Right panel width including button area.

    private int contractScrollOffset = 0;
    private int containerScrollOffset = 0;

    public ContainerDockScreen(ContainerDockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Outer GUI size. Keep this near 500 unless text becomes unreadable again.
        // If you widen this, consider increasing RIGHT_W before moving RIGHT_X.
        this.imageWidth = 470;
        this.imageHeight = 246;

        // Hide vanilla inventory label; this screen has no player inventory slots.
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        clampScrollOffsets();

        // Spawn button is anchored to the right edge of the left panel.
        // If button/text overlap, adjust LEFT_W first, then this "- 58" anchor.
        int spawnX = leftPos + LEFT_X + LEFT_W - 58;
        List<DockContractRow> contractRows = menu.contractRows();
        int visibleContracts = Math.min(VISIBLE_CONTRACT_ROWS, Math.max(0, contractRows.size() - contractScrollOffset));
        for (int i = 0; i < visibleContracts; i++) {
            DockContractRow row = contractRows.get(contractScrollOffset + i);
            addRenderableWidget(Button.builder(Component.translatable("screen.colonylogistics.container_dock.spawn"), button -> spawn(row))
                    .bounds(spawnX, topPos + ROW_START_Y + i * CONTRACT_ROW_HEIGHT + 5, 50, 18)
                    .build());
        }

        // Deliver button is anchored to the right edge of the right panel.
        // If button/text overlap, adjust RIGHT_W first, then this "- 58" anchor.
        int deliverX = leftPos + RIGHT_X + RIGHT_W - 58;
        List<DockContainerRow> containerRows = menu.containerRows();
        int visibleContainers = Math.min(VISIBLE_CONTAINER_ROWS, Math.max(0, containerRows.size() - containerScrollOffset));
        for (int i = 0; i < visibleContainers; i++) {
            DockContainerRow row = containerRows.get(containerScrollOffset + i);
            Button button = Button.builder(Component.translatable("screen.colonylogistics.container_dock.deliver"), b -> deliver(row))
                    .bounds(deliverX, topPos + ROW_START_Y + i * CONTAINER_ROW_HEIGHT + 5, 50, 18)
                    .build();
            button.active = row.deliverable();
            addRenderableWidget(button);
        }
    }

    private void spawn(DockContractRow row) {
        PacketDistributor.sendToServer(new SpawnContainerPayload(row.contractId(), menu.dockPos(), row.suggestedCorePos()));
    }

    private void deliver(DockContainerRow row) {
        PacketDistributor.sendToServer(new DeliverContainerPayload(menu.dockPos(), row.corePos()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xE0101010);
        drawPanel(graphics, LEFT_X - 4, 38, LEFT_W, imageHeight - 48, 0x60202020);
        drawPanel(graphics, RIGHT_X - 4, 38, RIGHT_W, imageHeight - 48, 0x60202020);
        renderPanelScrollbars(graphics);
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
        Component status = Component.translatable("screen.colonylogistics.container_dock.status_compact", menu.colonyId(), menu.dockLevel());
        graphics.drawString(font, status, 8, 24, menu.usable() ? 0x80FF80 : 0xFF8080, false);

        graphics.drawString(font, Component.translatable("screen.colonylogistics.container_dock.accepted_jobs"), LEFT_X, 42, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("screen.colonylogistics.container_dock.nearby_containers"), RIGHT_X, 42, 0xD0D0D0, false);
        renderCountHint(graphics, menu.contractRows().size(), contractScrollOffset, VISIBLE_CONTRACT_ROWS, LEFT_X + LEFT_W - 74, 42);
        renderCountHint(graphics, menu.containerRows().size(), containerScrollOffset, VISIBLE_CONTAINER_ROWS, RIGHT_X + RIGHT_W - 74, 42);

        renderContractRows(graphics);
        renderContainerRows(graphics);
    }

    private void renderCountHint(GuiGraphics graphics, int total, int offset, int visibleRows, int x, int y) {
        if (total > visibleRows) {
            int from = offset + 1;
            int to = Math.min(total, offset + visibleRows);
            graphics.drawString(font, Component.literal(from + "-" + to + "/" + total), x, y, 0x888888, false);
        }
    }

    private void renderContractRows(GuiGraphics graphics) {
        List<DockContractRow> rows = menu.contractRows();
        int y = ROW_START_Y;
        if (rows.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.colonylogistics.container_dock.no_jobs"), LEFT_X, y, 0xAAAAAA, false);
            return;
        }
        int visible = Math.min(VISIBLE_CONTRACT_ROWS, Math.max(0, rows.size() - contractScrollOffset));
        for (int i = 0; i < visible; i++) {
            DockContractRow row = rows.get(contractScrollOffset + i);
            graphics.fill(LEFT_X - 2, y - 2, LEFT_X + LEFT_W - 10, y + 26, 0x30101010);
            drawWrapped(graphics, row.originColonyId() + " -> " + row.destinationColonyId() + "  " + ClientDisplayNames.itemName(row.cargoName()), LEFT_X, y, 138, 0xFFFFFF, 1);
            drawWrapped(graphics, ClientDisplayNames.containerText(row.containerText()), LEFT_X, y + 10, 118, 0xD0D0D0, 1);
            drawWrapped(graphics, row.rewardAmount() + " " + ClientDisplayNames.itemName(row.currencyId()), LEFT_X + 104, y + 10, 58, 0xD0D0D0, 1);
            Component progress = row.requiredContainerCount() <= 1
                    ? Component.empty()
                    : Component.translatable("screen.colonylogistics.container_dock.spawn_progress", row.spawnedContainerCount(), row.requiredContainerCount());
            drawWrapped(graphics, Component.translatable("screen.colonylogistics.container_dock.spawn_line", row.suggestedCorePos().toShortString(), progress).getString(), LEFT_X, y + 20, 160, 0x9A9A9A, 1);
            y += CONTRACT_ROW_HEIGHT;
        }
    }

    private void renderContainerRows(GuiGraphics graphics) {
        List<DockContainerRow> rows = menu.containerRows();
        int y = ROW_START_Y;
        if (rows.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.colonylogistics.container_dock.no_containers"), RIGHT_X, y, 0xAAAAAA, false);
            return;
        }
        int visible = Math.min(VISIBLE_CONTAINER_ROWS, Math.max(0, rows.size() - containerScrollOffset));
        for (int i = 0; i < visible; i++) {
            DockContainerRow row = rows.get(containerScrollOffset + i);
            int color = row.deliverable() ? 0x80FF80 : 0xFFB060;
            graphics.fill(RIGHT_X - 2, y - 2, RIGHT_X + RIGHT_W - 10, y + 26, 0x30101010);
            String line1 = ClientDisplayNames.statusToken(row.deliveryStatus()) + " " + String.format(Locale.ROOT, "%.1f", row.distance()) + "m  C:" + row.shortContractId() + (row.batchText().isEmpty() ? "" : " #" + row.batchText());
            String line2 = ClientDisplayNames.statusToken(row.contractStatus()) + "  " + row.originColonyId() + "->" + row.destinationColonyId() + "  " + ClientDisplayNames.containerSize(row.size()) + "/" + ClientDisplayNames.containerWeight(row.weightClass());
            String line3 = Component.translatable("screen.colonylogistics.container_dock.container_line_destination", trim(ClientDisplayNames.itemName(row.cargoName()), 12), row.destinationDockPos().toShortString()).getString();
            if (!row.issueHint().isBlank()) {
                line3 = trim(line3 + "  " + row.issueHint(), 42);
            }
            drawWrapped(graphics, line1, RIGHT_X, y, 150, color, 1);
            drawWrapped(graphics, line2, RIGHT_X, y + 10, 170, 0xD0D0D0, 1);
            drawWrapped(graphics, line3, RIGHT_X, y + 20, 170, 0xB0B0B0, 1);
            y += CONTAINER_ROW_HEIGHT;
        }
    }

    private void renderPanelScrollbars(GuiGraphics graphics) {
        renderScrollbar(graphics, LEFT_X + LEFT_W - 10, ROW_START_Y - 2, VISIBLE_CONTRACT_ROWS * CONTRACT_ROW_HEIGHT, menu.contractRows().size(), contractScrollOffset, VISIBLE_CONTRACT_ROWS);
        renderScrollbar(graphics, RIGHT_X + RIGHT_W - 10, ROW_START_Y - 2, VISIBLE_CONTAINER_ROWS * CONTAINER_ROW_HEIGHT, menu.containerRows().size(), containerScrollOffset, VISIBLE_CONTAINER_ROWS);
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
        if (inside(localX, localY, LEFT_X - 4, ROW_START_Y - 4, LEFT_W, VISIBLE_CONTRACT_ROWS * CONTRACT_ROW_HEIGHT + 8)
                && menu.contractRows().size() > VISIBLE_CONTRACT_ROWS) {
            contractScrollOffset += scrollY > 0 ? -1 : 1;
            clampScrollOffsets();
            rebuildButtons();
            return true;
        }
        if (inside(localX, localY, RIGHT_X - 4, ROW_START_Y - 4, RIGHT_W, VISIBLE_CONTAINER_ROWS * CONTAINER_ROW_HEIGHT + 8)
                && menu.containerRows().size() > VISIBLE_CONTAINER_ROWS) {
            containerScrollOffset += scrollY > 0 ? -1 : 1;
            clampScrollOffsets();
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void clampScrollOffsets() {
        contractScrollOffset = Math.max(0, Math.min(contractScrollOffset, Math.max(0, menu.contractRows().size() - VISIBLE_CONTRACT_ROWS)));
        containerScrollOffset = Math.max(0, Math.min(containerScrollOffset, Math.max(0, menu.containerRows().size() - VISIBLE_CONTAINER_ROWS)));
    }

    private boolean inside(int x, int y, int relX, int relY, int w, int h) {
        return x >= relX && x < relX + w && y >= relY && y < relY + h;
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        if (inside(localX, localY, LEFT_X - 4, ROW_START_Y - 4, LEFT_W, VISIBLE_CONTRACT_ROWS * CONTRACT_ROW_HEIGHT + 8)) {
            int index = contractScrollOffset + Math.max(0, localY - ROW_START_Y) / CONTRACT_ROW_HEIGHT;
            if (index >= 0 && index < menu.contractRows().size()) {
                DockContractRow row = menu.contractRows().get(index);
                graphics.renderComponentTooltip(font, List.of(
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.contract", row.contractId().toString()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.route", row.originColonyId(), row.destinationColonyId()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.cargo", ClientDisplayNames.itemName(row.cargoName())),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.container", ClientDisplayNames.containerText(row.containerText())),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.reward", row.rewardAmount(), ClientDisplayNames.itemName(row.currencyId())),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.suggested_core", row.suggestedCorePos().toShortString()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.progress", row.spawnedContainerCount(), row.requiredContainerCount(), row.deliveredContainerCount(), row.requiredContainerCount())
                ), mouseX, mouseY);
            }
        } else if (inside(localX, localY, RIGHT_X - 4, ROW_START_Y - 4, RIGHT_W, VISIBLE_CONTAINER_ROWS * CONTAINER_ROW_HEIGHT + 8)) {
            int index = containerScrollOffset + Math.max(0, localY - ROW_START_Y) / CONTAINER_ROW_HEIGHT;
            if (index >= 0 && index < menu.containerRows().size()) {
                DockContainerRow row = menu.containerRows().get(index);
                graphics.renderComponentTooltip(font, List.of(
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.container_contract", row.shortContainerId(), row.contractId().toString(), row.batchText().isEmpty() ? "-" : row.batchText()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.status", ClientDisplayNames.statusToken(row.deliveryStatus()), ClientDisplayNames.statusToken(row.contractStatus()), String.format(Locale.ROOT, "%.1f", row.distance())),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.route", row.originColonyId(), row.destinationColonyId()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.size_weight", ClientDisplayNames.containerSize(row.size()), ClientDisplayNames.containerWeight(row.weightClass())),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.core", row.corePos().toShortString()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.origin_dock", row.originDockPos().toShortString()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.destination_dock", row.destinationDockPos().toShortString()),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.cargo", ClientDisplayNames.itemName(row.cargoName())),
                        Component.translatable("screen.colonylogistics.container_dock.tooltip.hint", row.issueHint())
                ), mouseX, mouseY);
            }
        }
    }

    private void drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color, int maxLines) {
        List<String> lines = wrapToWidth(text, width, maxLines);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), x, y + i * 9, color, false);
        }
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
                while (!line.isEmpty() && font.width(line + "...") > width) {
                    line = line.substring(0, line.length() - 1);
                }
                result.add(line + "...");
                remaining = "";
            } else {
                result.add(line);
            }
        }
        if (result.isEmpty()) result.add("");
        return result;
    }

    private String trim(String value, int max) {
        if (value == null) return "";
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }
}
