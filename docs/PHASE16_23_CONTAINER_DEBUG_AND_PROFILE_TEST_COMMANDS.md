# Phase 16.23 - Container Dock debug visibility and carrier profile test commands

This phase is focused on making the Create Aeronautics container test loop easier to debug.

## Container Dock GUI changes

The right-hand `Nearby containers` list now includes nearby Freight Container Core block entities even when they are not currently deliverable.

Each row carries a short delivery status:

- `OK`: the container is deliverable at this Dock.
- `INVALID_CONTRACT`: the manifest points at a contract that no longer exists.
- `STATUS_<status>`: the contract exists but is not in the expected `PICKED_UP` state.
- `WRONG_PLAYER`: the container is assigned to another carrier.
- `NOT_SEALED`: the manifest is not sealed.
- `WRONG_DOCK`: the manifest destination Dock does not match the Dock GUI being used.
- `TOO_FAR`: the container is visible in the recognition radius, but outside the delivery radius.

Only `OK` rows have an enabled Deliver button. Failed GUI actions still close the screen, so stale snapshots are not reused repeatedly.

## Debug commands

Inspect a container manifest after Create Aeronautics entity conversion and re-materialization:

```text
/colonylogistics container inspect <core>
```

Check Dock delivery range:

```text
/colonylogistics container range <dock> <core>
```

## Carrier profile test commands

These are test-only convenience commands for single-player development.

```text
/colonylogistics profile setlevel <1-5>
/colonylogistics profile addcompleted <count>
/colonylogistics profile reset
/colonylogistics profile me
```

`setlevel` adjusts completed jobs and reputation to the minimum thresholds for that level, so the value survives SavedData reloads.
