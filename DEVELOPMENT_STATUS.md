## Current Status

Phase 17.9.21: Container Dock coarse-dirt pad alignment. The collaborator blueprint pack remains active, and Container Dock contract containers now target the scanned coarse-dirt staging markers instead of spawning outside the building when resolve reports cargoForward=SOUTH.

Phase 17.9.11: MineColonies標準建築UIのタブとしてColony Logistics UIを開く方針へ移行。

# Phase 17.9.9.1 - Sable dependency metadata revert

- Reverted `neoforge.mods.toml` to the Phase 17.9.8 dependency style: `sable` is again `required` with version range `[1.1.3,)`.
- Removed the optional `sablecompanion` dependency metadata entry added in Phase 17.9.9.
- Kept the actual Logistics Office crash fix: UUID values in translated tooltips are still converted to strings before `Component.translatable`.
- Container Dock / Trade Terminal contract tooltips keep the same UUID string-safety fix.
- FreightBoardScreen manual layout constants remain unchanged (`imageWidth=470`, `STATUS_X=250`, `ASSIGNEE_X=300`, `REWARD_X=340`, `ACTION_W=40`).

# Colony Logistics - Development Status

## Phase 17.8.8 - Dock X alignment and production blueprint rebuild

After in-game confirmation that the Phase 17.8.7 Dock-side Z shift was broadly correct, the contract-safe indoor staging grid was shifted one additional block toward the Container Dock/control side on X. Contract-safe core offsets are now `(4,2,2)`, `(8,2,2)`, `(12,2,2)`, `(4,2,10)`, `(8,2,10)`, `(12,2,10)`. The unified 3x7x3 footprint, one-block spacing, rotation-aware cargo direction, and indoor-only contract spawning rules are unchanged.

Container Dock Level 1-4 blueprints were rebuilt to reserve the same 19x10x19 production footprint as Level 5, so upgrades proceed inside the final plot instead of expanding the buildable area. Trade Terminal Level 1-5 blueprints were also replaced with production-style marketplace/escrow hall layouts using a fixed 15x8x13 footprint, while keeping the Hut anchor `(3,1,3)` and blueprintDataProvider payloads.

Next development should move on to reward currency hardening and multiplayer pre-test cleanup.

## Phase 17.8.7 - Container Dock one-block Dock-side alignment

After in-game confirmation that Phase 17.8.6 generally placed containers correctly, the indoor Container Dock candidate grid was shifted one block toward the Container Dock/control side. The unified 3x7x3 footprint, one-block spacing, rotation-aware cargo direction, and indoor-only contract spawning rules are unchanged. The nearest contract row now uses local core offsets `(5,2,2)`, `(9,2,2)`, `(13,2,2)` and the second row uses `(5,2,10)`, `(9,2,10)`, `(13,2,10)`.

Next development should move on from Dock alignment to reward currency hardening and multiplayer pre-test cleanup.

## Phase 17.8.6 - Container Dock visual footprint orientation and offset fix

Fixed the remaining in-building offset mismatch shown in the May 9 test screenshot. The generated container multiblock now uses the manual mock-up orientation: the 3-deep side spans across the apron, the 7-wide side runs along the Dock cargo lane, and height remains 3 blocks. Contract spawn candidates use an indoor grid of local offsets `(5,2,3)`, `(9,2,3)`, `(13,2,3)`, `(5,2,11)`, ... ordered row-major from the Dock-side row toward the deeper cargo row, so the first three containers align in the nearest valid row. Contract spawning also checks the Phase 17.6 blueprint dimensions for the current building level so it cannot use indoor-grid positions outside lower-level Dock frames.

## Phase 17.8.5 - Container Dock contract spawn indoor-only fix

Fixed the follow-up issue where command diagnostics showed valid indoor Container Dock positions but GUI/contract spawning could still fall through to old debug fallback candidates and place a contract container outside the building. Contract spawning now uses `findAvailableContractCorePos(...)`, which is restricted to a deterministic indoor-only candidate list. Debug apron/ring candidates are still visible through `/colonylogistics container candidates`, but they are marked with `contractSafe=false` and are not used for accepted contract containers. The reported `dockForward=south` orientation is treated as local +Z / screenshot-left for the indoor offsets.

## Phase 17.8.4 - Container Dock rotation-aware replacement

- Container Dock indoor spawn candidates now respect MineColonies/Structurize Build Tool rotation.
- The nearest-first local lane remains `(5,2,6)`, `(5,2,10)`, `(5,2,14)`, but it is transformed into world coordinates using the Dock cargo-forward direction.
- Generated 3x7x3 container multiblocks rotate their footprint together with the Dock direction.
- Container manifests now persist `Facing` so removal uses the same rotated footprint after restart.
- `/colonylogistics container candidates` now reports `dockForward`.
- Manual GUI tuning files were not changed.


## Current phase

Phase 17.8.8: Dock X alignment and production blueprint rebuild.

## Implemented

- Fixed target versions:
  - Minecraft 1.21.1
  - NeoForge 21.1.227
  - MineColonies 1.1.1300
  - Sable 1.21.1-1.1.3
- MineColonies building registration skeleton:
  - Logistics Office
  - Container Dock
  - Trade Terminal
- Colony logistics state and limits
- Freight contracts SavedData
- Inventory freight parcel flow
- Freight Board GUI
- Container Dock GUI
- GUI C2S payloads
- Trade Post-style configurable currency item fallback
- Dock modes: BOTH / EXPORT / IMPORT
- Standard / Large / Heavy container generation with a unified 3x7x3 physical footprint, visual mock-up-aligned orientation, and rotation-aware indoor nearest-first Dock staging grid
- Sable-aware container delivery range resolver
- Player trade escrow terminal
- Player trade delivery
- Player trade cancellation and refund
- Full ItemStack escrow persistence for player trades
- Player trade match modes:
  - exact item + components
  - item id only
- Trade Terminal recent history section
- Full ItemStack persistence for Trade Terminal input slots

## Pending

- Final multiplayer balance pass for Structurize / MineColonies blueprint progression
- Trade Terminal live refresh / pagination
- Player trade long-term history cleanup config
- Trade Terminal live refresh / pagination
- Transport rank UI
- Sable mass tuning in live Create Aeronautics vehicles
- Build verification in a local Gradle environment

## Notes

Gradle wrapper is not included in this starter package, so packaging checks have been performed by ZIP integrity only in this environment.

## Phase 16 - Build/Test-play preparation

Added build/test-play preparation work:

- Sable Companion direct import replaced with reflection-based resolver to reduce first build failures.
- Sable physics remains data-driven through `physics_block_properties`.
- Added automatic server tick market top-up with config switches.
- Added example common config.
- Added local runtime mods README.
- Added project layout checklist script.

Remaining before full playable release:

- Real Gradle compile in local environment with dependency jars resolved.
- Structurize/MineColonies blueprint files.
- GUI live refresh/paging.
- Sable mass tuning in an actual Create Aeronautics vehicle.


## Phase 16.5 development placement note

Logistics Office, Container Dock, and Trade Terminal use vanilla `BlockItem` in this development build so they can be placed directly from the creative tab before real MineColonies/Structurize blueprints exist. This is for test-play only; production MineColonies placement still requires proper `.blueprint` structure data.

## Phase 16.6 - MineColonies blueprint placeholders

Added a bundled temporary style pack `Colony Logistics Dev` with level 1-5 `.blueprint` files for Logistics Office, Container Dock, and Trade Terminal. The three building items are restored to MineColonies `ItemBlockHut` behavior for Build Tool placement.

## Phase 16.10

- Added MineColonies hut BlockEntityType compatibility patch.
- Fixes Build Tool placement crash: `Invalid block entity minecolonies:colonybuilding ... got Block{colonylogistics:...}`.


## Phase 16.11

- Fixed MineColonies hut ticker crash by making custom hut BlockEntities implement `ITickable`.
- Applies to Container Dock and Trade Terminal.

## Phase 16.12 - MineColonies Hut state rework integrated

- Reworked Colony Logistics MineColonies hut blocks to avoid custom BlockEntity anchors.
- Added `AbstractColonyLogisticsHutBlock` to force Build Tool creative instant placement registration.
- Moved Container Dock mode and Trade Terminal input state to `LogisticsMarketSavedData` keyed by dimension + hut position.
- Updated Dock/Terminal GUI, payloads, and services to resolve from `ServerLevel + BlockPos` instead of custom hut BlockEntities.
- This is intended to fix the "hut block only placed / building cannot be found" issue and support multiple docks/terminals per colony.

## Phase 16.18 - Inventory freight single-player test flow

- Added right-click delivery for `FreightParcelItem`.
- A held parcel can now be delivered by right-clicking the destination Logistics Office or another logistics building in the destination colony.
- Added `testing.allowLoopbackFreightForTesting` so one active Logistics Office can generate loopback inventory parcel jobs during early single-player tests.
- Added destination position to parcel tooltip.
- `/colonylogistics freight deliver` remains available as a debug fallback.


## Phase 16.19 - Container Dock single-player test flow

- Added `testing.allowLoopbackContainerFreightForTesting` for one-colony container freight generation during early single-player tests.
- Container freight generation can now create loopback routes when no second container-capable colony exists.
- Same-colony loopback generation uses different docks when available; a one-dock colony can still produce same-dock loopback jobs for first-pass testing.
- Added `/colonylogistics container localtest <dock> [size]` to create a same-dock container freight contract explicitly.
- This phase targets Container Dock state-machine verification before Create / Create Aeronautics / Sable physical movement is integrated.

## Phase 16.25
- Added runtime-only `/colonylogistics testing ...` commands for single-player test toggles, auto-generation toggles, and board generation caps.
- Added specific translatable Container Dock spawn/delivery failure messages for easier Create Aeronautics round-trip debugging.
- Kept the restored 16.22-style directory layout with `run/mods` for required dependency jars.

## Phase 16.27 - Container Dock detail view and diagnose command

- Expanded Container Dock GUI diagnostics for nearby containers.
- Added shared delivery analysis in `ContainerDockService`.
- Added `/colonylogistics container diagnose <dock>` for Create Aeronautics restore tests.
- Deliver button remains enabled only for `OK` rows.
## Phase 17.6 - Production-style Logistics Office / Container Dock blueprints

Replaced the temporary Logistics Office and Container Dock Level 1-5 blueprint pads with larger production-style layouts for multiplayer test preparation. The MineColonies hut anchor remains at local `(3, 1, 3)`, all updated files retain `blueprintDataProvider`, and the duplicated `huts/` blueprint copies were updated together. Trade Terminal blueprints and the manually tuned GUI files were not changed.

Container Dock blueprints now prioritize the visual loading apron/gantry shape; container spawn and recognition offsets remain legacy defaults and should be aligned in a follow-up phase.



## Phase 17.7 - Container Dock apron offsets and diagnostics

Aligned Container Dock spawning with the production-style Phase 17.6 loading apron without changing the blueprint visuals or the manually tuned GUI files. Container spawning now tries a configurable positive-X/Z apron staging grid before falling back to the older symmetric rings. The default delivery and recognition radii are raised to 18 blocks so Standard / Large / Heavy containers can use the first 2x2 apron lanes. Added `/colonylogistics container candidates <dock> [size]` to inspect candidate core positions, relative offsets, range status, and space checks in-game before committing further offset changes.


## Phase 17.8 - Unified 3x7x3 containers and one-block lane gaps

Fixed the in-game issue where multiple Container Dock spawns could overlap. All physical container multiblocks are now unified to 3 deep x 7 wide x 3 high blocks while retaining Standard / Large / Heavy gameplay tiers, weights, and unlock progression. Apron-grid candidate spacing is clamped at runtime to at least container width/depth plus `containerSpawnHorizontalGap`, so existing worlds with older `containerSpawnApronSpacingX = 4` configs will still avoid overlap. The example config now uses one X column and four Z rows, causing the first three containers to line up as parallel lanes with one block of air between them.


## Phase 17.8.1 - Safe exterior Container Dock lane and placement verification

Fixed the follow-up issue where the Dock reported “container spawned” but the container was not visible from the Dock. The server now tries a deterministic exterior staging lane before old GUI/config apron candidates: core offsets `(19,2,4)`, `(19,2,8)`, `(19,2,12)`, ... relative to the Hut anchor. With the unified 3 deep x 7 wide x 3 high footprint, the 4-block Z pitch leaves exactly one clear block between adjacent containers.

The Dock delivery/recognition radius is runtime-clamped to at least 28 blocks so old `18.0` TOML files cannot make this safe lane unusable. Final multiblock placement is now verified after setting blocks; if the core block entity/manifest cannot be confirmed, the contract is not advanced and `PLACEMENT_FAILED` is reported instead of a false success. Successful spawns also print the actual core coordinate for in-game debugging.


## Phase 17.8.2 - Indoor nearest-first Container Dock lane

Fixed the follow-up issue where the Phase 17.8.1 safe lane spawned containers outside the Container Dock building. The server now tries an indoor lane before any GUI/config/fallback candidates: core offsets `(5,2,6)`, `(5,2,10)`, `(5,2,14)`, ... relative to the Hut anchor. With the unified 3 deep x 7 wide x 3 high footprint, these occupy local X `2..8` and Z `5..7`, `9..11`, `13..15`, leaving exactly one air block between adjacent containers while staying inside the Phase 17.6 Dock frame.

The candidate order is nearest-first from the Container Dock block. Stale GUI payloads and older TOML apron values are still accepted only after this deterministic indoor lane, so old worlds cannot prefer an exterior position unless all indoor candidates are blocked. The default delivery and recognition radius is returned to 18 blocks because the primary lane is close to the Dock again.


## Phase 17.8.4

- Fixed Container Dock spawn lane rotation by resolving cargo direction from the actual Build Tool-rotated hut block FACING state first.
- Kept MineColonies cached rotation as fallback, but no longer maps unresolved `-1` rotation to a valid direction.
- Container size, near-first indoor lane order, and GUI layout files remain unchanged.

## Phase 17.8.9 - Logistics Office fixed-footprint blueprint rebuild

Rebuilt Logistics Office Level 1-5 blueprints so Level 1 already reserves the full Level 5 production footprint. All Logistics Office levels now use a stable 15x9x13 Build Tool/MineColonies volume with Hut anchor `(3,1,3)`, matching the fixed-footprint policy used for Container Dock and Trade Terminal. Lower levels are simpler/opener but progressively add dispatch counters, archive shelves, queue rails, route planning desks, roof coverage, and a higher-level mezzanine without expanding the occupied site.

Container Dock offset behavior, Container Dock blueprints, Trade Terminal blueprints, and manually tuned GUI files were left unchanged from Phase 17.8.8.

## Phase 17.9 - Reward currencyization and Trade Terminal reward restriction

Centralized payout handling around `RewardSpec` and `CurrencyService`. Generated inventory and container freight now resolve the actual payable currency before mutating delivery state, so missing currency config no longer completes a contract without a valid payout path. Container delivery now reports `REWARD_UNAVAILABLE` when the final payout currency cannot be resolved.

Trade Terminal player trades are multiplayer-safe by default: new trades may request any item, but the escrow reward must be the active configured currency item when `currency.playerTradeRewardsMustBeCurrency = true`. The escrowed reward is normalized to a currency stack, and delivery/cancel refunds now use `CurrencyService` through `PlayerTradeContract.rewardSpec()` while keeping the legacy full ItemStack NBT layout for old saves.

Manual GUI files remain unchanged from Phase 17.4 tuning. Next recommended phase: multiplayer preflight defaults and checks, especially disabling self-delivery and loopback generation for a clean two-player/two-colony test.

## Phase 17.9.1 - Default reward currency corrected to Trade Post coin

Corrected the Phase 17.9 issue where generated freight rewards could still display/pay `minecraft:emerald` because the old development placeholder `tradepost:coin` fell through to the emerald fallback. The default generated reward item is now `mctradepost:mctp_coin`, matching Trade Post for MineColonies' base coin. Existing TOML values `tradepost:coin` and `fallbackCurrencyItem = "minecraft:emerald"` are normalized at runtime so old test worlds stop using emerald as the default reward without requiring immediate config deletion.

`CurrencyService` no longer has a hardcoded emerald last fallback, `ItemCurrencyAdapter` no longer creates emerald stacks when the configured currency is missing, and old/generated contracts are normalized when loaded or inserted into `LogisticsMarketSavedData`. If the configured Trade Post coin is unavailable and fallback is disabled, payout now fails visibly instead of silently using emerald.

## Phase 17.9.2 - Balance config surface

Added persistent TOML-backed balance knobs before multiplayer preflight. Generated freight balance can now be tuned without code edits for building-level limits, container unlock tiers, carrier level requirements, generated job caps, reward formulas, distance/weight multipliers, deadlines, late-delivery payout percentage, container generation ratios, per-contract container counts, and Carrier Profile progression/penalties.

`ColonyLogisticsLimits`, generated `FreightJobSpec` creation, reward calculation, late payout handling, and Carrier Profile level/reputation updates now read from `ColonyLogisticsConfig`. The existing early-test market cap keys remain for compatibility but are documented as normal balance knobs. Added `/colonylogistics balance show` to print the active TOML values in-game for quick verification after editing `config/colonylogistics-common.toml`.

Manual GUI files remain unchanged from Phase 17.4 tuning. Next recommended step remains multiplayer preflight/safety defaults after balance values are tested in-world.

## Phase 17.9.3 - Assigned freight cancellation and unlimited deadlines

Added a carrier-side cancellation path for generated freight before multiplayer preflight. Assigned players can now cancel their own ACCEPTED / PICKED_UP generated freight from the Logistics Office board. Inventory freight cancellation removes matching freight parcel items from the carrier inventory when present. Container freight cancellation marks the contract CANCELLED, releases the origin colony active-container-job slot when a container had spawned, and removes matching spawned containers found near the origin/destination Dock; containers already moved far away become undeliverable because the saved contract is cancelled.

Added TOML balance controls under `[balance.deadlines]`: `generatedJobsAllowCarrierCancel`, `generatedJobsCancelCountsAsFailed`, and `generatedJobsAllowCancelAfterContainerSpawn`. The existing pickup/delivery window settings now treat `0` as unlimited. Newly generated contracts store pickup/delivery/expires values as `0` for unlimited windows, deadline checks now also consult the current TOML window, and board/candidate ordering pushes unlimited entries after finite-deadline jobs. The Freight Board displays `No deadline` / `無期限` for `expiresGameTime = 0`.

## Phase 17.9.4 - Currency denomination exchange payouts

Reward amounts are now treated as base-coin value and are exchanged into configured coin denominations when paid. By default, `mctradepost:mctp_coin` is the base unit, `mctradepost:mctp_coin_gold` is worth 8 base coins, and `mctradepost:mctp_coin_diamond` is worth 64 base coins. Payouts greedily use the largest registered denomination first and split stacks safely by each coin item's max stack size, dropping overflow if the carrier inventory is full.

Trade Terminal escrow now accepts configured denominations instead of only the base coin. For example, an escrow stack of two diamond coins is stored visually as diamond coins but resolves to 128 base coins for payout/refund, which may then be paid back as the largest available registered denominations. `/colonylogistics balance show` now reports the active base coin, exchange toggle, and registered denominations.

Manual GUI layout constants remain unchanged.

## Phase 17.9.5 - Recipes and container block textures

Added simple vanilla-only crafting recipes for the player-obtainable blocks: Logistics Office, Container Dock, Trade Terminal, and Freight Board. The generated freight container core/part blocks remain intentionally uncraftable because they are runtime multiblock pieces managed by Container Dock contracts.

Replaced the placeholder vanilla copper/weathered copper container visuals with dedicated 32x32 Colony Logistics textures. `freight_container_core` and `freight_container_part` now use weight-class-specific corrugated metal models for empty/light/medium/heavy/super-heavy/extreme containers, and the core block has a visible hatch/control panel for debugging.

Manual GUI layout constants, Container Dock offsets, blueprint data, currency payouts, cancellation behavior, and balance config remain unchanged from Phase 17.9.4.

## Phase 17.9.6 - Auto-generated job count and low-difficulty mix

Increased the default auto-generated freight pool size and exposed a low-difficulty mix so the expanded market does not become dominated by high-reward/high-difficulty jobs. The default top-up interval is now 12000 ticks, inventory job cap per origin colony is 12, and container job cap per origin colony is 6. Building-level defaults were raised accordingly while still using the existing effective target formula of `min(level cap, market cap)`.

Added TOML settings under `[market]`: `lowDifficultyInventoryPercent`, `lowDifficultyContainerPercent`, `lowDifficultyInventoryMax`, and `lowDifficultyContainerMax`. New top-up generation checks the current OPEN pool for each origin colony/unit type and marks newly generated jobs as low-difficulty-preferred until the configured share is reached. Inventory jobs select cargo at or below the configured difficulty ceiling; container jobs prefer Standard containers and low/normal cargo when possible. Existing OPEN jobs are not rewritten, and if no low-difficulty cargo matches the configured ceiling the generator falls back to the normal catalog instead of stalling.

`/colonylogistics balance show` now prints the low-difficulty mix settings for in-game verification. Manual GUI layout constants, Container Dock offsets, recipes/textures, currency payouts, and cancellation behavior remain unchanged from Phase 17.9.5.

## Phase 17.9.7 - Parcel icon, style pack icon, unlimited defaults, and item recipes

Added a dedicated 32x32 item texture for `freight_parcel` and changed its item model away from vanilla paper. Blank/crafted parcels now have a clearer tooltip explaining that they are not contract-assigned; accepted inventory freight still stamps the real contract NBT server-side.

Replaced the 1x1 placeholder `blueprints/colony_logistics_dev/icon.png` with a 128x128 Colony Logistics style-pack image so the Build Tool style is recognizable while preserving `pack.json`'s required `"icon": "icon.png"` field.

Changed generated freight deadline defaults to unlimited by setting pickup, inventory delivery, and all container delivery windows to `0` in both `ColonyLogisticsConfig` defaults and the example TOML. Positive tick values can still be configured to re-enable deadlines.

Added a simple vanilla-only `freight_parcel` crafting recipe and documented all current player-facing recipes. Runtime container core/part blocks remain uncraftable. Manual GUI layout constants, Dock offsets, currency payout exchange, and generated job balance settings remain unchanged from Phase 17.9.6.

## Phase 17.9.8

- Added a dedicated Colony Logistics creative tab for player-facing mod items.
- Removed the standalone Freight Board block/item, recipe, blockstate, model, item model, and textures.
- Kept the Logistics Office contract screen/menu internally to preserve the manually tuned GUI while removing the standalone block from gameplay.
- Removed the Container Dock mode-cycle button and normalized all Container Docks to bidirectional operation.
- Replaced hardcoded contract detail/tooltip labels with translation keys for English/Japanese clients.
- Added an initial sneak-right-click fallback on Logistics Office, Container Dock, and Trade Terminal hut blocks; Phase 17.9.10 replaces this because sneak right-click is used by MineColonies inventory access.


## Phase 17.9.9.1 - Sable dependency metadata restored

Kept the Logistics Office crash fix from Phase 17.9.9, where UUID tooltip arguments were converted to strings before being passed to translatable components. Reverted the unrelated dependency metadata change so `sable` remains required as before, and removed the optional `sablecompanion` metadata added during the crash-fix pass.

## Phase 17.9.10 - Separate MineColonies building UI access

Sneak right-click on Colony Logistics hut cores is no longer treated as the intended MineColonies upgrade/repair UI shortcut because in-game behavior showed that MineColonies uses it for the hut block inventory. Normal right-click remains reserved for Colony Logistics screens, and sneak right-click remains delegated to MineColonies unchanged.

Added a separate access path: hold the Structurize/MineColonies Build Tool and right-click a Logistics Office, Container Dock, or Trade Terminal core without sneaking. This delegates to the inherited MineColonies hut interaction and opens the standard MineColonies building UI for upgrades/repairs/building management. Supported opener ids are `structurize:build_tool`, `structurize:buildtool`, `minecolonies:build_tool`, and `minecolonies:buildtool`. Manual GUI files and Sable dependency metadata remain unchanged from Phase 17.9.9.1.

## Phase 17.9.11.1

- Fixed compile errors in MineColonies UI tab integration.
- Updated ColonyLogisticsBuildingModuleView deserialize signature to RegistryFriendlyByteBuf.
- Added explicit typed nested Supplier for BuildingEntry.ModuleProducer view module creation.
- No gameplay or GUI layout changes.

## Phase 17.9.13 - Multiplayer Beta Logging

- Added server-side `[CL-MP]` debug logs for multiplayer beta testing.
- Added config flags under `[testing]`:
  - `debugMultiplayerNetworkLogging = true`
  - `debugContractLifecycleLogging = true`
- C2S packet handlers now log packet receive, validation rejects, and final results for Logistics Office, Container Dock, Trade Terminal, and MineColonies-tab open actions.
- Generated freight, accept/cancel/deliver lifecycle, container spawn/delivery, and Trade Terminal escrow create/deliver/cancel paths now emit lifecycle logs.
- Added `docs/PHASE17_9_13_MULTIPLAYER_BETA_LOGGING.md`.

## Phase 17.9.12 - Multiplayer Safety Pass 1

- Added server-side guards so Colony Logistics action packets must come from the matching open menu and nearby building.
- Fixed menu `stillValid` checks to validate the expected Hut block type.
- Made Trade Terminal setup slots menu-local to prevent shared escrow/input conflicts between multiple players.
- Added `docs/PHASE17_9_12_MULTIPLAYER_SAFETY.md`.


## Phase 17.9.14 - Multiplayer defaults and route preflight

- Reverted single-player diagnostic defaults for multiplayer beta: `allowSelfDeliveryForTesting`, `allowLoopbackFreightForTesting`, and `allowLoopbackContainerFreightForTesting` now default to `false`.
- Kept Phase 17.9.13 multiplayer beta logging enabled by default.
- Blocked `/colonylogistics market localtest ...` unless loopback container testing is explicitly enabled at runtime.
- Added route preflight logging for skipped generated-freight top-ups when fewer than two eligible colonies exist.
- Static route inspection confirms generated inventory/container contracts filter out the origin colony when loopback testing is disabled.
- Added `docs/PHASE17_9_14_MULTIPLAYER_DEFAULTS_AND_ROUTE_PREFLIGHT.md`.

## Phase 17.9.15 - Bundled Blueprint Runtime Installer

- Fixed packaged-jar beta environments where the mod loaded but the MineColonies / Structurize building style was not listed.
- Added a common-side runtime installer that copies the bundled style pack from jar resources to `<gameDir>/blueprints/colony_logistics_dev` during mod construction.
- The previous Gradle `copyBlueprintsToRun` helper remains for development runs, but packaged jars no longer rely on Gradle to populate the instance `blueprints` directory.
- No client-only classes are referenced by the installer.
- Added phase notes at `docs/PHASE17_9_15_BUNDLED_BLUEPRINT_RUNTIME_INSTALLER.md`.

## Phase 17.9.16 - Trade Terminal safe system chat

- Added `SafeSystemChat`, a server-side helper that sends simple literal system messages with sanitized string-only dynamic values.
- Switched Trade Terminal create/deliver/cancel network rejection messages and `PlayerTradeService` player notifications away from dynamic `Component.translatable(...)` system-chat payloads.
- This targets collaborator-only disconnects with `Failed to decode packet 'clientbound/minecraft:system_chat'` during player trade creation.
- Gameplay, escrow handling, multiplayer guards, and `[CL-MP]` beta logging are unchanged.
- Added phase notes at `docs/PHASE17_9_16_TRADE_TERMINAL_SAFE_SYSTEM_CHAT.md`.
