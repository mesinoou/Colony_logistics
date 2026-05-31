# Phase 17.9.1 - Default rewards use Trade Post coin, not emerald

Phase 17.9 still allowed the old development fallback path to resolve generated
freight rewards to `minecraft:emerald` when the placeholder currency id was not
registered. Phase 17.9.1 removes that behavior.

## Changes

- Default generated reward currency is now `mctradepost:mctp_coin`.
- Old config value `tradepost:coin` is normalized to `mctradepost:mctp_coin`.
- Old config value `fallbackCurrencyItem = "minecraft:emerald"` is treated as a
  legacy placeholder and normalized to `mctradepost:mctp_coin`.
- `useFallbackCurrencyWhenMissing` now defaults to `false`.
- `CurrencyService` no longer has a hardcoded emerald last fallback.
- `ItemCurrencyAdapter.createCurrencyStack` returns `ItemStack.EMPTY` when the
  requested currency item is unavailable instead of creating emeralds.
- Generated-freight contracts are normalized when loaded and when inserted into
  `LogisticsMarketSavedData`, so old open generated contracts stop displaying
  emerald as the default reward.

## Game-test note

Existing worlds may still have an old `colonylogistics-common.toml`. The runtime
normalization handles the old values, but regenerating or updating the TOML is
recommended so the file clearly shows the new default:

```toml
[currency]
tradePostCurrencyItem = "mctradepost:mctp_coin"
fallbackCurrencyItem = "mctradepost:mctp_coin"
useFallbackCurrencyWhenMissing = false
playerTradeRewardsMustBeCurrency = true
```
