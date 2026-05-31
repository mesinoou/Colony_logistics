# Phase 16.8 - Structure Pack Metadata Fix

Structurize 1.0.822 requires `pack.json` to include an `icon` field.
Phase 16.7 omitted this field, causing `StructurePackMeta` to throw a NullPointerException while reading `run/blueprints/colony_logistics_dev`.

This phase adds:

- `src/main/resources/blueprints/colony_logistics_dev/icon.png`
- `icon: "icon.png"` to `pack.json`
- a clean step in `copyBlueprintsToRun` so stale run/blueprints copies are replaced.

Before launching after upgrading, it is still recommended to remove `run/blueprints` once.
