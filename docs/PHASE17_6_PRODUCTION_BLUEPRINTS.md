# Phase 17.6 - Production-Style Logistics Office and Container Dock Blueprints

Phase 17.6 replaces the temporary 7x5x7 placeholder blueprints for **Logistics Office** and **Container Dock** with production-style Level 1-5 layouts for multiplayer test preparation.

## Scope

Updated generated Structurize/MineColonies blueprint files:

- `src/main/resources/blueprints/colony_logistics_dev/logistics_office1.blueprint` ... `logistics_office5.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/container_dock1.blueprint` ... `container_dock5.blueprint`
- matching duplicated files under `src/main/resources/blueprints/colony_logistics_dev/huts/`

The `Trade Terminal` blueprints are intentionally left unchanged in this phase.

## Invariants kept

- The Build Tool style pack remains `Colony Logistics Dev`.
- `pack.json` still includes `icon.png` and `"icon": "icon.png"`.
- Every updated blueprint keeps the MineColonies hut anchor at local `(3, 1, 3)`.
- Every updated blueprint keeps a tile entity entry at `(3, 1, 3)` with `blueprintDataProvider`.
- `optional_data/structurize/primary_offset` remains `(3, 1, 3)`.
- Root blueprint copies and `huts/` copies are byte-identical for the updated Logistics Office and Container Dock files.
- No GUI screen/menu files were modified.

## Logistics Office layout direction

The Logistics Office is now an enclosed office building instead of a temporary pad:

- Level 1 starts as a compact dispatch office with a front entrance, visible route-planning desk, queue rail, archive shelf, and corner lighting.
- Higher levels expand footprint and height, adding more archive shelving, service counters, windows, upgraded wall accents, and a rear mezzanine.
- The walking lane from the front entrance to the hut anchor is intentionally kept open.

Blueprint sizes:

| Level | Size |
|---:|---|
| 1 | 9 x 6 x 9 |
| 2 | 9 x 7 x 10 |
| 3 | 11 x 7 x 11 |
| 4 | 13 x 8 x 12 |
| 5 | 15 x 9 x 13 |

## Container Dock layout direction

The Container Dock is now a production-style loading apron with a heavy frame instead of a small temporary structure:

- The ground plane is mostly open and visual-first, with smooth-stone apron flooring and hazard/cargo pad markings.
- The hut anchor remains a control terminal near the front-side control area.
- Perimeter columns, high side rails, lights, and crane/gantry details increase by level.
- The central cargo apron is intentionally kept mostly open above the floor so future spawn/recognition offsets can be aligned to the visual dock without fighting dense blueprint blocks.

Blueprint sizes:

| Level | Size |
|---:|---|
| 1 | 11 x 6 x 11 |
| 2 | 13 x 7 x 13 |
| 3 | 15 x 8 x 15 |
| 4 | 17 x 9 x 17 |
| 5 | 19 x 10 x 19 |

## Container offset note

Container generation, delivery recognition, and dock occupied-footprint config values were **not** functionally changed in this phase. The current defaults are still legacy-safe values from the earlier 5x5 temporary test pad. A follow-up phase should align:

- `containerSpawnDockHalfX`
- `containerSpawnDockHalfZ`
- `containerSpawnDockOccupiedHeight`
- `containerSpawnBottomYOffset`
- recognition/delivery radius values

with the new visual Container Dock footprint and the intended Standard / Large / Heavy container staging lanes.

## Generation script

A reproducible generator was added at:

```text
scripts/generate_phase17_6_blueprints.py
```

It writes the updated Logistics Office and Container Dock blueprints in Structurize v1 compressed NBT format and preserves the MineColonies Build Tool metadata required by the current integration.

## Test notes

Before testing this phase in a development client, remove stale copied blueprints once:

```powershell
Remove-Item -Recurse -Force .\run\blueprints
```

Then run the usual client task so `copyBlueprintsToRun` refreshes the instance-level `run/blueprints` copy.
