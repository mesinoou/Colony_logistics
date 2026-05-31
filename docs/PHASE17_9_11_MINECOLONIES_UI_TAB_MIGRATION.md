# Phase 17.9.11 - MineColonies UI Tab Migration

This phase moves Colony Logistics building controls behind MineColonies' standard building window.

## Why

The previous Build Tool right-click path was unreliable because core block interaction is owned by MineColonies hut behavior. Instead of trying to override one more interaction combination, Colony Logistics now registers a MineColonies building module tab for each logistics building.

## Interaction model

- Right-click a Logistics Office / Container Dock / Trade Terminal core block: opens the MineColonies building UI.
- Use the Colony Logistics tab inside that MineColonies UI.
- Press **Open Logistics UI** to open the existing Colony Logistics screen for that building.
- Sneak/right-click and other MineColonies-owned interactions are no longer repurposed by Colony Logistics.

## Buildings with tabs

- Logistics Office: opens the colony-bound logistics contracts screen.
- Container Dock: opens the container generation / delivery screen.
- Trade Terminal: opens the player-trade escrow screen.

## Implementation notes

- `ModMineColoniesBuildings` now registers one no-op module producer per Colony Logistics building.
- `ColonyLogisticsBuildingModuleView` provides the tab title, icon, and BlockUI window.
- `ColonyLogisticsMineColoniesTabWindow` sends `OpenColonyLogisticsMenuPayload` when the tab button is clicked.
- Existing NeoForge menu/screen classes are reused behind the tab to reduce UI regression risk.
- Direct block right-click no longer opens the Colony Logistics screen. This keeps MineColonies upgrade, repair, build and inventory flows as the primary building UI.

## Compatibility

The standalone Freight Board remains removed. The Logistics Office tab still opens the contract screen internally backed by the existing FreightBoard menu/screen classes.
