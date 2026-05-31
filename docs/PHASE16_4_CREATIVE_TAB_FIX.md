# Phase 16.4 - Creative Tab Visibility Fix

The mod blocks/items were registered, but they were not inserted into any Creative Mode tab.
This made them difficult to find during test-play even though `/give` worked.

## Added to Functional Blocks

- Logistics Office
- Container Dock
- Trade Terminal
- Freight Board

## Added to Tools & Utilities

- Freight Parcel

## Intentionally hidden

- Freight Container Core
- Freight Container Part

Those container blocks remain hidden from creative tabs because contract containers should be
created and removed only through Container Dock logic or admin/debug commands.
