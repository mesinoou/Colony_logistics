# Phase 9: Dock mode and currency config

## Currency config

Generated freight contracts now read their reward currency from the common config:

```toml
[currency]
tradePostCurrencyItem = "mctradepost:mctp_coin"
fallbackCurrencyItem = "mctradepost:mctp_coin"
useFallbackCurrencyWhenMissing = false
```

The configured item is intended to be the Trade Post for MineColonies currency item used by the modpack. Phase 17.9.1 changed the default to the Trade Post base coin and removed emerald as the silent default fallback. If the configured item is absent and fallback is disabled, payouts fail visibly instead of paying emerald.

Generated contracts are normalized to the active currency item when loaded or inserted into saved data. Player-created Trade Terminal contracts still keep their own escrow data.

## Dock mode

Container Docks now have three modes:

- `both`: export and import
- `export`: spawn containers only
- `import`: deliver containers only

The Container Dock GUI includes a mode button. Clicking it sends `SetDockModePayload`; the server validates the Dock and updates the BlockEntity. The GUI closes after the change so the player reopens a fresh snapshot.

Server logic also enforces mode rules:

- export-disabled Docks reject container spawn with `EXPORT_DISABLED`
- import-disabled Docks reject delivery with `IMPORT_DISABLED`

## Configurable delivery radius

Container delivery range is now configurable:

```toml
[dock]
deliveryRadius = 12.0
```

The range still goes through the Sable-aware delivery resolver.
