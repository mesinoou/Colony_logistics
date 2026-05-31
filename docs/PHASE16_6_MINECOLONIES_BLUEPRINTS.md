# Phase 16.6 - MineColonies Build Tool Blueprints

This phase adds a bundled temporary Structurize style pack so the three Colony Logistics hut blocks can be selected from the MineColonies Build Tool instead of being placed directly.

## Added style pack

Path inside the built jar/source resources:

```text
blueprints/colonylogistics/colony_logistics_dev/pack.json
blueprints/colonylogistics/colony_logistics_dev/huts/logistics_office1.blueprint ... logistics_office5.blueprint
blueprints/colonylogistics/colony_logistics_dev/huts/container_dock1.blueprint ... container_dock5.blueprint
blueprints/colonylogistics/colony_logistics_dev/huts/trade_terminal1.blueprint ... trade_terminal5.blueprint
```

## Designs

All designs are deliberately temporary:

- 7x5x7 footprint.
- Same hut-block anchor position across levels: local `(3, 1, 3)`.
- Level 1-5 files all exist.
- Higher levels add small decorative blocks but keep the same footprint and anchor.

## Hut item behavior

`Logistics Office`, `Container Dock`, and `Trade Terminal` items are restored to `ItemBlockHut` so they behave as MineColonies building blocks and are meant to be placed by the Build Tool.

## Test flow

1. Build and run the client.
2. Create or enter a colony.
3. Obtain the Build Tool and one of the hut blocks.
4. Right-click a solid block with the Build Tool.
5. Select the hut category and switch style pack to `Colony Logistics Dev`.
6. Select level 1 initially and confirm placement.
7. Let the Builder construct it or use MineColonies blueprint build mode for faster testing.

## Notes

These are generated compressed NBT `.blueprint` files following Structurize Blueprint v1 fields: `version`, `size_x`, `size_y`, `size_z`, `palette`, `blocks`, `tile_entities`, `entities`, `required_mods`, `mcversion`, and `optional_data/structurize/primary_offset`.
