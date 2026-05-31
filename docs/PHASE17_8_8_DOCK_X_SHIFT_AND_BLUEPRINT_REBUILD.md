# Phase 17.8.8 - Container Dock X alignment and production blueprint rebuild

This phase keeps the in-game confirmed Phase 17.8.6/17.8.7 container footprint/orientation and makes the final one-block X-axis correction requested after test play.

## Container spawn alignment

Contract-safe indoor Container Dock candidate centers are now:

- `(4, 2, 2)`
- `(8, 2, 2)`
- `(12, 2, 2)`
- `(4, 2, 10)`
- `(8, 2, 10)`
- `(12, 2, 10)`

The unified physical footprint remains 3 deep x 7 wide x 3 high. The first three containers still spawn in the nearest Dock-side row, one block apart, but are shifted one block toward the Container Dock/control side on X as well as the previous one-block Z shift.

Contract spawning remains indoor-only. Debug fallback/ring candidates are still available for diagnostics, but accepted contracts use only the contract-safe indoor list.

## Container Dock blueprints

Container Dock Level 1 through Level 4 were rebuilt to reserve the same production footprint as Level 5:

- size: `19 x 10 x 19`
- hut anchor: `(3, 1, 3)`
- `blueprintDataProvider` retained
- root blueprint copies and `huts/` copies regenerated together

Lower levels now visually progress inside the same reserved Dock footprint instead of expanding the plot on upgrade. This prevents future upgrades from colliding with containers, Create contraptions, colony roads, or adjacent buildings.

## Trade Terminal blueprints

Trade Terminal Level 1 through Level 5 were replaced with production-style marketplace/escrow hall blueprints:

- fixed footprint: `15 x 8 x 13`
- hut anchor: `(3, 1, 3)`
- `blueprintDataProvider` retained
- root blueprint copies and `huts/` copies regenerated together

The new design keeps Trade Terminal as a separate building from Logistics Office and provides a progression from open market stall to secure exchange hall with queue space, teller counter, trade board, ledger/history wall, and back escrow vault.

## Notes

The manually tuned GUI files were not modified. Gradle compilation still needs to be run in a local environment with dependency jars because the distributed ZIP does not include `gradlew`.
