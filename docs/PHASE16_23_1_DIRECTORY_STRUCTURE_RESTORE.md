# Phase 16.23.1 Directory Structure Restore

This patch keeps Phase 16.23 gameplay/debug changes, but restores the project layout to the pre-16.22.1 style.

## Important

- `runs/client/` is no longer included.
- Pre-copied `run/blueprints/` is no longer included.
- `run/mods/` is included as a development convenience location for prerequisite/runtime-only mod jars.
- `copyBlueprintsToRun` copies `src/main/resources/blueprints` to `run/blueprints` before `runClient` / `runServer`.
- The task only deletes `run/blueprints/colony_logistics_dev`; it does not touch `run/mods`.

## Use

1. Put prerequisite mods into `run/mods/`.
2. Run `gradle wrapper --gradle-version 8.10.2` if the wrapper is not present.
3. Run `./gradlew.bat build`.
4. Run `./gradlew.bat runClient`.
