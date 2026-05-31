# Phase 12 - Player Trade Cancel / Refund

This phase adds the first safe rollback path for player-created escrow trades.

## Added

- `CancelPlayerTradePayload`
- `PlayerTradeService#cancelTrade`
- `PlayerTradeContract#cancel`
- Trade Terminal row ownership data
- Trade Terminal UI action split:
  - creator sees `Cancel`
  - other players see `Deliver`
- escrow reward refund to the creator
- server-side validation for cancellation

## Cancellation rules

A player trade can be cancelled only when all conditions are true:

- the Trade Terminal is active
- the contract exists
- the contract is still `OPEN`
- the contract belongs to that terminal
- the requesting player is the creator
- the escrow reward item still exists in the registry

On success:

- the escrow reward is returned to the creator inventory
- overflow is dropped at the creator
- the contract status becomes `CANCELLED`
- the cancelled contract disappears from open terminal rows

## Still intentionally limited

- cancelled/completed player trades are retained in SavedData for audit/debug
- there is no history tab yet
- refund still stores item id + count only; full ItemStack component preservation is not implemented yet
- open trade rows are still snapshot-based and refresh when the terminal is reopened
