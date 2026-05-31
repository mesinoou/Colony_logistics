# Phase 17.9.24 - Container Create Aeronautics Assembly Fix

## Problem

Phase 17.9.18 made runtime freight container blocks fully unbreakable by using negative destroy speed (`strength(-1.0f, ...)`). This prevented ordinary block breaking, but it also caused Create / Create Aeronautics assembly logic to treat the container blocks as non-assemblable.

## Fix

Runtime freight container blocks now use positive hardness while preserving multiplayer mining protection:

- `FreightContainerCoreBlock`
- `FreightContainerPartBlock`

Both blocks use:

```java
strength(50.0f, 3600000.0f)
```

They keep:

- `PushReaction.NORMAL` for contraption movement compatibility.
- High explosion resistance.
- `getDestroyProgress(...) == 0.0F` so ordinary survival mining does not progress.
- Empty `playerDestroy(...)` so vanilla destruction side effects and drops are not produced through normal player mining paths.

## Expected validation

1. Generate a freight container from Container Dock.
2. Confirm ordinary survival mining does not break the container.
3. Confirm Create / Create Aeronautics can assemble the container blocks into a moving contraption.
4. Confirm Colony Logistics delivery / cancel cleanup can still remove the runtime container.
