# Phase 17.9.20 - Blueprint Pack Replacement Fix

Phase 17.9.19 put the collaborator scan blueprints into the legacy `colony_logistics_dev` style-pack folder. In existing multiplayer/client instances, that allowed old copied files or Structurize/MineColonies style cache state to keep showing stale building data.

This phase makes the collaborator pack the only active bundled pack:

- The bundled resources now live under `src/main/resources/blueprints/colony_logistics/`.
- The old `src/main/resources/blueprints/colony_logistics_dev/` resource folder is removed.
- Runtime installation writes to `<gameDir>/blueprints/colony_logistics/`.
- Runtime installation deletes `<gameDir>/blueprints/colony_logistics_dev/` before installing the new pack.
- Runtime installation also deletes and recreates `<gameDir>/blueprints/colony_logistics/` to prevent stale beta files from surviving across builds.
- The development `copyBlueprintsToRun` task deletes both old and new run blueprint folders before copying resources.

Container Dock offsets remain those calculated for the collaborator scan footprint:

- Hut anchor: `(2, 1, 8)`
- Footprint: `21 x 8 x 22`
- Relative container core pads: `(6,2,-2)`, `(10,2,-2)`, `(14,2,-2)`, `(6,2,6)`, `(10,2,6)`, `(14,2,6)`
