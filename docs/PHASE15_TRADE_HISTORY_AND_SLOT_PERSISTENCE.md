# Phase 15 - Trade Terminal history and slot persistence

## Summary

Phase 15 improves player trade operations after the escrow flow is complete.

Added:

- Recent completed / cancelled / failed trade history in the Trade Terminal screen.
- Separate open-trade and history snapshots in `TradeTerminalMenu`.
- `TradeTerminalRow` now carries `ContractStatus`.
- `LogisticsMarketSavedData#finishedPlayerTradesForTerminal` for terminal-local history.
- Full `ItemStack` persistence for the request / reward input slots inside the terminal BlockEntity.
- A purge helper for finished player trades.

## UI behavior

The Trade Terminal now shows two sections:

- Open player trades: actionable rows with Deliver or Cancel buttons.
- Recent terminal history: read-only rows showing Completed, Cancelled, Failed, or Expired.

The history is a snapshot taken when the menu opens. Live refresh and paging are still future work.

## Persistence

Before Phase 15, the Trade Terminal's input slots were saved as item id + count. Phase 15 now stores each slot as a full `ItemStack` NBT payload using registry-aware item stack serialization.

This preserves:

- custom names
- enchantments
- damage
- 1.21 data components
- other stack metadata

Legacy id + count fields are still written as debug / migration fallback and still read if the new `Stack` compound is absent.

## Remaining work

- Live menu refresh after accepting / cancelling / delivering.
- Pagination for terminals with many open or finished trades.
- Long-term history retention policy exposed in config.
