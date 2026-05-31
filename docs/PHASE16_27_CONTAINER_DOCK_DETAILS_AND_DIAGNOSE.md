# Phase 16.27 - Container Dock detail view and diagnose command

Goal: make the Container Dock workflow easier to test after Create Aeronautics entity conversion and multiblock restoration.

## Changes

- Container Dock GUI now shows more details for every recognized nearby container:
  - delivery status (`OK`, `WRONG_DOCK`, `TOO_FAR`, `STATUS_*`, etc.)
  - distance from the Dock
  - short contract id
  - manifest route
  - actual core position
  - current saved contract status
  - cargo id
  - expected container size / weight class
  - destination Dock position
  - diagnostic hint
- Only `OK` rows have an active Deliver button.
- The right-hand container panel is wider/taller to fit diagnostics.
- Added one shared read-only analysis path in `ContainerDockService` so GUI and command diagnostics use the same status logic.
- Added `/colonylogistics container diagnose <dock>`.

## Commands

```mcfunction
/colonylogistics container inspect <core>
/colonylogistics container diagnose <dock>
```

`inspect` focuses on one container core and prints manifest data.

`diagnose` scans the Dock recognition radius and prints one line per nearby container core with deliverability status, contract status, expected size/weight, destination Dock, and a short hint.

## Expected test flow

1. Accept a container freight contract.
2. Spawn the container at the origin Dock.
3. Convert/move/restore the multiblock with Create Aeronautics.
4. Run `/colonylogistics container inspect <core>` to confirm the manifest survived.
5. Run `/colonylogistics container diagnose <destinationDock>` to confirm the Dock sees the container and why it can or cannot deliver it.
6. Open the destination Dock GUI and verify the right-hand row shows the same status and hint.
7. Deliver only when the row status is `OK`.

