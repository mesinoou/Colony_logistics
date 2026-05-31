# Phase 17.9.11.1 - MineColonies UI Tab Compile Fix

## Summary

Fixes compile errors introduced by the Phase 17.9.11 MineColonies building UI tab migration.

## Fixes

- `ColonyLogisticsBuildingModuleView#deserialize` now uses `RegistryFriendlyByteBuf`, matching MineColonies 1.21.1 / 1.1.1300.
- `ModMineColoniesBuildings#moduleProducer` now gives the nested MineColonies module view supplier an explicit type.
  - This avoids javac inferring the outer raw `Supplier#get` return as `Object` and rejecting the inner lambda.

## Preserved behavior

- MineColonies building-window tab migration remains enabled.
- Existing Colony Logistics screens and menus remain unchanged.
- Standalone Freight Board remains removed.
- Sable dependency setting remains as restored in Phase 17.9.9.1.
