# Phase 16.13 - Hut right-click functional GUI fix

## Problem

After Phase 16.12, MineColonies hut registration works and multiple Container Dock / Trade Terminal buildings can be placed. However, right-clicking the hut core while holding an item can still open the default MineColonies building GUI first. That prevents access to Colony Logistics' functional Container Dock / Trade Terminal menus.

## Cause

Minecraft / NeoForge 1.21.1 routes block interactions with a held item through `BlockBehaviour#useItemOn` before `BlockBehaviour#useWithoutItem`. `AbstractBlockHut` handles that path for the MineColonies management GUI, so the Colony Logistics `useWithoutItem` override is not always reached.

## Fix

`ContainerDockBlock` and `TradeTerminalBlock` now override both interaction paths:

- `useItemOn(...)`
- `useWithoutItem(...)`

Both delegate to the same menu-opening method:

- `openContainerDockMenu(...)`
- `openTradeTerminalMenu(...)`

This keeps the MineColonies hut / building registration model from Phase 16.12, while restoring direct access to Colony Logistics functional GUIs from the hut core.

## Files changed

- `src/main/java/jp/colonylogistics/dock/ContainerDockBlock.java`
- `src/main/java/jp/colonylogistics/terminal/TradeTerminalBlock.java`

## Verification

1. Build:
   - `./gradlew build` or `gradlew.bat build`
2. Launch client:
   - `./gradlew runClient` or `gradlew.bat runClient`
3. Place via MineColonies Build Tool:
   - Container Dock
   - Trade Terminal
4. Right-click each hut core while holding an ordinary item.
5. Expected result:
   - Container Dock opens the Colony Logistics Container Dock GUI.
   - Trade Terminal opens the Colony Logistics Trade Terminal GUI.
   - The old "building not found" message must not appear.
6. Repeat with multiple Container Docks and multiple Trade Terminals in the same colony.
