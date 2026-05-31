# Phase 16.28 - Freight Board details pane

Phase 16.28 improves the Freight Board as a gameplay/debug surface after the basic
parcel, trade, and container delivery loops have been confirmed.

## Changes

- Freight Board rows now carry compact origin/destination position text in the menu snapshot.
- The client remembers the last Freight Board filter and scroll offset for the current client session.
- Clicking a visible row selects it.
- A details pane below the list shows:
  - short contract id
  - source/type/status
  - colony route and origin/destination positions
  - cargo and container requirement
  - reward, difficulty, and deadline game tick
  - assignee state

## Notes

The details pane is intentionally client-side and snapshot-based. Reopen the board after
changing contract state to refresh the server snapshot.
