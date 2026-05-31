# Phase 13 - Full ItemStack Escrow Persistence

## Goal

Phase 12 stored player trade escrow data as item id + count. That was enough for a prototype, but it lost important Minecraft 1.21.1 item state such as custom names, enchantments, damage, and data components.

Phase 13 changes player trades to store full `ItemStack` values for both:

- the requested item template
- the escrowed reward

## What changed

### PlayerTradeContract

`PlayerTradeContract` now stores:

```java
ItemStack requestedTemplate
ItemStack escrowedReward
```

instead of:

```java
ResourceLocation requestedItemId
int requestedCount
ResourceLocation rewardItemId
int rewardCount
```

Compatibility accessor methods are kept for GUI rows and debug display:

```java
requestedItemId()
requestedCount()
rewardItemId()
rewardCount()
```

### PlayerTradeNbt

Player trades now save stacks with the registry-aware ItemStack serialization API:

```java
RequestedStack
EscrowedReward
```

Legacy fields are still written for debugging and migration:

```java
RequestedItemId
RequestedCount
RewardItemId
RewardCount
```

Load supports both the new full-stack format and the old id/count format.

### Delivery matching

Delivery now uses exact item + component matching:

```java
ItemStack.isSameItemSameComponents(stack, template)
```

This means a player trade that requests a named, damaged, enchanted, or component-bearing item requires a matching item to deliver.

### Reward/refund behavior

Delivery and cancellation now pay the stored escrow `ItemStack` copy directly. This preserves:

- count
- custom name
- enchantments
- damage
- data components
- other serialized stack state

If the player's inventory is full, the full stack is dropped at the player as before.

## Remaining work

- Show component/exact-match indicators in the Trade Terminal GUI.
- Add a match mode toggle later if loose item-id matching is desired.
- Add history inspection for completed/cancelled player trades.
- Build-test against the exact NeoForge/MineColonies dev workspace.
