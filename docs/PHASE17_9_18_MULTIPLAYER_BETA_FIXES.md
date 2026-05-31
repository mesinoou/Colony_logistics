# Phase 17.9.18 - Multiplayer Beta Fixes

## Problems reported
- Runtime freight container blocks could be broken during multiplayer testing.
- Player-to-player trades created from a Trade Terminal only appeared on the Logistics Office board of the colony where the trade was created.

## Fixes

### Runtime container block protection
`FreightContainerCoreBlock` and `FreightContainerPartBlock` now use unbreakable block hardness:

```java
strength(-1.0f, 3600000.0f)
```

Their player destroy progress always returns `0.0F`, and `playerDestroy(...)` no longer delegates to the vanilla break path for operators. Runtime containers should now be removed only by Colony Logistics service flows such as successful delivery, cancellation cleanup, or explicit admin/world-edit style commands.

`PushReaction.NORMAL` was intentionally kept so the transport/multiblock movement assumptions from earlier phases are not changed by this safety patch.

### Global visibility for open player trades
Logistics Office board snapshots no longer restrict open player-to-player trades to the creator colony.

Open player trades are now visible from every active Logistics Office, while finished player trade history remains visible from:
- the originating colony's office;
- the creator's own board view;
- the delivering player's board view.

The originating terminal and colony are still preserved in the row details, so the trade's source remains auditable in multiplayer beta logs and UI details.

## Verification performed in this workspace
- Confirmed the old player trade board filter was `trade.colonyId() == officeColonyId`.
- Replaced it with explicit global visibility for `OPEN` player trades.
- Confirmed runtime container block properties now use negative destroy time and high explosion resistance.
- Performed source-level diff and brace-balance checks.

Gradle/compileJava was not run in this workspace because the provided project ZIP does not include a Gradle wrapper and this environment does not have Gradle installed.
