# Phase 17.8.7 - Container Dock one-block Dock-side shift

## Purpose

Phase 17.8.6 mostly matched the in-game Container Dock mock-up, but the generated containers were still one block too far from the Container Dock/control side. This phase keeps the successful orientation and indoor-only behavior, then shifts the entire indoor candidate grid one block toward the Dock side.

## Changed behavior

- Container physical footprint remains 3 deep x 7 wide x 3 high.
- The 7-block side still runs along the Dock cargo lane.
- Contract spawning remains restricted to production-safe indoor pads.
- Rotation-aware cargo direction remains unchanged.
- The first row of contract candidate core positions moves from local Z `3` to local Z `2`.
- The second row moves from local Z `11` to local Z `10`.

Nearest-first contract candidate order is now:

```text
(5, 2, 2)
(9, 2, 2)
(13, 2, 2)
(5, 2, 10)
(9, 2, 10)
(13, 2, 10)
```

These offsets are still interpreted in the Container Dock cargo-local coordinate system, so rotated buildings should receive the same visual shift toward their own Dock/control side.

## Test notes

Use:

```text
/colonylogistics container candidates <dock> standard
```

Expected first candidates for a level 5 Dock are the local offsets above, with `contractSafe=true`. Then spawn contract containers from the Dock GUI and confirm the entire row has shifted one block toward the Dock/control side while maintaining one-block gaps.

## Next

Dock alignment can now be treated as good enough for the next phase. The recommended next target is reward currency hardening and multiplayer pre-test cleanup.
