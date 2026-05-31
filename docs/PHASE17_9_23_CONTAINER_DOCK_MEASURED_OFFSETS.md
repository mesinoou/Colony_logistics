# Phase 17.9.23 - Container Dock measured multiplayer offsets

## Context

Phase 17.9.22 still failed to align generated containers with the collaborator
Container Dock pads in multiplayer. The final offsets were measured in-game using
the Dock resolved direction as the positive front axis.

## Offset convention

Offsets are expressed from the Hut / container center as:

- `localX`: right side is positive.
- `localZ`: the direction returned by `/colonylogistics minecolonies resolve <dock>` is positive.

## Contract spawn pads

The seven production indoor pads are now fixed to the measured values:

- `(7, 2)`
- `(11, 2)`
- `(15, 2)`
- `(3, -8)`
- `(7, -8)`
- `(11, -8)`
- `(15, -8)`

The old Phase 17.9.22 additional four-block front shift was removed because the
measured offsets already include the final desired position.

## Notes

The previous blueprint-footprint pre-filter is not used for these measured pads.
The placement path still performs the real runtime `builder.hasSpace(...)` check,
so blocked pads fail normally, but the measured pad list itself is no longer
trimmed before placement.
