# Phase 17.9.3 - Assigned Freight Cancellation and Unlimited Deadlines

## Goals

- Let carriers cancel generated freight they already accepted.
- Make deadline window config values of `0` mean unlimited instead of immediate expiry.
- Keep the cancellation behavior configurable for multiplayer balance testing.

## Cancellation behavior

Generated freight can now be cancelled from the Logistics Office board when all of the following are true:

- The contract is `GENERATED_FREIGHT`.
- The viewer is the assigned carrier.
- The status is `ACCEPTED`, `PICKED_UP`, or `DELIVERED`.
- `balance.deadlines.generatedJobsAllowCarrierCancel = true`.

Inventory freight cancellation removes matching Freight Parcel items from the cancelling player's inventory when they are still present.

Container freight cancellation:

- Marks the saved contract as `CANCELLED`.
- Releases the origin colony active-container-job slot if at least one physical container had spawned.
- Removes matching containers found near the origin or destination Dock.
- Does not attempt an expensive global scan. If a Create/Aeronautics contraption has already moved a container far away, the physical container may remain in-world but can no longer be delivered because the contract is cancelled.

## New balance config

```toml
[balance.deadlines]
# Assigned carriers can cancel their own generated freight from the Logistics Office.
generatedJobsAllowCarrierCancel = true
# If true, cancellation increments failed jobs and applies failedJobReputationPenalty.
generatedJobsCancelCountsAsFailed = false
# If false, container contracts cannot be cancelled after any physical container has spawned.
generatedJobsAllowCancelAfterContainerSpawn = true
```

## Unlimited deadlines

The following generated-freight windows now treat `0` as unlimited:

```toml
pickupWindowTicks = 0
inventoryDeliveryWindowTicks = 0
standardContainerDeliveryWindowTicks = 0
largeContainerDeliveryWindowTicks = 0
heavyContainerDeliveryWindowTicks = 0
```

When a window is `0`, generated contracts store the corresponding deadline as `0`. Deadline validation now checks both the current TOML window and the saved deadline, so setting the TOML window to `0` also suppresses expiry for older saved contracts that still contain a non-zero deadline. The Logistics Office board displays `No deadline` / `無期限` for `expiresGameTime = 0`.

## Files touched

- `src/main/java/jp/colonylogistics/config/ColonyLogisticsConfig.java`
- `src/main/java/jp/colonylogistics/service/FreightMarketService.java`
- `src/main/java/jp/colonylogistics/service/ContractService.java`
- `src/main/java/jp/colonylogistics/network/CancelFreightPayload.java`
- `src/main/java/jp/colonylogistics/network/ModNetwork.java`
- `src/main/java/jp/colonylogistics/menu/FreightBoardRow.java`
- `src/main/java/jp/colonylogistics/client/screen/FreightBoardScreen.java`
- `src/main/java/jp/colonylogistics/dock/ContainerDockBlock.java`
- `src/main/java/jp/colonylogistics/command/LogisticsCommands.java`
- `src/main/resources/assets/colonylogistics/lang/en_us.json`
- `src/main/resources/assets/colonylogistics/lang/ja_jp.json`
- `config/colonylogistics-common.toml.example`
