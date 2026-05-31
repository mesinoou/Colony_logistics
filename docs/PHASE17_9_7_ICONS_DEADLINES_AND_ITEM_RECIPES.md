# Phase 17.9.7 - Parcel Icon, Style Pack Icon, Unlimited Deadlines, and Item Recipes

## Scope

This phase finishes several player-facing polish items before multiplayer preflight:

- Give the generated freight parcel its own item icon instead of using vanilla paper.
- Replace the placeholder 1x1 Structurize/MineColonies style-pack icon with a real Colony Logistics building-style image.
- Make generated job deadlines unlimited by default.
- Add a simple craft recipe for the freight parcel item and document the current recipe set.

## Freight Parcel Icon

`assets/colonylogistics/models/item/freight_parcel.json` now points to:

```text
assets/colonylogistics/textures/item/freight_parcel.png
```

The icon is a 32x32 parcel/label/twine pixel-art texture. Contract-assigned parcels still receive their route/contract NBT from the server when a freight contract is picked up.

Crafted parcels are blank and show the tooltip:

```text
Blank parcel: not assigned to a contract
```

They are not valid deliveries until stamped by an accepted contract.

## Style Pack Icon

The Build Tool style pack keeps:

```json
"icon": "icon.png"
```

but `blueprints/colony_logistics_dev/icon.png` is now a 128x128 Colony Logistics-themed image showing an office, terminal, container dock, route arrows, and coin motif. This keeps Structurize's icon requirement satisfied while making the style easier to identify in-game.

## Default Deadlines

The default generated-job deadline windows are now unlimited:

```toml
[balance.deadlines]
pickupWindowTicks = 0
inventoryDeliveryWindowTicks = 0
standardContainerDeliveryWindowTicks = 0
largeContainerDeliveryWindowTicks = 0
heavyContainerDeliveryWindowTicks = 0
```

Phase 17.9.3 already made `0` mean unlimited. This phase changes the defaults so newly generated config files start in unlimited mode. Servers that want deadlines can set positive tick values.

## Recipes

All player-facing recipes are intentionally simple and vanilla-only.

### Freight Parcel

```text
PPP
PSP
PPP
```

- `P` = Paper
- `S` = String
- Result: `colonylogistics:freight_parcel` x1

This produces a blank parcel. The Logistics Office still creates the contract-stamped parcel when a carrier accepts an inventory freight contract.

### Logistics Office

```text
PPP
CLC
WWW
```

- `P` = Paper
- `C` = Chest
- `L` = Lectern
- `W` = Oak Planks
- Result: `colonylogistics:logistics_office` x1

### Container Dock

```text
III
RCR
SSS
```

- `I` = Iron Ingot
- `R` = Rail
- `C` = Chest
- `S` = Smooth Stone
- Result: `colonylogistics:container_dock` x1

### Trade Terminal

```text
RGR
GCG
WWW
```

- `R` = Redstone
- `G` = Glass Pane
- `C` = Chest
- `W` = Oak Planks
- Result: `colonylogistics:trade_terminal` x1

### Freight Board

```text
PIP
 S 
 W 
```

- `P` = Paper
- `I` = Ink Sac
- `S` = Oak Sign
- `W` = Oak Planks
- Result: `colonylogistics:freight_board` x1

The standalone Freight Board remains a legacy/guide block and directs players to the Logistics Office for actual market access.

## Notes

Generated `freight_container_core` and `freight_container_part` blocks remain uncraftable because they are runtime contract multiblock pieces managed by Container Dock.
