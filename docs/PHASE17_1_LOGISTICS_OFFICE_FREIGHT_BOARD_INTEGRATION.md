# Phase 17.1 - Logistics Office freight board integration

The functional Freight Board has been moved into the MineColonies Logistics Office.

## Gameplay changes

- Right-clicking a Logistics Office opens the freight/trade board snapshot scoped to that office's colony.
- Freight contracts shown in that office are limited to contracts whose origin or destination colony matches the office colony.
- Player trade rows shown in that office are limited to trades belonging to that colony.
- Accepting a freight contract now sends the Logistics Office position to the server.
- The server validates that the accept request came from a usable Logistics Office and that the contract is related to that office colony.
- The standalone Freight Board block no longer opens the market and is removed from the creative tab.

## Notes

The old Freight Board block remains registered for now, but it only displays a message directing players to the Logistics Office.
