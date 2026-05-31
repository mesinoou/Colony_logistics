# Phase 17.8.5 - Container Dock contract spawn indoor-only fix

## Problem

Game testing showed that `/colonylogistics container candidates` could find valid indoor Container Dock positions, but spawning a contract container from the Container Dock GUI could still fall through to the older debug fallback ring and place a contract container outside the production building.

The reported candidate log for a rotated Dock used `dockForward=south`; in the user's screenshot, SOUTH corresponds to the left side of the image. The first two indoor positions were usable, the third close-lane position was blocked, and later fallback positions outside the Dock frame were still usable.

## Fix

Contract spawning now uses a separate indoor-only candidate list:

- real contract spawning calls `findAvailableContractCorePos(...)`
- GUI suggestions also use `findAvailableContractCorePos(...)`
- stale GUI payload core positions are ignored for real contract placement
- old apron/ring fallback candidates remain only for diagnostics and manual debugging

This prevents accepted contract containers from silently appearing outside the Container Dock building when one indoor pad is blocked.

## Indoor candidate order

The deterministic indoor list keeps the verified close lane first, then tries additional indoor overflow pads before any debug fallback candidate is shown:

1. `(5, 2, 6)`
2. `(5, 2, 10)`
3. `(0, 2, 11)`
4. `(0, 2, 15)`
5. `(5, 2, 14)`
6. `(13, 2, 6)`
7. `(13, 2, 10)`
8. `(13, 2, 14)`
9. `(5, 2, 18)`
10. `(13, 2, 18)`

The offsets are still transformed through the resolved Dock cargo direction, so rotated MineColonies/Build Tool buildings remain supported. For the reported test, `dockForward=south` means the local +Z side maps to the left side of the screenshot.

## Diagnostics

`/colonylogistics container candidates <dock> standard|large|heavy` now prints:

- `contractIndoorCandidates=<count>` in the header
- `contractSafe=true/false` per candidate

Only candidates marked `contractSafe=true` can be selected by contract spawning. If all contract-safe candidates are blocked, spawning returns `NO_SPACE` instead of placing the container outside the Dock.

## Unchanged

- Container size remains unified to 3×7×3 (D/W/H).
- The Container Dock blueprint and visual model are unchanged.
- Container placement and removal still use the manifest facing.
- Manual GUI layout files are untouched.
