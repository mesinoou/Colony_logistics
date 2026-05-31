# Phase 17.8.6 - Container Dock visual offset fix

## Problem

In-game testing after Phase 17.8.5 showed that contract containers no longer spawned outside the Container Dock, but the container footprint was still visibly offset from the manual mock-up. The generated footprint was effectively rotated across the apron: it appeared as 7 blocks across and 3 blocks deep, while the mock-up used the long side along the Dock cargo lane.

The user also confirmed that `SOUTH` corresponds to the screenshot-left cargo direction for the tested Dock.

## Fix

The physical container footprint remains the player-facing unified 3 x 7 x 3 size:

- depth: 3 blocks across the apron
- width: 7 blocks along the Dock cargo lane
- height: 3 blocks

`ContainerSize` keeps the existing depth/width/height meaning, while `ContainerMultiblockBuilder` now maps the 7-wide axis along the Dock cargo lane and the 3-deep axis across the apron. This makes the multiblock placement match the manual block mock-up instead of being rotated ninety degrees.

## Contract spawn grid

Contract spawning now uses a hardcoded indoor grid, ordered from the Dock-side row toward the deeper cargo row:

```text
(5, 2, 3)
(9, 2, 3)
(13, 2, 3)
(5, 2, 11)
(9, 2, 11)
(13, 2, 11)
```

For a Level 5 Dock, these centers produce 3-deep x 7-wide x 3-high containers with one block of air between columns and rows. The first three contract containers therefore align in the nearest row before the second row is used.

The candidate list is filtered by Phase 17.6 blueprint dimensions for the current building level, so lower-level Docks do not silently use positions outside their actual frame.

## Kept behavior

- Build Tool / Hut facing rotation handling from Phase 17.8.4 is preserved.
- Contract spawning remains indoor-only from Phase 17.8.5.
- Fallback rings remain visible for diagnostics, but they are not used for contract container spawning.
- GUI layout files are unchanged.
