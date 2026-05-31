# Phase 16.22 - Container Dock spawn offsets and recognition tuning

This phase makes Container Dock container spawn placement configurable for the current test blueprints and future production staging layouts.

## Current spawn candidate model

The Container Dock Hut block is treated as the origin. The current temporary Container Dock blueprint occupies a 5x5x4 area around that Hut block, so the defaults assume:

- `containerSpawnDockHalfX = 2`
- `containerSpawnDockHalfZ = 2`
- `containerSpawnDockOccupiedHeight = 4`

Container core positions are tried in this order:

1. The GUI/request preferred core position, if one is supplied.
2. A side ring around the Dock.
3. A top ring above the Dock occupied height.
4. A wider second side ring for cluttered test pads.

For each axis, the side ring core offset is calculated as:

```text
dock half extent + container half extent + horizontal gap + 1
```

With the default 5x5 Dock footprint and `containerSpawnHorizontalGap = 1`, the first side-ring core offsets are:

| Size | Dimensions | X-side core offset | Z-side core offset | Side core Y |
| --- | --- | ---: | ---: | ---: |
| SMALL | 3x3x3 | 5 | 5 | 2 |
| MEDIUM | 5x3x3 | 6 | 5 | 2 |
| LARGE | 5x5x5 | 6 | 6 | 3 |
| HEAVY | 7x5x5 | 7 | 6 | 3 |

This keeps the generated container footprint outside the Dock's 5x5 footprint instead of touching the boundary.

The top-ring core Y is calculated as:

```text
containerSpawnDockOccupiedHeight + containerSpawnTopGap + container half height
```

## Config keys

All keys are under `[dock]` in the common config.

```toml
containerRecognitionRadius = 12.0
containerSpawnDockHalfX = 2
containerSpawnDockHalfZ = 2
containerSpawnDockOccupiedHeight = 4
containerSpawnHorizontalGap = 1
containerSpawnBottomYOffset = 1
containerSpawnTopGap = 1
containerSpawnExtraRingGap = 2
```

`containerRecognitionRadius` controls how far the Container Dock GUI scans for sealed container cores. `deliveryRadius` still controls the actual delivery acceptance range.

## Notes for later implementation

When production Container Dock blueprints get a fixed staging pad, tune these offsets to match the staging pad instead of relying on the wider fallback ring. Create/Aeronautics integration can later replace or supplement this placement with physical container detection.
