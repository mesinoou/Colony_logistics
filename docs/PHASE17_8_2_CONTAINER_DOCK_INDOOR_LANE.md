# Phase 17.8.2 - Container Dock indoor nearest-first lane

## Problem observed in-game

Phase 17.8.1 fixed container overlap and false success messages, but its deterministic safe lane placed the generated containers outside the visible Container Dock building. The in-game screenshot confirmed that a 3 deep x 7 wide x 3 high placeholder can fit on the open apron inside the Dock frame.

## Fix

Container spawning now tries a deterministic indoor lane before any client-provided position, TOML apron grid, or legacy fallback ring.

Primary indoor core offsets from the Container Dock Hut anchor are:

- First container: `(5, 2, 6)`
- Second container: `(5, 2, 10)`
- Third container: `(5, 2, 14)`

The 3 deep x 7 wide x 3 high container footprint means these occupy:

- X `2..8`, Z `5..7`
- X `2..8`, Z `9..11`
- X `2..8`, Z `13..15`

This keeps the first three generated containers inside the Phase 17.6 Dock frame and leaves one air block between each row.

## Candidate order

The order is now nearest-first from the Container Dock block:

1. Hardcoded indoor lane `(5,2,6)`, `(5,2,10)`, `(5,2,14)`, ...
2. A secondary indoor lane for larger Docks if the first lane is full
3. GUI/client suggested position
4. Configurable apron grid
5. Legacy side/top fallback rings

This means stale GUI payloads or old TOML files cannot prefer an exterior position while indoor space is available.

## Radius defaults

Because the primary lane is close to the Dock again, default and runtime minimum delivery/recognition radius return to `18.0` blocks. Existing worlds with a larger generated TOML value will continue to use the larger value until the config is edited or regenerated.

## Test commands

Use these in-game before and after spawning:

```text
/colonylogistics container candidates <dock> standard
/colonylogistics container candidates <dock> large
/colonylogistics container candidates <dock> heavy
```

Expected first usable offsets for an unobstructed Phase 17.6 Level 5 Dock are near:

```text
rel=(5,2,6)
rel=(5,2,10)
rel=(5,2,14)
```

If the screenshot placeholder blocks are still present, remove them before testing real generation because `hasSpaceForContainer` requires the entire multiblock volume to be replaceable air.
