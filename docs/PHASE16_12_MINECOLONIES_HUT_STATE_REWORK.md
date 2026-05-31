# Phase 16.12 - MineColonies Hut State Rework

## Problem

Build Tool creative instant placement could paste Colony Logistics hut blocks without a MineColonies building being registered behind them. In addition, Container Dock and Trade Terminal were implemented as MineColonies hut blocks that also provided their own custom BlockEntities. MineColonies hut anchors are expected to use MineColonies' shared `minecolonies:colonybuilding` BlockEntity, so keeping custom logistics BlockEntities on the hut anchors caused placement/ticking/lookup conflicts.

Observed symptoms included:

- Hut block placed, but right click reported that no MineColonies building could be found for the block.
- `minecolonies:colonybuilding` valid-block validation crashes.
- MineColonies hut ticker casting a custom logistics BlockEntity to `ITickable`.

## Fix

This phase makes the MineColonies hut anchor blocks MineColonies-owned again.

- `LogisticsOfficeBlock`, `ContainerDockBlock`, and `TradeTerminalBlock` now extend `AbstractColonyLogisticsHutBlock`.
- `ContainerDockBlock` and `TradeTerminalBlock` no longer implement `EntityBlock` and no longer create custom BlockEntities.
- `AbstractColonyLogisticsHutBlock` forces the MineColonies registration path during creative instant Build Tool placement.
- Per-building runtime state is stored in `LogisticsMarketSavedData` using `LogisticsBuildingKey(dimension, pos)`.

## New runtime state

Added:

- `LogisticsBuildingKey`
- `ResolvedLogisticsBuilding`
- `DockRuntimeState`
- `TradeTerminalRuntimeState`
- `SavedTradeTerminalContainer`

Dock mode is now stored by dimension + hut position. Trade Terminal setup slots are stored by dimension + hut position. This allows multiple Container Docks and multiple Trade Terminals in the same colony without overwriting each other.

## Updated systems

- Container Dock GUI now resolves MineColonies building state from level + position.
- Container Dock payloads now use level + dock position instead of `ContainerDockBlockEntity`.
- Container Dock service now uses level + dock position and SavedData mode.
- Trade Terminal GUI now uses a SavedData-backed two-slot container.
- Player trade create/deliver/cancel payloads now use level + terminal position instead of `TradeTerminalBlockEntity`.
- MineColonies sync writes dock/terminal runtime state into SavedData.

## Remaining cleanup

The old `ContainerDockBlockEntity` and `TradeTerminalBlockEntity` classes remain in the source tree for migration reference, but the hut blocks no longer create or depend on them. A later cleanup phase can remove their registrations after confirming no old worlds need migration support.
