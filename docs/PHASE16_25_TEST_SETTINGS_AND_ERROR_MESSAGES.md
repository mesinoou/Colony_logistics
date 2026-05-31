# Phase 16.25 - Test Settings Commands and Container Error Messages

This phase keeps the Phase 16.24 Freight Board filters and the restored 16.22-style directory layout, then adds in-game test controls and clearer Container Dock failure messages.

## Goals

- Avoid editing `config/colonylogistics-common.toml` for every early test-play toggle.
- Make it obvious why a Container Dock Spawn/Deliver action failed.
- Keep command-driven changes runtime-only so temporary single-player helpers are not accidentally committed to pack defaults.

## New commands

All commands require permission level 2 and live under `/colonylogistics testing`.

```text
/colonylogistics testing show
/colonylogistics testing clearoverrides
/colonylogistics testing selfdelivery <true|false>
/colonylogistics testing loopbackfreight <true|false>
/colonylogistics testing loopbackcontainer <true|false>
/colonylogistics testing automarket <true|false>
/colonylogistics testing autoinventory <true|false>
/colonylogistics testing autocontainer <true|false>
/colonylogistics testing inventorycap <0-100>
/colonylogistics testing containercap <0-100>
```

These commands change runtime overrides only. They do not edit the TOML file and are reset when the JVM restarts or when `clearoverrides` is run.

For persistent defaults, edit:

```text
config/colonylogistics-common.toml
```

## Container Dock message cleanup

Container Dock GUI payloads and debug commands now send specific translatable messages for each `SpawnResult` and `DeliveryResult`, such as:

- `NO_SPACE`: no configured candidate position had enough free space.
- `WRONG_DOCK`: move/spawn at the assigned Dock.
- `TOO_FAR`: the container is outside delivery radius.
- `EXPORT_DISABLED` / `IMPORT_DISABLED`: Dock mode blocks the operation.
- `NOT_UNLOCKED`: colony logistics level does not unlock the required container size.

This is intended to make Create Aeronautics round-trip testing easier to diagnose.

## Directory layout

The 16.22-style project layout remains in place. `run/mods` is present for required dependency jars. The package does not include a `runs/client` directory.
