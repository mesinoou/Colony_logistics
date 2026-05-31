# Phase 16.21 - Container spawn fallback positions and GUI close-on-action

## Goals

- Keep the early single-player Container Dock delivery test flow from failing on a cluttered test blueprint pad with `NO_SPACE`.
- Close the active GUI whenever a server-side GUI action cannot be completed, so the player is forced to reopen a fresh snapshot instead of continuing from stale rows/buttons.

## Changes

### Container spawn fallback

`ContainerDockService.spawnForAcceptedContract(...)` no longer trusts only the GUI-provided suggested core position.

It now calls `findAvailableCorePos(...)`, which tries:

1. the GUI-suggested preferred position;
2. nearby horizontal offsets around the dock;
3. a higher vertical layer for cluttered blueprint floors;
4. a second local ring for small/medium test pads.

Candidates are only accepted if they remain within the dock delivery range and the full container footprint can be replaced.

`ContainerDockBlock` also asks the service for the first available suggested position when building the GUI snapshot.

### GUI close behavior

The following C2S GUI actions now close the container screen after any handled failure as well as on success:

- `SpawnContainerPayload`
- `DeliverContainerPayload`
- `SetDockModePayload`
- `AcceptFreightPayload`

This avoids stale GUI snapshots after contract state, player distance, dock mode, or available blocks have changed.

## Test flow

1. Accept a container freight contract from the Freight Board.
2. Open the Container Dock.
3. Press Spawn.
4. If the preferred footprint is blocked, the service should try nearby positions before returning `NO_SPACE`.
5. After any result, the GUI should close.
6. Reopen the Dock to refresh the nearby container list.
7. Deliver the generated container.
