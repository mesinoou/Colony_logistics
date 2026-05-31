# Phase 17.8.3 - Container Dock rotation-aware indoor spawning

## Goal

Phase 17.8.2 fixed overlapping and moved generated containers back inside the
Container Dock building, but the indoor lane was still expressed as fixed world
`+X/+Z` offsets. Rotated Container Dock buildings therefore spawned containers in
the wrong world direction.

Phase 17.8.3 keeps the same 3x7x3 unified container footprint and the same
nearest-first local lane, but transforms that lane through the MineColonies /
Structurize building rotation before choosing a world-space spawn position.

## Behavior

The blueprint-local core candidates remain nearest-first:

- `(5, 2, 6)`
- `(5, 2, 10)`
- `(5, 2, 14)`

At runtime, local `+Z` is converted to the Dock building's cargo-forward world
direction. Local `+X` is derived from that direction, so both candidate positions
and the actual 7-wide x 3-deep multiblock footprint rotate together.

The manifest now stores the generated container facing. This lets removal use the
same rotated footprint later, including after server restart.

## Replacement files

For manual file replacement, copy these files over the existing project:

- `src/main/java/jp/colonylogistics/colony/MineColoniesLogisticsResolver.java`
- `src/main/java/jp/colonylogistics/minecolonies/resolver/MineColoniesBuildingResolver.java`
- `src/main/java/jp/colonylogistics/buildingstate/ResolvedLogisticsBuilding.java`
- `src/main/java/jp/colonylogistics/container/ContainerManifest.java`
- `src/main/java/jp/colonylogistics/container/FreightContainerCoreBlockEntity.java`
- `src/main/java/jp/colonylogistics/container/ContainerMultiblockBuilder.java`
- `src/main/java/jp/colonylogistics/service/ContainerDockService.java`
- `src/main/java/jp/colonylogistics/dock/ContainerDockBlock.java`
- `src/main/java/jp/colonylogistics/command/LogisticsCommands.java`

The four manually tuned GUI files are unchanged.

## Test command

Use the candidate debug command on rotated Dock buildings:

```text
/colonylogistics container candidates <dock> standard
```

The header now includes `dockForward=<direction>`. The first candidates should
remain inside the building and should be ordered from the Dock block side toward
the far side.
