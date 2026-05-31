# Phase 5: Sable-aware delivery range

This phase wires Sable Companion into the container delivery boundary.

## Why

When a Freight Container is carried by a Sable/Create Aeronautics sub-level, its local block coordinates may no longer be close to the destination Dock in vanilla coordinate space. Direct `Vec3#distanceToSqr` can report an enormous distance even if the vehicle is visually parked beside the Dock.

## Implementation

- `DeliveryRangeResolvers.current()` now defaults to `SableCompanionDeliveryRangeResolver`.
- `SableCompanionDeliveryRangeResolver` delegates to `SableCompanion.INSTANCE.distanceSquaredWithSubLevels`.
- `ContainerDockService.deliverContainer` now rejects container delivery if the Sable-aware distance between Dock and Core is greater than `DEFAULT_DELIVERY_RADIUS`.
- Debug command added:

```mcfunction
/colonylogistics container range <dock> <core>
```

## Current limitation

The debug command still requires the Core position explicitly. A later phase should add a Dock-side scan that uses Sable Companion's sub-level search utilities so the player can press a Dock GUI button instead of entering coordinates.
