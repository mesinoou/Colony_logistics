# Colony Logistics

Colony Logistics is a Minecraft 1.21.1 / NeoForge 21.1.227 mod prototype for MineColonies-based colony logistics.

## Concept

The mod turns MineColonies colonies into logistics endpoints. Players can earn currency by delivering generated freight jobs, and they can create escrowed player-to-player trades through colony Trade Terminals.

External role split:

- MineColonies: buildings, colony ownership, building levels, usage limits
- Trade Post for MineColonies: currency item only
- Create / Create Aeronautics: physical container transport
- Sable: physics mass, volume, and sub-level aware distance handling

## Current implemented scope

- Logistics Office building skeleton
- Container Dock building skeleton
- Trade Terminal building skeleton
- Freight Board block and GUI
- Inventory freight parcel jobs
- Container freight jobs
- Small / Medium / Large / Heavy container multiblock generation
- Sable-aware container delivery distance resolver
- Configurable currency item with emerald fallback
- Dock mode switching
- Trade Terminal escrow creation
- Trade Terminal delivery
- Trade Terminal cancellation and escrow refund
- Full ItemStack escrow persistence for player trades
- Player trade match mode selection: exact components or item id only
- Trade Terminal recent history display
- Full ItemStack persistence for Trade Terminal request/reward input slots

## Player trade flow

1. Open a Trade Terminal.
2. Put a request sample in the request slot.
3. Put the reward in the escrow reward slot.
4. Press Create Exact for item + component matching, or Create Item-ID for loose item-id matching.
5. Another player delivers the requested item and receives the reward.
6. The creator can cancel an open trade and receive the escrow refund.
7. Completed and cancelled trades appear in the terminal history section.

Player trade matching can use exact item + data component matching or item-id-only matching. Escrowed rewards are stored as full ItemStacks, preserving custom names, enchantments, damage, and 1.21 data components.

## Debug commands

Useful before full blueprint validation:

```mcfunction
/colonylogistics colony setoffice <colonyId> <level>
/colonylogistics terminal bind <pos> <colony> <level>
/colonylogistics dock bind <pos> <colony> <level>
/colonylogistics freight generate
/colonylogistics freight generatecontainers all
/colonylogistics freight list
/colonylogistics profile me
```

## Build notes

Gradle wrapper is not included. Use a local Gradle installation or add a wrapper matching your environment.

Pinned versions:

- Minecraft: 1.21.1
- NeoForge: 21.1.227
- MineColonies: 1.1.1300
- Sable: 1.21.1-1.1.3

## Phase 16 build/test-play notes

This phase prepares the project for local test-play builds.

Run the lightweight layout check:

```bash
./scripts/build_testplay_checklist.sh
```

Then build locally with a Gradle installation or generated wrapper:

```bash
gradle wrapper --gradle-version 8.10.2
./gradlew build
./gradlew runClient
```

Runtime dependency jars should be placed in `run/mods/`. See `run/mods/README.md`.

The Sable delivery distance resolver no longer imports Sable Companion at compile time. It uses reflection at runtime and falls back to vanilla distance if no compatible Companion API is present. This keeps the first build focused on NeoForge + MineColonies compilation while still allowing Sable-backed distance in a full runtime.

## Phase 16.3 Gradle wrapper note

`settings.gradle` intentionally does not use `RepositoriesMode.FAIL_ON_PROJECT_REPOS` because NeoForge ModDevGradle adds required Minecraft repositories during plugin application. Enabling that mode causes `gradle wrapper` to fail with `Mojang Minecraft Libraries was added by plugin`.

## Test-play creative inventory note

As of Phase 16.4, Logistics Office, Container Dock, Trade Terminal, and Freight Board are added to the vanilla Functional Blocks creative tab. Freight Parcel is added to Tools & Utilities.

Freight Container Core/Part are intentionally not added to creative tabs; contract containers should be spawned via Dock logic or admin/debug commands.

Direct give commands for testing:

```mcfunction
/give @p colonylogistics:logistics_office
/give @p colonylogistics:container_dock
/give @p colonylogistics:trade_terminal
/give @p colonylogistics:freight_board
/give @p colonylogistics:freight_parcel
```


## Phase 16.5 development placement note

Logistics Office, Container Dock, and Trade Terminal use vanilla `BlockItem` in this development build so they can be placed directly from the creative tab before real MineColonies/Structurize blueprints exist. This is for test-play only; production MineColonies placement still requires proper `.blueprint` structure data.

## Phase 16.6 MineColonies blueprint test pack

The mod now bundles a temporary Structurize/MineColonies style pack:

```text
blueprints/colonylogistics/colony_logistics_dev/
```

It contains level 1-5 blueprints for:

```text
logistics_office
container_dock
trade_terminal
```

These are intentionally simple placeholder builds so the blocks can be selected from the MineColonies Build Tool. Replace them later with proper production designs made with the Structurize Scan Tool.

## Phase 16.7 note - Build Tool style discovery

The temporary MineColonies style pack is bundled at:

```text
src/main/resources/blueprints/colony_logistics_dev/pack.json
```

During `runClient`, Gradle copies it to:

```text
run/blueprints/colony_logistics_dev/pack.json
```

If the Build Tool still does not show the style, delete old cached/nested copies from `run/blueprints`, then restart the client.


## Phase 16.8 note

Build Tool style pack metadata now includes `icon.png`, which Structurize requires while loading `pack.json`. If the style does not appear, delete `run/blueprints` and restart `runClient`.

## Phase 16.12 MineColonies hut-state rework

MineColonies hut anchors now remain MineColonies-owned. Container Dock and Trade Terminal no longer attach custom BlockEntities to the hut block. Runtime state is stored in `LogisticsMarketSavedData` by dimension + hut position, allowing multiple docks and terminals in the same colony.

Before testing this phase, delete stale generated blueprints once:

```powershell
Remove-Item -Recurse -Force .\run\blueprints
```
## Phase 17.6 production-style blueprint pass

Logistics Office and Container Dock Level 1-5 blueprints in `src/main/resources/blueprints/colony_logistics_dev` are now production-style multiplayer-test layouts instead of the earlier temporary 7x5x7 pads. The Build Tool anchor remains `(3, 1, 3)` and the required `blueprintDataProvider` metadata is preserved.

Container Dock visuals now represent a larger loading apron/gantry. Spawn and recognition offsets are intentionally left for the next tuning pass; see `docs/PHASE17_6_PRODUCTION_BLUEPRINTS.md`.

