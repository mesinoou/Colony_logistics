# Phase 17.9.4 - Currency Denomination Exchange Payouts

## Goal

Rewards are stored and balanced as a single base-coin amount, but players should receive the appropriate Trade Post coin denominations when the reward is paid.

## Defaults

- Base coin: `mctradepost:mctp_coin` = 1
- Gold coin: `mctradepost:mctp_coin_gold` = 8
- Diamond coin: `mctradepost:mctp_coin_diamond` = 64

## Config

The `[currency]` section now includes:

```toml
currencyExchangeEnabled = true
baseCoinItem = ""
goldCoinItem = "mctradepost:mctp_coin_gold"
goldCoinValue = 8
diamondCoinItem = "mctradepost:mctp_coin_diamond"
diamondCoinValue = 64
```

`baseCoinItem = ""` means the legacy-compatible `tradePostCurrencyItem` value is used as the base coin.

## Payout behavior

Generated freight reward values remain base-coin amounts. On payout, `CurrencyService` decomposes the base amount greedily using registered denominations sorted by value descending.

Example:

- Reward amount: 130
- Denominations: diamond=64, gold=8, base=1
- Paid stacks: `2 x mctradepost:mctp_coin_diamond`, `2 x mctradepost:mctp_coin`

If a higher denomination item is not registered because Trade Post is absent or a pack changed item ids, it is skipped. If the remaining amount cannot be represented by registered denominations, payout fails before contract mutation where applicable.

## Trade Terminal

When `playerTradeRewardsMustBeCurrency = true`, escrow accepts configured currency denominations. A deposited diamond-coin stack resolves to base-coin value for reward accounting, while the original escrow stack remains visible in trade history.

Delivery and cancel/refund messages now display the exchanged breakdown instead of only a single item id.

## Diagnostics

Use:

```text
/colonylogistics balance show
```

The command prints the active base currency, exchange toggle, and registered denomination values.
