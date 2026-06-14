# Issue #2: Lightman's Currency migration

Colony Logistics now treats Lightman's Currency as the required reward currency mod.

## Runtime model

- Generated freight rewards are stored as base-coin value.
- The default base coin is `lightmanscurrency:coin_copper`.
- Payouts can be decomposed into configured denominations before items are given.
- The default denominations are:
  - `lightmanscurrency:coin_gold` = 100 base coins
  - `lightmanscurrency:coin_diamond` = 10000 base coins

The implementation remains item-id based. Colony Logistics does not call Lightman's Currency wallet or economy APIs directly.

## Config

Use `config/colonylogistics-common.toml` to match server currency policy:

```toml
[currency]
baseCoinItem = "lightmanscurrency:coin_copper"
goldCoinItem = "lightmanscurrency:coin_gold"
goldCoinValue = 100
diamondCoinItem = "lightmanscurrency:coin_diamond"
diamondCoinValue = 10000
```

If the server changes Lightman's Currency Master Coin List, update these values to the same exchange rates.

## Legacy migration

Old reward IDs are normalized at runtime:

```text
tradepost:coin -> lightmanscurrency:coin_copper
minecraft:emerald -> lightmanscurrency:coin_copper
mctradepost:mctp_coin -> lightmanscurrency:coin_copper
mctradepost:mctp_coin_gold -> lightmanscurrency:coin_gold
mctradepost:mctp_coin_diamond -> lightmanscurrency:coin_diamond
```

Saved rewards using the old Trade Post gold and diamond defaults are converted back to base value using the old ratios:

```text
mctradepost:mctp_coin_gold = 8 base coins
mctradepost:mctp_coin_diamond = 64 base coins
```

Old TOML files that still point `goldCoinItem` / `diamondCoinItem` at the Trade Post defaults are read as Lightman's Currency gold/diamond coins. If those files still use the old default values `8` and `64`, the runtime reads them as `100` and `10000`.

## Verification

- `./gradlew compileJava`
- Start a test client/server with Lightman's Currency installed.
- Confirm `/colonylogistics balance show` reports `lightmanscurrency:coin_copper` as the base currency.
- Accept and complete an inventory delivery and a container delivery.
- Create, cancel, and fulfill a Trade Terminal listing using configured Lightman's Currency coins.
