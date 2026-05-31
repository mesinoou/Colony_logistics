# Phase 16.24 - Freight Board filters and test-play cleanup

## Purpose

Phase 16.23.1 confirmed the end-to-end single-player flows for Trade Terminal, inventory parcel freight, and Container Dock freight including Create Aeronautics entity conversion and multiblock restoration.

Phase 16.24 focuses on making ongoing test-play less noisy:

- The Freight Board now has client-side tabs/filters.
- The existing scrollable snapshot is preserved.
- Test-play config keys remain grouped and documented as temporary helpers.
- Legacy Hut BlockEntity cleanup is intentionally deferred to avoid world-compatibility risk while gameplay testing is still active.

## Freight Board filters

The board now has these tabs:

- All
- Open
- Mine
- Parcel
- Container
- Trade
- History

The server still sends the same unified snapshot of `LogisticsContract` and `PlayerTradeContract` rows. Filtering is client-side only, so no SavedData migration is required.

`Accept` remains enabled only for open generated freight rows. Trade Terminal rows remain display-only from the Freight Board; they are still operated from the Trade Terminal GUI.

## Test settings

The following config keys remain intentionally enabled by default during early single-player testing:

```toml
[testing]
allowSelfDeliveryForTesting = true
allowLoopbackFreightForTesting = true
allowLoopbackContainerFreightForTesting = true
```

For multiplayer or balance testing, set them to false.

Generated job counts remain capped to keep the Freight Board readable:

```toml
[market]
testInventoryJobCapPerColony = 4
testContainerJobCapPerColony = 2
```

Raise these later when testing broader market pacing.

## Validation checklist

1. Open the Freight Board with a mix of generated freight and Trade Terminal contracts.
2. Confirm that All shows every row in the snapshot.
3. Confirm that Open only shows open rows.
4. Confirm that Mine shows rows assigned to or created by the current player.
5. Confirm that Parcel only shows inventory freight.
6. Confirm that Container only shows container multiblock freight.
7. Confirm that Trade only shows Trade Terminal contracts.
8. Confirm that History shows completed/cancelled/expired/failed rows.
9. Confirm that mouse-wheel scrolling still works inside each filter.
10. Confirm that Accept is only enabled for open generated freight rows.
