# Phase 17.9.5 - Crafting recipes and container block textures

This phase adds simple survival crafting recipes for the player-obtainable Colony Logistics blocks and replaces the placeholder vanilla copper container block visuals with dedicated 32x32 textures.

## Crafting recipes

Recipes are intentionally simple and vanilla-only so they remain usable even when Trade Post for MineColonies is absent or when the currency item ID is changed by config.

- `colonylogistics:logistics_office`
  - Paper, chests, lectern, oak planks.
- `colonylogistics:container_dock`
  - Iron ingots, rails, chest, smooth stone.
- `colonylogistics:trade_terminal`
  - Redstone, glass panes, chest, oak planks.
- `colonylogistics:freight_board`
  - Paper, ink sac, oak sign, oak plank.

Generated container blocks remain intentionally uncraftable. They are contract runtime blocks spawned and removed by Container Dock logic, not normal player progression blocks.

## Container textures

`freight_container_core` and `freight_container_part` no longer use vanilla copper/weathered copper models. Both now use custom corrugated metal textures. The existing `weight_class` blockstate now selects one texture family per weight class:

- `empty`: gray maintenance/empty shell.
- `light`: teal cargo shell.
- `medium`: orange default cargo shell.
- `heavy`: red reinforced shell.
- `super_heavy`: purple heavy-duty shell.
- `extreme`: dark shell with hazard striping.

The core block texture includes a reinforced hatch/control panel so the core can be visually distinguished from container part blocks during debugging and admin cleanup.

## Notes

The manually tuned GUI files were not changed. Container generation size, rotation, indoor lane offsets, reward currency, cancellation, and coin denomination exchange behavior are unchanged from Phase 17.9.4.
