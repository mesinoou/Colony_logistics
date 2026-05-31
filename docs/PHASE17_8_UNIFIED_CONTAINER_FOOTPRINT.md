# Phase 17.8 - Unified Container Footprint and Non-overlapping Dock Lanes

Phase 17.8 fixes the in-game issue where three containers generated from a Container Dock could overlap.

## Player-facing rule

All generated freight containers now use one physical multiblock footprint:

- Depth / 縦: 3 blocks
- Width / 横: 7 blocks
- Height / 高さ: 3 blocks

The Standard / Large / Heavy names remain gameplay tiers for unlocks, job generation, required counts, and weight classes. They no longer create differently sized multiblocks.

## Code changes

- `ContainerSize.SMALL`, `MEDIUM`, `LARGE`, and `HEAVY` now all place as `7 wide x 3 high x 3 deep` blocks.
- Container volume is unified to `63` for the physical footprint. Existing base gameplay weights and logistics level gates remain tier-specific.
- The Container Dock apron grid now clamps candidate spacing at runtime:
  - X pitch is at least `container width + containerSpawnHorizontalGap`.
  - Z pitch is at least `container depth + containerSpawnHorizontalGap`.
- With the default one-block gap, the minimum pitch is now:
  - X: `7 + 1 = 8`
  - Z: `3 + 1 = 4`

This runtime clamp is intentional. If an existing test world still has an older config file with `containerSpawnApronSpacingX = 4`, the code still uses at least `8` and avoids overlap.

## Default lane layout

The example config now defaults to a single X column and four Z rows:

```toml
containerSpawnApronColumns = 1
containerSpawnApronRows = 4
containerSpawnApronSpacingX = 8
containerSpawnApronSpacingZ = 4
```

This means the first three generated containers line up as parallel 7-wide lanes with one block of air between their 3-deep footprints.

## Debugging

Use the existing candidate command before spawning:

```text
/colonylogistics container candidates <dock> standard
/colonylogistics container candidates <dock> large
/colonylogistics container candidates <dock> heavy
```

The command header now displays the unified footprint and the minimum X/Z pitch used for one-block spacing.

## Unchanged

- Logistics Office / Container Dock / Trade Terminal blueprints were not changed.
- Container Dock visual textures were not changed.
- Delivery and recognition radius defaults remain at Phase 17.7 values.
- Manually tuned GUI files were not changed.
