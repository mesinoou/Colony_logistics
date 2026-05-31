# Phase 17.9.21 - Container Dock coarse-dirt pad alignment

## Summary

Multiplayer testing confirmed that the Phase 17.9.20 bundled collaborator blueprint pack was being loaded, but Container Dock contract containers still spawned outside the building. The test screenshot showed that the visible intended pads are the coarse-dirt rectangles inside the new Container Dock.

The reported resolver output was `cargoForward=SOUTH` while the screenshot's top side was NORTH. With the previous transform, the hardcoded local pad offsets were mirrored to the outside of the Dock. Phase 17.9.21 keeps the MineColonies resolve result, but changes how blueprint-local X is transformed for the scanned collaborator Dock.

## Fix

- Container Dock contract-safe pads are now centered on the scanned coarse-dirt markers.
- `offsetLocal(...)` maps blueprint-local `+X` to `cargoForward.getClockWise()` for the collaborator pack. The previous `getCounterClockWise()` mapping mirrored the pads outside the Dock in game.
- The second row Z offset was corrected from `6` to `8`, matching the coarse-dirt row centered at blueprint-local `z=16` with Hut anchor `z=8`.
- The bottom/south coarse-dirt row contains four markers, so the contract-safe list now has seven marker centers instead of six.

## New contract-safe core offsets

The Container Dock blueprint size is still `21 x 8 x 22` and the Hut anchor is still `(2,1,8)`. Core offsets from the Hut anchor are:

```text
(7,  2, -2)
(11, 2, -2)
(15, 2, -2)
(3,  2,  8)
(7,  2,  8)
(11, 2,  8)
(15, 2,  8)
```

These correspond to coarse-dirt marker centers:

```text
top/north row:    (9,6), (13,6), (17,6)
bottom/south row: (5,16), (9,16), (13,16), (17,16)
```

## Test command

Use:

```text
/colonylogistics container candidates <dockX> <dockY> <dockZ> standard
```

For the reported `cargoForward=SOUTH` placement, the first candidates should now be on the coarse-dirt rectangles inside the Dock, not outside the building.
