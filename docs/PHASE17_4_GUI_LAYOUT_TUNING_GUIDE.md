# Phase 17.4 - GUI layout tuning guide

This phase does not significantly change gameplay. It adds source comments and layout guides so manual GUI tuning is safer.

## Key files

### Logistics Office freight view

File:

`src/main/java/jp/colonylogistics/client/screen/FreightBoardScreen.java`

Important constants:

- `imageWidth`, `imageHeight`: outer GUI size.
- `ROW_HEIGHT`: height of each contract row.
- `VISIBLE_ROWS`: number of rows shown before mouse-wheel scrolling.
- `TAB_Y`, `HEADER_Y`, `ROW_START_Y`: vertical layout anchors.
- `ROUTE_X`, `CARGO_X`, `STATUS_X`, `ASSIGNEE_X`, `REWARD_X`: list column X positions.
- `ACTION_W`: width of accept/terminal action buttons.
- `BoardFilter(... width)`: filter tab widths.

### Container Dock

File:

`src/main/java/jp/colonylogistics/client/screen/ContainerDockScreen.java`

Important constants:

- `imageWidth`, `imageHeight`: outer GUI size.
- `LEFT_X`, `LEFT_W`: accepted-contract panel.
- `RIGHT_X`, `RIGHT_W`: nearby-container panel.
- `CONTRACT_ROW_HEIGHT`, `CONTAINER_ROW_HEIGHT`: row heights.
- `VISIBLE_CONTRACT_ROWS`, `VISIBLE_CONTAINER_ROWS`: visible rows before scrolling.

Button positions are calculated from panel width:

- Spawn: `LEFT_X + LEFT_W - 58`
- Deliver: `RIGHT_X + RIGHT_W - 58`

### Trade Terminal

Screen file:

`src/main/java/jp/colonylogistics/client/screen/TradeTerminalScreen.java`

Slot/menu file:

`src/main/java/jp/colonylogistics/menu/TradeTerminalMenu.java`

Important screen constants:

- `LEFT_PANEL_*`: request/reward/create panel.
- `OPEN_PANEL_*`: open trades panel.
- `HISTORY_PANEL_*`: trade history panel.
- `INVENTORY_PANEL_*`: drawn background for player inventory.

Important menu constants:

- `REQUEST_SLOT_X/Y`
- `REWARD_SLOT_X/Y`
- `startX`, `startY` for the actual player inventory slots.

If inventory slots are visually misaligned, update BOTH:

1. `TradeTerminalMenu.startX/startY`
2. `TradeTerminalScreen.INVENTORY_PANEL_X/Y/W/H`

## Rule of thumb

- For overlap: increase row height or reduce visible row count.
- For clipping: increase panel width, reduce text width, or rely on hover tooltip.
- For excessive width: reduce `imageWidth`, then shift right-side columns left.
- For slot mismatch: menu slot constants control real slot positions; screen constants only draw backgrounds.
