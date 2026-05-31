# Phase 17.9.12 - Multiplayer Safety Pass 1

## Goal
Prepare the MineColonies UI-tab based Colony Logistics entry points for multiplayer use.

## Changes
- Hardened C2S payload handlers so actions must come from the matching open Colony Logistics menu.
  - Logistics Office freight accept/cancel requires an open `FreightBoardMenu` for the same office position.
  - Container Dock spawn/deliver requires an open `ContainerDockMenu` for the same dock position.
  - Trade Terminal create/deliver/cancel requires an open `TradeTerminalMenu` for the same terminal position.
- Added server-side proximity checks to Logistics Office and Trade Terminal action payloads.
- Added server-side block-type checks for Container Dock and Trade Terminal action payloads.
- Fixed menu `stillValid` checks to compare against the expected Colony Logistics Hut block instead of the current block at the position.
- Changed Trade Terminal setup slots to be menu-local rather than shared through `LogisticsMarketSavedData`.
  - This prevents one player from using or refunding another player's pending escrow input.
  - Request sample and unsubmitted reward are returned when the menu closes.
  - On successful trade creation, only the reward slot is consumed into the saved player-trade contract; the request sample is returned on close.

## Notes
- `TradeTerminalRuntimeState` remains readable/writable in SavedData for legacy compatibility, but new Trade Terminal menu setup no longer depends on that shared buffer.
- This phase is focused on packet/menu/escrow safety. MineColonies colony permission integration is a separate follow-up pass.
