# Phase 17.9.15 - Bundled Blueprint Runtime Installer

## Problem

A packaged Colony Logistics jar could start correctly in a clean multiplayer beta environment, but the MineColonies / Structurize building style was not visible in the Build Tool.

The bundled style pack already existed in the jar under:

```text
blueprints/colony_logistics_dev/
```

However, the previous development flow also relied on the Gradle helper task:

```text
copyBlueprintsToRun
```

That task copies the pack to the active game directory:

```text
run/blueprints/colony_logistics_dev/
```

When the built jar is copied to an independent client or dedicated server instance, Gradle is no longer involved, so the instance-level `blueprints` directory is not populated automatically.

## Fix

Added:

```text
src/main/java/jp/colonylogistics/blueprint/BlueprintPackInstaller.java
```

The installer copies all bundled `colony_logistics_dev` style pack files from the mod jar to:

```text
<gameDir>/blueprints/colony_logistics_dev/
```

It is invoked during `ColonyLogistics` mod construction so the pack is installed as early as possible when the jar is loaded.

## Notes

- This is common-side only and does not load client-only classes.
- Existing identical files are left untouched.
- Changed or missing files are overwritten from the bundled jar.
- The dev Gradle `copyBlueprintsToRun` task remains useful for local runs, but packaged jars no longer depend on it.

## Expected beta-test behavior

After starting a client or server with the packaged jar once, the instance should contain:

```text
blueprints/colony_logistics_dev/pack.json
blueprints/colony_logistics_dev/icon.png
blueprints/colony_logistics_dev/logistics_office1.blueprint ... logistics_office5.blueprint
blueprints/colony_logistics_dev/container_dock1.blueprint ... container_dock5.blueprint
blueprints/colony_logistics_dev/trade_terminal1.blueprint ... trade_terminal5.blueprint
```

The server / client log should also contain a line similar to:

```text
Installed Colony Logistics MineColonies style pack at .../blueprints/colony_logistics_dev
```
