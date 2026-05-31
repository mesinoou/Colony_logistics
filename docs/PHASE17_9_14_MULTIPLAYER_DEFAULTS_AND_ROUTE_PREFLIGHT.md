# Phase 17.9.14 - Multiplayer defaults and route preflight

This pass switches the remaining single-player/local diagnostic defaults back to normal multiplayer behavior before beta testing.

## Config defaults

The following `[testing]` defaults are now `false`:

- `allowSelfDeliveryForTesting`
- `allowLoopbackFreightForTesting`
- `allowLoopbackContainerFreightForTesting`

This means:

- Trade Terminal creators cannot fulfill their own trades unless an admin temporarily enables the runtime override.
- Generated inventory freight requires at least two eligible Logistics Office colonies.
- Generated container freight requires at least two eligible colonies with usable Container Docks and compatible container standards.
- `/colonylogistics market localtest ...` is blocked unless `loopbackcontainer` is explicitly enabled for diagnostics.

The multiplayer beta logging defaults added in Phase 17.9.13 remain enabled:

- `debugMultiplayerNetworkLogging = true`
- `debugContractLifecycleLogging = true`

## Route generation preflight

Static route inspection confirms that normal generated contracts cannot choose the origin colony as the destination while loopback testing is disabled:

- Inventory generation builds the active colony list from colonies with a built Logistics Office and inventory freight enabled.
- If fewer than two eligible colonies exist and loopback is disabled, generation returns without creating jobs.
- Destination selection filters out the origin colony id before picking a route.
- Container generation additionally requires a registered Dock and container freight enabled for each eligible colony.
- Container size selection uses the common max standard of origin and destination, so Large/Heavy jobs are only generated when both sides can support them.

## Beta log additions

Generation skips caused by incomplete multiplayer setup now emit `[CL-MP][contract][generation-skip]` lines. Expected reasons include:

- `NEED_TWO_INVENTORY_COLONIES`
- `NEED_TWO_CONTAINER_COLONIES_WITH_DOCKS`
- `LOOPBACK_CONTAINER_TESTING_DISABLED`

These logs make it clear whether missing contracts are caused by route eligibility rather than packet/UI failures.
