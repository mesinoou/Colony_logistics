# Phase 16.18 - Inventory Freight Single-Player Test Flow

Goal: make the generated Freight Board parcel loop testable by one player before full multi-player and multi-colony balancing.

## Added behavior

- `FreightParcelItem` can now be delivered by right-clicking a destination Logistics Office / logistics building while holding the parcel.
- `/colonylogistics freight deliver` remains as a debug fallback and still completes the held parcel without a clicked block check.
- New testing config: `testing.allowLoopbackFreightForTesting`.
  - Default: `true`.
  - If only one active Logistics Office exists, generated inventory freight may use the same colony as origin and destination.
  - Set to `false` when testing normal multi-colony routes.
- Freight parcel tooltip now shows the destination block position.

## Suggested test

1. Build or bind at least one Logistics Office.
2. Open a Freight Board; generated inventory parcel contracts should appear even with only one active colony while the testing config is enabled.
3. Accept an `INVENTORY_ITEM` job.
4. Confirm the parcel appears in the player's inventory and the board shows it as `PICKED_UP / self` after reopening.
5. Hold the parcel and right-click the destination Logistics Office or another logistics building in the destination colony.
6. Confirm the parcel is consumed, the reward is paid, the contract becomes `COMPLETED`, and `/colonylogistics profile me` increments completed freight stats.

## Production note

This phase intentionally keeps single-player test helpers enabled by default. Before multiplayer balance tests, set:

```toml
[testing]
allowSelfDeliveryForTesting = false
allowLoopbackFreightForTesting = false
```
