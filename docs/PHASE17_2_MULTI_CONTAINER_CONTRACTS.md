# Phase 17.2 - Multi-container contract foundation

This phase adds the first implementation of multi-container freight contracts.

## Changes

- `LogisticsContract` now stores `requiredContainerCount`, `spawnedContainerCount`, and `deliveredContainerCount`.
- Container freight generation assigns 2-3 required containers depending on size.
- `ContainerManifest` now stores `batchIndex` and `batchCount`.
- Container Dock can spawn additional containers for a contract after the first spawn.
- Each delivered container increments contract progress.
- Reward/profile completion is granted only when all required containers are delivered.
- Container Dock rows and tooltips show spawn/delivery progress and batch numbers.

## Intended test flow

1. Accept a container contract from the Logistics Office board.
2. Open the origin Container Dock.
3. Spawn each required container.
4. Move each container with Create Aeronautics or manually for testing.
5. Deliver each container at the destination Dock.
6. Verify reward is only paid after the final container.
