# Phase 16.12.1 - Structurize API signature fix

`AbstractColonyLogisticsHutBlock` originally used `com.ldtteam.structurize.util.PlacementSettings`, but MineColonies 1.1.1300 / Structurize 1.0.822 uses `com.ldtteam.structurize.api.RotationMirror` in `AbstractBlockHut#setup` and `onBlockPlacedByBuildTool`.

Fix:

- Replace `PlacementSettings` import with `RotationMirror`.
- Update `setup(...)` signature to match MineColonies 1.1.1300.
- Pass `rotationMirror` through to `super.setup(...)` and `onBlockPlacedByBuildTool(...)`.
- Use `building.setRotationMirror(rotationMirror)`.

This patch only addresses the compile error from the wrong Structurize API class name/package.
