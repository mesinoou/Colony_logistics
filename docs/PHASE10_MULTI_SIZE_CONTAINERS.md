# Phase 10: Multi-size container freight

This phase expands container freight from Small-only to all planned container sizes:

- `SMALL` 3x3x3, unlocked by Logistics Office level 2
- `MEDIUM` 5x3x3, unlocked by level 3
- `LARGE` 5x5x5, unlocked by level 4
- `HEAVY` 7x5x5, unlocked by level 5

## Generation rules

`FreightMarketService.ensureMinimumContainerJobs` now looks at both origin and destination colony limits. A job may only use a container size that both colonies can support.

The default generator is mixed-size and weighted toward smaller jobs:

- Small: common
- Medium: regular
- Large: uncommon
- Heavy: rare

A debug size override is available:

```mcfunction
/colonylogistics freight generatecontainers small
/colonylogistics freight generatecontainers medium
/colonylogistics freight generatecontainers large
/colonylogistics freight generatecontainers heavy
/colonylogistics freight generatecontainers all
```

If a forced size is above either colony's Logistics Office limit, no job is generated for that route.

## Container placement

The Dock GUI now suggests a core position based on the requested container size. The core is offset horizontally by half the width plus clearance, and vertically by half the height, so larger containers do not intersect the dock or ground as easily.

Manual command spawning remains available:

```mcfunction
/colonylogistics container spawn <contract_uuid> <dock_pos> <core_pos>
```

The server still validates clear space using the final multiblock dimensions.

## Sable mass behavior

No new block ids were added. The same container Core/Part blocks are used for all sizes.

Sable mass scales through two mechanisms:

1. Larger containers have more physical blocks.
2. Cargo weight maps to `weight_class`, which is written onto every Core/Part block.

This keeps mass distributed over the full container rather than concentrated only in the Core.
