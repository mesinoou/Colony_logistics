# Phase 16.10 - MineColonies Hut BlockEntity placement fix

Build Tool placement crashed when MineColonies tried to create the shared
`minecolonies:colonybuilding` BlockEntity for a Colony Logistics hut block.
Minecraft 1.21 validates `BlockEntityType` against its valid block set before
constructing the BlockEntity, and the MineColonies shared hut BlockEntityType did
not include third-party hut blocks.

This patch adds the three Colony Logistics hut blocks to the MineColonies
`minecolonies:colonybuilding` valid-block set during common setup:

- `colonylogistics:logistics_office`
- `colonylogistics:container_dock`
- `colonylogistics:trade_terminal`

The compatibility code is isolated in
`MineColoniesBlockEntityTypeCompat` so it can be replaced by an official
MineColonies API later.
