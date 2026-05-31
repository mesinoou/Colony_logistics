# Phase 17.9.9 - Logistics Office crash fix

## Fixes

- Fixed the Logistics Office/Freight Board tooltip crash caused by passing `UUID` objects directly to `Component.translatable`.
- Converted contract UUID arguments to strings in:
  - `FreightBoardScreen`
  - `ContainerDockScreen`
  - `TradeTerminalScreen`

## Notes

Minecraft 1.21.1 only accepts `Component`, `Number`, `Boolean`, or `String` values as translatable component arguments. UUID and ResourceLocation-like values must be converted to strings before being passed to translation components.

The Sable dependency declaration was intentionally left in the pre-17.9.9 state after Phase 17.9.9.1. Sable remains required in `neoforge.mods.toml`; this crash fix is limited to the tooltip translation argument issue.
