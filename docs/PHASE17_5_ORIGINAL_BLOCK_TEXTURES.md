# Phase 17.5 - Original block textures

This phase replaces placeholder vanilla block textures with original 32x32 face textures for Colony Logistics blocks.

## Implemented blocks

- Logistics Office
- Container Dock
- Trade Terminal
- Freight Board

Each block now uses a `minecraft:block/cube` model with separate front/back/side/top/bottom textures.

## Container Dock note

The Container Dock visual texture is intentionally prioritized over functional footprint. Container spawn and recognition offsets will be adjusted separately, so this phase does not change block shape, multiblock placement offsets, or container recognition radius.

## Texture files

Texture files are in:

`src/main/resources/assets/colonylogistics/textures/block/`

The preview sheet is in:

`docs/texture_preview/phase17_5_texture_net_preview.png`

All texture PNGs are 32x32.

## GUI baseline

The manually adjusted GUI files uploaded after Phase 17.4 are preserved:

- `ContainerDockScreen.java`
- `FreightBoardScreen.java`
- `TradeTerminalScreen.java`
- `TradeTerminalMenu.java`
