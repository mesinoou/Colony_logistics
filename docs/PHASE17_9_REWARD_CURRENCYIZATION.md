# Phase 17.9 - Reward currencyization and multiplayer-safe Trade Terminal rewards

Phase 17.9 finishes the reward-currency cleanup that was left after the
Container Dock and building-blueprint production passes.

## Goals

- Keep Trade Post for MineColonies integration limited to a currency item id.
- Route generated freight payouts and player-trade payouts through `RewardSpec`
  and `CurrencyService`.
- Restrict newly-created Trade Terminal rewards to the active currency item by
  default, so multiplayer testing does not allow arbitrary non-currency reward
  escrow.
- Avoid completing freight contracts before the server has confirmed that the
  configured payout currency can be resolved.

## Currency config

The existing config keys are still used:

```toml
[currency]
tradePostCurrencyItem = "mctradepost:mctp_coin"
fallbackCurrencyItem = "mctradepost:mctp_coin"
useFallbackCurrencyWhenMissing = false
```

A new multiplayer-safe switch was added:

```toml
playerTradeRewardsMustBeCurrency = true
```

When this is true, players may still request any item in the Trade Terminal, but
the reward slot must contain the active currency item only. As of Phase 17.9.1,
the default active currency is `mctradepost:mctp_coin`, the Trade Post for
MineColonies base coin. Old test configs using `tradepost:coin` or
`minecraft:emerald` are normalized to `mctradepost:mctp_coin` at runtime.

The active currency is resolved in this order:

1. `tradePostCurrencyItem`, after legacy placeholder normalization.
2. `fallbackCurrencyItem`, only if fallback is enabled and the fallback is registered.
3. If both are unavailable, payout fails visibly instead of silently using emerald.

Set `playerTradeRewardsMustBeCurrency = false` only when testing legacy full-item
escrow saves.

## Code changes

### `CurrencyService`

- Added `payableReward(...)` to resolve fallback currency before any contract
  state is mutated.
- Added `PaymentResult` so callers can record the actual item id and amount paid.
- Added `currencyRewardFromStack(...)` and `isAcceptedCurrencyStack(...)` for
  Trade Terminal escrow validation.
- Added `createCurrencyStack(...)` for normalized currency escrow storage.

### Generated freight completion

- Inventory freight checks `CurrencyService.payableReward(...)` before shrinking
  the parcel stack and completing the contract.
- Container freight checks the final payout before removing the final container
  and completing the contract.
- If the currency is unavailable and fallback is disabled, completion is refused
  with `message.colonylogistics.currency.unavailable` instead of silently losing
  the payout.

### Trade Terminal

- New player-created trades require the escrow reward stack to be the active
  currency item when `playerTradeRewardsMustBeCurrency = true`.
- The stored escrow stack is normalized to a currency stack; custom components on
  currency items do not affect the payout.
- Deliver and cancel now pay/refund through `CurrencyService` using
  `PlayerTradeContract.rewardSpec()`.
- Legacy saved contracts still load through the existing full `ItemStack` NBT
  shape, but new multiplayer-safe creation is currency-only by default.

## Manual GUI preservation

No changes were made to the manually tuned GUI Java files:

- `ContainerDockScreen.java`
- `FreightBoardScreen.java`
- `TradeTerminalScreen.java`
- `TradeTerminalMenu.java`

## Test notes

Recommended in-game checks:

1. Confirm generated freight rewards show `mctradepost:mctp_coin` by default,
   not emerald.
2. Temporarily set the currency id to a missing item and confirm delivery is
   refused before the parcel or final container is consumed.
3. Enable fallback with a non-emerald valid currency item and confirm payouts use
   that configured fallback.
4. Try to create a Trade Terminal trade with a non-currency reward item and
   confirm creation is rejected.
5. Create a Trade Terminal trade with the currency item, deliver it, and confirm
   the reward is paid as currency.
