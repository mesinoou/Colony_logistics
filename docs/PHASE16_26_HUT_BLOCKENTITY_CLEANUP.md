# Phase 16.26 - Hut BlockEntity cleanup

This phase assumes test worlds are recreated per phase, so no legacy world migration is required.

## Changes

- Removed the legacy `ContainerDockBlockEntity` source class.
- Removed the legacy `TradeTerminalBlockEntity` source class.
- Removed the legacy `colonylogistics:container_dock` and `colonylogistics:trade_terminal` BlockEntityType registrations.
- Kept `colonylogistics:freight_container_core` as the only Colony Logistics BlockEntityType in `ModBlockEntities`.
- Added `/colonylogistics minecolonies blockentity <pos>` to inspect the actual BlockEntity at a hut core.

## Expected hut behavior

The Colony Logistics hut blocks should use MineColonies' shared hut BlockEntity:

- `colonylogistics:logistics_office` -> `minecolonies:colonybuilding`
- `colonylogistics:container_dock` -> `minecolonies:colonybuilding`
- `colonylogistics:trade_terminal` -> `minecolonies:colonybuilding`

Container Dock and Trade Terminal runtime state remains in `LogisticsMarketSavedData` keyed by dimension + BlockPos.

## Game check

1. Create a fresh test world.
2. Place Logistics Office, Container Dock, and Trade Terminal through the MineColonies Build Tool.
3. Run:

```mcfunction
/colonylogistics minecolonies blockentity <hut core pos>
/colonylogistics minecolonies resolve <hut core pos>
```

4. Confirm the BlockEntity type is `minecolonies:colonybuilding`.
5. Confirm Container Dock and Trade Terminal custom GUIs still open.
6. Confirm Freight Board, Trade Terminal, parcel delivery, and container delivery still work.
