# Phase 14 - Player Trade Match Modes

Phase 14 adds a selectable matching rule to player-created Trade Terminal contracts.

## New match modes

`ItemMatchMode` has two values:

- `ITEM_AND_COMPONENTS` - exact matching. The delivered stack must have the same item id and the same 1.21 data components as the request template. This remains the safe default.
- `ITEM_ONLY` - loose matching. The delivered stack only needs the same item id. Custom name, damage, enchantments, and other components are ignored for delivery matching.

## GUI changes

The Trade Terminal create action is split into two buttons:

- `Create Exact`
- `Create Item-ID`

Open trade rows also display the stored match mode, so delivery players can tell whether a request is strict or loose.

## Persistence

`PlayerTradeContract` now stores `ItemMatchMode`, and `PlayerTradeNbt` writes it as `MatchMode`.
Old Phase 13 saves do not contain this key and load as `ITEM_AND_COMPONENTS` to preserve prior behavior.

## Server validation

The client-provided match mode is only used at contract creation time and is saved server-side.
Delivery consumes matching stacks using the saved mode from the contract, not any client-side display state.
