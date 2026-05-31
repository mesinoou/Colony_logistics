# Phase 16.19 - Container Dock single-player test flow

Goal: make the Container Dock freight loop testable before Create / Create Aeronautics / Sable physical transport is required.

## Changes

- Added `testing.allowLoopbackContainerFreightForTesting`.
  - Default: `true`.
  - With one container-capable colony, generated container freight may route back to the same colony.
  - If a loopback colony has multiple docks, generated jobs prefer dock-to-dock routes inside that colony.
  - If it has one dock, generated jobs become same-dock loopback jobs.
- Added a focused development command:
  - `/colonylogistics container localtest <dock>` creates a SMALL same-dock container job.
  - `/colonylogistics container localtest <dock> <small|medium|large|heavy>` creates a same-dock job for the requested size.
- The localtest command is intended for early one-player testing:
  1. Create a contract pinned to one Container Dock.
  2. Accept it from the Freight Board.
  3. Open the same Dock and spawn the sealed container.
  4. Reopen the same Dock and deliver the nearby container.
  5. Confirm Freight Board / profile state updates.

## Normal generated container jobs

`/colonylogistics freight generatecontainers` now works with one container-capable colony when `allowLoopbackContainerFreightForTesting = true`.

For multi-colony or later balance testing, set:

```toml
[testing]
allowSelfDeliveryForTesting = false
allowLoopbackFreightForTesting = false
allowLoopbackContainerFreightForTesting = false
```

## Important

This phase does not implement Create / Aeronautics vehicle movement. It verifies the Container Dock service state machine: accepted job -> spawn sealed contract container -> detect deliverable container -> complete contract -> pay reward -> update carrier profile.
