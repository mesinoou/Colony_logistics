# Phase 17.9.22 - Container Dock front-side spawn shift

## Summary

Phase 17.9.21 fixed the collaborator Container Dock pad mirroring and placed the
base spawn markers on the coarse-dirt pad list. Multiplayer visual confirmation
then showed that the actual container core should be shifted four blocks toward
the Dock's resolved cargo-front direction.

## Changes

- Added `COLLABORATOR_DOCK_FRONT_SHIFT_BLOCKS = 4` in `ContainerDockService`.
- Kept the Phase 17.9.21 coarse-dirt marker list and ordering unchanged.
- Applied the four-block shift in world space after `offsetLocal(...)` resolves
  the marker position.
- The shift is applied only to indoor contract candidates, so the legacy debug
  fallback rings and config apron grid are not retuned.

## Expected diagnostics

For a Dock whose `/colonylogistics minecolonies resolve <dock>` reports
`cargoForward=SOUTH`, the contract-safe candidate positions should move four
blocks toward world SOUTH compared with Phase 17.9.21.

Use:

```mcfunction
/colonylogistics container candidates <dockX> <dockY> <dockZ> standard
```

The visible `rel=(x,y,z)` values should show the same X side as Phase 17.9.21,
with Z increased by 4 when the resolved cargo-forward direction is SOUTH.
