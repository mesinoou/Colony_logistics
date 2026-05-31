# Phase 17.9.6 - Auto-generated job count and low-difficulty mix

Phase 17.9.6 increases the default number of generated freight contracts visible per origin colony and adds TOML-backed controls to keep low-difficulty contracts in the pool.

## Default pacing changes

The market top-up interval is now shorter by default:

```toml
[market]
generationIntervalTicks = 12000
```

The default target open jobs per origin colony were raised:

```toml
[market]
testInventoryJobCapPerColony = 12
testContainerJobCapPerColony = 6
```

The old key names are kept for compatibility with existing worlds, but these are now normal balance controls rather than test-only settings.

Building-level caps were also raised so the global market caps are no longer blocked by the per-building defaults:

- Logistics Office Level 1: inventory 6, container 0
- Level 2: inventory 10, container 2
- Level 3: inventory 14, container 4
- Level 4: inventory 20, container 6
- Level 5: inventory 30, container 10

The effective target remains:

```text
effective target = min(level cap, market cap)
```

## Low-difficulty generation mix

New settings:

```toml
[market]
lowDifficultyInventoryPercent = 50
lowDifficultyContainerPercent = 45
lowDifficultyInventoryMax = "normal"
lowDifficultyContainerMax = "normal"
```

These percentages are checked against the current OPEN pool for each origin colony and unit type when the market creates new jobs. Existing OPEN jobs are not rewritten, but the next generated jobs prefer low difficulty until the visible pool reaches the configured share.

For inventory jobs, a low-difficulty slot picks cargo whose calculated difficulty is at or below `lowDifficultyInventoryMax`.

For container jobs, a low-difficulty slot first prefers the Standard container tier when Standard is allowed, then picks cargo whose calculated difficulty is at or below `lowDifficultyContainerMax`. The default max is `normal` because Standard container jobs are normally NORMAL difficulty rather than EASY once container base weight is included.

If no cargo matches the selected low-difficulty max, the generator falls back to the normal catalog so generation does not stall.

## Verification

Use:

```text
/colonylogistics balance show
```

The command now reports:

- generation interval
- inventory/container caps
- low-difficulty percentages and max difficulty
- Standard/Large/Heavy generation weights
