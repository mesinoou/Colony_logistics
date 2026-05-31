# Phase 17.9.10 - MineColonies building UI access path

Phase 17.9.8 attempted to expose the MineColonies building UI through sneak right-click on the Colony Logistics hut core blocks. In-game testing showed that sneak right-click is already used by MineColonies for the hut block inventory, so it is not a reliable upgrade/repair UI shortcut.

This phase keeps the existing interactions intact and adds a separate access path:

- Normal right-click on a Colony Logistics hut core opens the Colony Logistics screen.
  - Logistics Office: freight contract board.
  - Container Dock: container spawn/delivery screen.
  - Trade Terminal: player trade escrow screen.
- Sneak right-click is left to the inherited MineColonies behavior, including block inventory access.
- Right-clicking the core block while holding the Structurize/MineColonies Build Tool, without sneaking, delegates to the inherited MineColonies hut interaction and opens the standard MineColonies building UI used for upgrade/repair/building management.

The supported opener item ids are intentionally broad enough for development/test environments:

- `structurize:build_tool`
- `structurize:buildtool`
- `minecolonies:build_tool`
- `minecolonies:buildtool`

Implementation notes:

- The shared helper lives in `AbstractColonyLogisticsHutBlock`.
- `LogisticsOfficeBlock`, `ContainerDockBlock`, and `TradeTerminalBlock` all call the same helper before opening their Colony Logistics GUI.
- The helper calls `super.useWithoutItem(...)` so MineColonies' own client-side `building.openGui(player.isShiftKeyDown())` path is used with `shift=false`.
- No custom MineColonies packet or direct GUI class import was added.
- Sable dependency metadata remains as restored in Phase 17.9.9.1.
- Manual GUI layout constants were not touched.

Test plan:

1. Place/upgrade each Colony Logistics hut through Build Tool.
2. Normal right-click the core block and confirm the Colony Logistics screen opens.
3. Sneak right-click the core block and confirm the existing MineColonies inventory behavior is unchanged.
4. Hold the Structurize Build Tool, do not sneak, and right-click the core block.
5. Confirm the MineColonies building UI opens and upgrade/repair controls are reachable.
