# Phase 17.9.8 - Creative Tab, Translation Cleanup, Dock Mode Removal, and Freight Board Removal

## Creative tab

Colony Logistics now registers its own creative tab, `colonylogistics:colony_logistics`.

Player-facing items are shown there:

- Logistics Office hut item
- Container Dock hut item
- Trade Terminal hut item
- Freight Parcel item

Generated freight container core/part blocks remain hidden from normal creative access because they are contract-generated multiblock internals.

## Standalone Freight Board removal

The standalone `freight_board` block/item has been formally removed from the player-facing registry and resource set.

Removed assets/resources:

- `src/main/java/jp/colonylogistics/board/FreightBoardBlock.java`
- `src/main/resources/data/colonylogistics/recipe/freight_board.json`
- `src/main/resources/assets/colonylogistics/blockstates/freight_board.json`
- `src/main/resources/assets/colonylogistics/models/block/freight_board.json`
- `src/main/resources/assets/colonylogistics/models/item/freight_board.json`
- `src/main/resources/assets/colonylogistics/textures/block/freight_board_*.png`

The Logistics Office contract GUI still uses the existing internal menu/screen classes to minimize risk to the manually tuned GUI layout. These are implementation details; the standalone block no longer exists.

## Container Dock mode removal

Container Docks are now always bidirectional.

- Export-only and import-only operation modes are omitted.
- The GUI mode-cycle button is removed.
- Old SavedData mode values are normalized to `BOTH` at runtime.
- Spawn and delivery logic effectively treats every valid Container Dock as both export-capable and import-capable.

## Translation cleanup

Hardcoded Japanese detail lines in the Logistics Office contract detail panel were replaced with translation keys. Container Dock and Trade Terminal contract tooltips were also changed to use translation keys.

This keeps English clients in English and Japanese clients in Japanese.

## MineColonies building UI access

Normal right-click still opens the Colony Logistics functional GUI.

Sneak-right-click the Logistics Office, Container Dock, or Trade Terminal hut/core block to pass the interaction to MineColonies and open the MineColonies building UI.
