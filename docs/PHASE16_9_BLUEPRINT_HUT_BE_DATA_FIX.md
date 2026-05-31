# Phase 16.9 - Blueprint Hut BlockEntity Data Fix

Build Tool could list the `Colony Logistics Dev` style, but selecting a hut blueprint crashed in the Structurize Build Tool screen.

Crash root cause:

- The generated `.blueprint` files contained MineColonies hut blocks.
- Those hut blocks had no matching tile entity data in the `tile_entities` list.
- Structurize calls `BlueprintTagUtils.isInvisible(...)` for the blueprint anchor.
- MineColonies `AbstractBlockHut#isVisible(...)` assumes the anchor hut has `blueprintDataProvider` data and dereferences the tile entity compound.

Fix:

- Every generated hut blueprint now contains a tile entity entry at the hut anchor position `(3, 1, 3)`.
- The entry includes `blueprintDataProvider` with:
  - `schematicName`
  - `corner1`
  - `corner2`
  - empty `posTagMap`
  - `pack`
  - `path`
- This gives MineColonies/Structurize enough metadata for Build Tool preview/list handling.

Affected files:

- `blueprints/colony_logistics_dev/logistics_office1..5.blueprint`
- `blueprints/colony_logistics_dev/container_dock1..5.blueprint`
- `blueprints/colony_logistics_dev/trade_terminal1..5.blueprint`
- and the duplicated `huts/` copies.

Before testing this version, delete stale dev blueprints:

```powershell
Remove-Item -Recurse -Force .\run\blueprints
```

Then run:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```
