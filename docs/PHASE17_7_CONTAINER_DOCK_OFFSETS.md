# Phase 17.7 - Container Dock Apron Offsets and Recognition Range

Phase 17.7 aligns Container Dock behavior with the production-style Phase 17.6 Container Dock blueprints while keeping the visual building data fixed.

## Scope

Changed functional Container Dock offset/range behavior only:

- `ContainerDockService`
- `ColonyLogisticsConfig`
- `/colonylogistics container ...` debug commands
- `config/colonylogistics-common.toml.example`

The Phase 17.6 blueprint files are not regenerated in this phase.

## Important invariants

- Logistics Office, Container Dock, Trade Terminal, and Freight Board block textures are not changed.
- Phase 17.6 blueprint visuals are not changed.
- MineColonies hut anchors remain standard MineColonies hut blocks with state stored in `LogisticsMarketSavedData`.
- The manually tuned GUI files are not changed:
  - `ContainerDockScreen.java`
  - `FreightBoardScreen.java`
  - `TradeTerminalScreen.java`
  - `TradeTerminalMenu.java`

## New spawn strategy

`ContainerDockService.spawnCandidateCorePositions(...)` now builds candidates in this order:

1. GUI/client preferred core position, if provided.
2. A configurable apron staging grid.
3. The older symmetric horizontal fallback ring.
4. The older top fallback ring.
5. A second wider fallback ring.

The apron grid exists because the Phase 17.6 Dock blueprints are asymmetric around the Hut anchor:

- Hut anchor: local `(3, 1, 3)`
- Visual cargo apron: extends mainly toward positive X and positive Z
- Control booth / interaction side: near the front-left anchor area

This lets the Dock try visually sensible staging pads before old all-directions offsets.

## Default Phase 17.7 values

```toml
[dock]
deliveryRadius = 18.0
containerRecognitionRadius = 18.0
containerSpawnDockHalfX = 8
containerSpawnDockHalfZ = 8
containerSpawnDockOccupiedHeight = 7
containerSpawnHorizontalGap = 1
containerSpawnBottomYOffset = 1
containerSpawnTopGap = 1
containerSpawnExtraRingGap = 2

containerSpawnApronGridEnabled = true
containerSpawnApronStartX = 8
containerSpawnApronStartZ = 6
containerSpawnApronColumns = 2
containerSpawnApronRows = 2
containerSpawnApronSpacingX = 4
containerSpawnApronSpacingZ = 4
```

With these defaults, the first four apron candidates relative to the Dock block are:

- `(8, 2/3, 6)` depending on container height
- `(12, 2/3, 6)`
- `(8, 2/3, 10)`
- `(12, 2/3, 10)`

The Y value still uses `containerSpawnBottomYOffset + size.halfHeight()` so vertical placement remains configurable per pack/world.

## New diagnostic command

Use this before changing TOML values or blueprint lanes:

```text
/colonylogistics container candidates <dock> [standard|large|heavy]
```

It prints each candidate core position with:

- absolute core position
- relative offset from Dock block
- distance using the active delivery range resolver
- delivery-radius pass/fail
- multiblock space pass/fail
- combined usable flag

Example workflow:

```text
/colonylogistics container candidates 100 64 100 standard
/colonylogistics container candidates 100 64 100 large
/colonylogistics container candidates 100 64 100 heavy
```

Then create a same-dock test job and use the Dock GUI:

```text
/colonylogistics container localtest 100 64 100 heavy
```

Accept the generated job from the Logistics Office, open the Container Dock, spawn containers, and verify the GUI's nearby container diagnostics.

## Range behavior

`ContainerDockService` now exposes `deliveryDistance(...)`, and range diagnostics use the same active `DeliveryRangeResolvers.current()` path as delivery checks. This keeps Sable-aware fallback behavior and command diagnostics closer together.

## Next recommended checks

- Validate Level 1 and Level 5 Container Dock builds with `standard`, `large`, and `heavy` candidate commands.
- Confirm generated containers do not overlap gantry rails, hut/control booth blocks, or player approach lanes.
- Confirm same-colony two-dock jobs still select the correct destination Dock.
- Confirm recognition rows appear in the Container Dock GUI after Create / Create Aeronautics movement restores a container core near the destination Dock.
