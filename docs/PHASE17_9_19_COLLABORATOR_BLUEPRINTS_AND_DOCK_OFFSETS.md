# Phase 17.9.19 - Collaborator blueprints and Container Dock offsets

## Summary

Phase 17.9.19 replaces the bundled MineColonies style pack blueprints with the collaborator-provided scan set from `scans.zip` and retunes Container Dock contract spawn pads to the new dock footprint.

## Blueprint replacement

The bundled `blueprints/colony_logistics_dev` style pack now contains the new scanned buildings for:

- `logistics_office1` through `logistics_office5`
- `container_dock1` through `container_dock5`
- `trade_terminal1` through `trade_terminal5`

The same files are mirrored under `blueprints/colony_logistics_dev/huts/` for MineColonies hut discovery compatibility.

The style pack display name was changed from `Colony Logistics Dev` to `Colony Logistics` while keeping the same pack folder id, `colony_logistics_dev`, so existing installer and discovery paths continue to work.

## Blueprint sanitization

The supplied scan files contained volatile scan metadata such as `SCAN_...` schematic names and captured world positions on some building block entities. The bundled files were sanitized so each hut blueprint data provider now uses stable values:

- `schematicName`: the final lowercase blueprint name, for example `container_dock5`
- `pack`: `Colony Logistics`
- `path`: the final blueprint filename, for example `container_dock5.blueprint`
- `posTagMap`: empty compound list
- volatile captured world `pos` values removed from the hut block entity payload

## New scanned footprint reference

The new scanned Container Dock levels all use:

- size: `21 x 8 x 22`
- Hut anchor: `(2, 1, 8)`

This replaces the previous mock-up assumptions:

- size: `19 x 10 x 19`
- Hut anchor: `(3, 1, 3)`

## Container spawn offset retune

Contract-spawned containers remain restricted to deterministic indoor pads only. For the unified 3 x 7 x 3 container footprint, the new relative core offsets from the Hut anchor are:

```text
(6,  2, -2)
(10, 2, -2)
(14, 2, -2)
(6,  2,  6)
(10, 2,  6)
(14, 2,  6)
```

The `y = 2` core height is derived from `containerSpawnBottomYOffset = 1` plus container half height `1`.

These six pads were checked against all supplied Container Dock levels 1 through 5 by reading the blueprint block arrays and verifying the full `3 x 7 x 3` container volume is air for each pad.

## Files changed

- `src/main/resources/blueprints/colony_logistics_dev/pack.json`
- `src/main/resources/blueprints/colony_logistics_dev/*.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/huts/*.blueprint`
- `src/main/java/jp/colonylogistics/service/ContainerDockService.java`
