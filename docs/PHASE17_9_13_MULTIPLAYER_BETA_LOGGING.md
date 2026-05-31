# Phase 17.9.13 - Multiplayer Beta Logging

This phase adds server-side debug logging for multiplayer beta tests.

## Config

Two common config flags were added under `[testing]` and default to `true` for the beta build:

- `debugMultiplayerNetworkLogging`
  - Logs C2S packet receipt, validation rejects, and final server-side packet results.
  - Prefix: `[CL-MP][network]`
- `debugContractLifecycleLogging`
  - Logs generated freight, accepted/cancelled/delivered freight, container spawn/delivery, and Trade Terminal escrow lifecycle events.
  - Prefixes: `[CL-MP][contract]`, `[CL-MP][container]`, `[CL-MP][trade]`

Disable these flags on production servers if the beta logs become too noisy.

## Logged network actions

- Opening Colony Logistics menus from the MineColonies building tab
- Accepting generated freight from the Logistics Office board
- Cancelling accepted generated freight
- Spawning accepted contract containers from a Container Dock
- Delivering sealed containers to a Container Dock
- Creating Trade Terminal escrow trades
- Delivering Trade Terminal trades
- Cancelling Trade Terminal trades

## Logged lifecycle actions

- Auto-generated inventory freight contracts
- Auto-generated container freight contracts
- Local container test jobs
- Inventory freight accept/deliver/reject paths
- Container freight accept/cancel/spawn/deliver/reject paths
- Trade Terminal create/deliver/cancel/reject paths

## Log format notes

Logs include player name, player UUID, dimension, relevant block positions, contract/trade UUIDs, status/result codes, colonies, reward currency, container batch progress, and basic item stack summaries.
