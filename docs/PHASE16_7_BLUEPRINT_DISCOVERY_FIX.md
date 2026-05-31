# Phase 16.7 - Build Tool Blueprint Discovery Fix

## Problem

The temporary style pack was previously stored at:

```text
src/main/resources/blueprints/colonylogistics/colony_logistics_dev/pack.json
```

MineColonies/Structurize style packs must be directly below the `blueprints` directory, for example:

```text
blueprints/colony_logistics_dev/pack.json
```

Because the pack was nested one level too deep, the Build Tool did not list the style.

## Fix

The style pack is now stored at:

```text
src/main/resources/blueprints/colony_logistics_dev/pack.json
```

The hut blueprints are also duplicated at the style root:

```text
blueprints/colony_logistics_dev/logistics_office1.blueprint
blueprints/colony_logistics_dev/container_dock1.blueprint
blueprints/colony_logistics_dev/trade_terminal1.blueprint
```

Root copies follow the MineColonies custom hut filename convention:

```text
{StyleName}/{HutName}{HutLevel}.blueprint
```

The `huts/` copies are kept as a development fallback.

## Dev run helper

A Gradle task was added:

```text
copyBlueprintsToRun
```

`runClient` and `runServer` depend on it, so the style pack is copied to:

```text
run/blueprints/colony_logistics_dev/pack.json
```

This makes the style visible even if Structurize scans only the instance-level `blueprints` folder during a development launch.
