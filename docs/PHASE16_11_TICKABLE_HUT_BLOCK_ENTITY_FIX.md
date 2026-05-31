# Phase 16.11 - Tickable Hut BlockEntity Fix

## Problem

Placing the Container Dock as a MineColonies hut caused a server tick crash:

```text
ClassCastException: jp.colonylogistics.dock.ContainerDockBlockEntity cannot be cast to com.minecolonies.api.tileentities.ITickable
```

MineColonies hut blocks inherit a ticker that casts their BlockEntity to `ITickable`.
Our custom logistics BlockEntities were valid Minecraft BlockEntities, but did not implement MineColonies' `ITickable` marker/interface.

## Fix

The following BlockEntities now implement `com.minecolonies.api.tileentities.ITickable` with a no-op `tick()` method:

- `ContainerDockBlockEntity`
- `TradeTerminalBlockEntity`

This keeps their custom GUI/state data while satisfying the inherited MineColonies hut ticker.

## Notes

The no-op tick is intentional. Logistics state is updated through MineColonies building sync, GUI payloads, commands, and service calls rather than per-tick BlockEntity logic.
