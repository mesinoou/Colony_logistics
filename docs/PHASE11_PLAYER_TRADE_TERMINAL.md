# Phase 11 - Trade Terminal / Player Trade Escrow

This phase adds the first playable player-to-player trade loop.

## Added

- `Trade Terminal` MineColonies hut block and building registry entry.
- `TradeTerminalBlockEntity` with two setup slots:
  - request sample slot
  - escrow reward slot
- `TradeTerminalMenu` / `TradeTerminalScreen`.
- `PlayerTradeContract` saved in `LogisticsMarketSavedData`.
- `CreatePlayerTradePayload` and `DeliverPlayerTradePayload`.
- `PlayerTradeService` server-side validation.

## MVP trade flow

1. Build or bind a Trade Terminal in a colony.
2. Place the requested item stack in the request sample slot.
3. Place the reward item stack in the escrow reward slot.
4. Press `Create Trade`.
5. The reward stack is removed from the terminal and saved as escrow in the contract.
6. Another player opens the same terminal and presses `Deliver` on an open trade.
7. The server removes the requested item count from the delivering player's inventory.
8. The escrow reward is paid to that player.
9. The trade becomes `COMPLETED`.

## Current limitations

- Item matching is registry-id + count only.
- Components/NBT are not matched yet.
- Delivery is immediate; there is no separate accepted state for player trades yet.
- Rewards are stored as item id + count, not full component-preserving stacks.
- Completed player trades are retained in SavedData for audit/debug visibility.

## Next steps

- Add component/NBT matching options.
- Add cancellation/refund flow for open trades.
- Add Trade Terminal paging and live refresh.
- Add player-created container-delivery trade contracts.
