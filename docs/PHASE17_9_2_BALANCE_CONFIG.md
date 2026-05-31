# Phase 17.9.2 - Balance config surface

This phase exposes the main gameplay-balance values through `config/colonylogistics-common.toml` so balancing can continue without editing Java code.

## New / expanded TOML sections

### `[balance.buildingLevels.level0]` ... `[balance.buildingLevels.level5]`

Each Logistics Office level can now tune:

- `maxOpenFreightJobs`
- `maxContainerDocks`
- `maxActiveContainerJobs`
- `maxPlayerTradeContracts`
- `maxContainerStandard` (`none`, `standard`, `large`, `heavy`)
- `inventoryFreightEnabled`
- `containerFreightEnabled`

`ColonyLogisticsLimits.forBuildingLevel()` reads these values directly.

### `[balance.carrierRequirements]`

Controls the Carrier Level required to accept generated jobs:

- `inventoryRequiredCarrierLevel`
- `standardRequiredCarrierLevel`
- `largeRequiredCarrierLevel`
- `heavyRequiredCarrierLevel`

### `[balance.rewards.inventory]`

Controls inventory parcel reward calculation:

- base reward
- distance divisor and minimum distance bonus
- cargo value / weight / fragility multipliers
- global multiplier

### `[balance.rewards.container]`

Controls container reward calculation:

- base reward
- distance divisor and minimum distance bonus
- cargo value and weight multipliers
- container size volume/base-weight multipliers
- weight class bonuses
- Standard / Large / Heavy multipliers
- global multiplier

### `[balance.deadlines]`

Controls pickup windows, delivery windows, and late-delivery payout:

- `pickupWindowTicks`
- `inventoryDeliveryWindowTicks`
- `standardContainerDeliveryWindowTicks`
- `largeContainerDeliveryWindowTicks`
- `heavyContainerDeliveryWindowTicks`
- `generatedJobsAllowLateDelivery`
- `lateDeliveryRewardPercent`

Both inventory and container delivery paths now use `lateDeliveryRewardPercent` instead of the old hardcoded half reward.

### `[balance.containerGeneration]`

Controls generated container mix and contract size:

- `standardGenerationWeight`
- `largeGenerationWeight`
- `heavyGenerationWeight`
- `standardDefaultContainerCount`
- `largeDefaultContainerCount`
- `heavyDefaultContainerCount`
- `largeFragileContainerCount`

Setting a generation weight to `0` prevents that standard from being automatically generated.

### `[balance.carrierProfile]`

Controls progression and penalties:

- completed-job reputation gain from reward amount
- minimum reputation gain
- failure reputation penalty
- completed job and reputation thresholds for Carrier Levels 2-5

## In-game verification command

Use:

```text
/colonylogistics balance show
```

This prints the active TOML values for level limits, generation caps/weights, carrier requirements, and deadlines.

## Compatibility notes

The old keys `market.testInventoryJobCapPerColony` and `market.testContainerJobCapPerColony` are intentionally kept so existing configs do not break. They are now documented as normal generated-job cap balance knobs.

Manual GUI tuning files are not changed by this phase.
