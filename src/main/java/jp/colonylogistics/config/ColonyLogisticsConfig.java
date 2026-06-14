package jp.colonylogistics.config;

import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.ContainerStandard;
import jp.colonylogistics.freight.FreightDifficulty;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Common/server configuration for integration points, test-play pacing, and balance tuning. */
public final class ColonyLogisticsConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<String> LEGACY_TRADE_POST_CURRENCY_ITEM;
    private static final ModConfigSpec.ConfigValue<String> FALLBACK_CURRENCY_ITEM;
    private static final ModConfigSpec.BooleanValue USE_FALLBACK_CURRENCY_WHEN_MISSING;
    private static final ModConfigSpec.BooleanValue PLAYER_TRADE_REWARDS_MUST_BE_CURRENCY;
    private static final ModConfigSpec.BooleanValue CURRENCY_EXCHANGE_ENABLED;
    private static final ModConfigSpec.ConfigValue<String> BASE_COIN_ITEM;
    private static final ModConfigSpec.ConfigValue<String> GOLD_COIN_ITEM;
    private static final ModConfigSpec.IntValue GOLD_COIN_VALUE;
    private static final ModConfigSpec.ConfigValue<String> DIAMOND_COIN_ITEM;
    private static final ModConfigSpec.IntValue DIAMOND_COIN_VALUE;

    private static final ModConfigSpec.DoubleValue DOCK_DELIVERY_RADIUS;
    private static final ModConfigSpec.DoubleValue DOCK_CONTAINER_RECOGNITION_RADIUS;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_DOCK_HALF_X;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_DOCK_HALF_Z;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_DOCK_OCCUPIED_HEIGHT;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_HORIZONTAL_GAP;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_BOTTOM_Y_OFFSET;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_TOP_GAP;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_EXTRA_RING_GAP;
    private static final ModConfigSpec.BooleanValue DOCK_CONTAINER_SPAWN_APRON_GRID_ENABLED;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_APRON_START_X;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_APRON_START_Z;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_APRON_COLUMNS;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_APRON_ROWS;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_APRON_SPACING_X;
    private static final ModConfigSpec.IntValue DOCK_CONTAINER_SPAWN_APRON_SPACING_Z;

    private static final ModConfigSpec.BooleanValue AUTO_GENERATE_MARKET_JOBS;
    private static final ModConfigSpec.IntValue MARKET_GENERATION_INTERVAL_TICKS;
    private static final ModConfigSpec.BooleanValue AUTO_GENERATE_INVENTORY_JOBS;
    private static final ModConfigSpec.BooleanValue AUTO_GENERATE_CONTAINER_JOBS;
    private static final ModConfigSpec.IntValue MARKET_TEST_INVENTORY_JOB_CAP_PER_COLONY;
    private static final ModConfigSpec.IntValue MARKET_TEST_CONTAINER_JOB_CAP_PER_COLONY;
    private static final ModConfigSpec.IntValue MARKET_LOW_DIFFICULTY_INVENTORY_PERCENT;
    private static final ModConfigSpec.IntValue MARKET_LOW_DIFFICULTY_CONTAINER_PERCENT;
    private static final ModConfigSpec.ConfigValue<String> MARKET_LOW_DIFFICULTY_INVENTORY_MAX;
    private static final ModConfigSpec.ConfigValue<String> MARKET_LOW_DIFFICULTY_CONTAINER_MAX;
    private static final ModConfigSpec.IntValue MARKET_PURGE_INTERVAL_TICKS;

    private static final ModConfigSpec.IntValue[] LEVEL_MAX_OPEN_FREIGHT_JOBS = new ModConfigSpec.IntValue[6];
    private static final ModConfigSpec.IntValue[] LEVEL_MAX_CONTAINER_DOCKS = new ModConfigSpec.IntValue[6];
    private static final ModConfigSpec.IntValue[] LEVEL_MAX_ACTIVE_CONTAINER_JOBS = new ModConfigSpec.IntValue[6];
    private static final ModConfigSpec.IntValue[] LEVEL_MAX_PLAYER_TRADE_CONTRACTS = new ModConfigSpec.IntValue[6];
    private static final ModConfigSpec.ConfigValue<String>[] LEVEL_MAX_CONTAINER_STANDARD = new ModConfigSpec.ConfigValue[6];
    private static final ModConfigSpec.BooleanValue[] LEVEL_INVENTORY_FREIGHT_ENABLED = new ModConfigSpec.BooleanValue[6];
    private static final ModConfigSpec.BooleanValue[] LEVEL_CONTAINER_FREIGHT_ENABLED = new ModConfigSpec.BooleanValue[6];

    private static final ModConfigSpec.IntValue INVENTORY_REQUIRED_CARRIER_LEVEL;
    private static final ModConfigSpec.IntValue STANDARD_REQUIRED_CARRIER_LEVEL;
    private static final ModConfigSpec.IntValue LARGE_REQUIRED_CARRIER_LEVEL;
    private static final ModConfigSpec.IntValue HEAVY_REQUIRED_CARRIER_LEVEL;

    private static final ModConfigSpec.IntValue INVENTORY_REWARD_BASE;
    private static final ModConfigSpec.IntValue INVENTORY_REWARD_MIN_DISTANCE_BONUS;
    private static final ModConfigSpec.IntValue INVENTORY_REWARD_DISTANCE_DIVISOR;
    private static final ModConfigSpec.DoubleValue INVENTORY_REWARD_CARGO_VALUE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue INVENTORY_REWARD_CARGO_WEIGHT_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue INVENTORY_REWARD_FRAGILITY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue INVENTORY_REWARD_GLOBAL_MULTIPLIER;

    private static final ModConfigSpec.IntValue CONTAINER_REWARD_BASE;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_MIN_DISTANCE_BONUS;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_DISTANCE_DIVISOR;
    private static final ModConfigSpec.DoubleValue CONTAINER_REWARD_CARGO_VALUE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CONTAINER_REWARD_CARGO_WEIGHT_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CONTAINER_REWARD_SIZE_VOLUME_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CONTAINER_REWARD_SIZE_BASE_WEIGHT_MULTIPLIER;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_EMPTY_WEIGHT_BONUS;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_LIGHT_WEIGHT_BONUS;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_MEDIUM_WEIGHT_BONUS;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_HEAVY_WEIGHT_BONUS;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_SUPER_HEAVY_WEIGHT_BONUS;
    private static final ModConfigSpec.IntValue CONTAINER_REWARD_EXTREME_WEIGHT_BONUS;
    private static final ModConfigSpec.DoubleValue STANDARD_REWARD_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue LARGE_REWARD_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue HEAVY_REWARD_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CONTAINER_REWARD_GLOBAL_MULTIPLIER;

    private static final ModConfigSpec.IntValue PICKUP_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue INVENTORY_DELIVERY_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue STANDARD_DELIVERY_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue LARGE_DELIVERY_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue HEAVY_DELIVERY_WINDOW_TICKS;
    private static final ModConfigSpec.BooleanValue GENERATED_JOBS_ALLOW_LATE_DELIVERY;
    private static final ModConfigSpec.IntValue LATE_DELIVERY_REWARD_PERCENT;
    private static final ModConfigSpec.BooleanValue GENERATED_JOBS_ALLOW_CARRIER_CANCEL;
    private static final ModConfigSpec.BooleanValue GENERATED_JOBS_CANCEL_COUNTS_AS_FAILED;
    private static final ModConfigSpec.BooleanValue GENERATED_JOBS_ALLOW_CANCEL_AFTER_CONTAINER_SPAWN;

    private static final ModConfigSpec.IntValue STANDARD_GENERATION_WEIGHT;
    private static final ModConfigSpec.IntValue LARGE_GENERATION_WEIGHT;
    private static final ModConfigSpec.IntValue HEAVY_GENERATION_WEIGHT;
    private static final ModConfigSpec.IntValue STANDARD_DEFAULT_CONTAINER_COUNT;
    private static final ModConfigSpec.IntValue LARGE_DEFAULT_CONTAINER_COUNT;
    private static final ModConfigSpec.IntValue HEAVY_DEFAULT_CONTAINER_COUNT;
    private static final ModConfigSpec.IntValue LARGE_FRAGILE_CONTAINER_COUNT;

    private static final ModConfigSpec.IntValue REPUTATION_GAIN_REWARD_DIVISOR;
    private static final ModConfigSpec.IntValue MIN_REPUTATION_GAIN_PER_JOB;
    private static final ModConfigSpec.IntValue FAILED_JOB_REPUTATION_PENALTY;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_2_COMPLETED_JOBS;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_2_REPUTATION;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_3_COMPLETED_JOBS;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_3_REPUTATION;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_4_COMPLETED_JOBS;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_4_REPUTATION;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_5_COMPLETED_JOBS;
    private static final ModConfigSpec.IntValue CARRIER_LEVEL_5_REPUTATION;

    private static final ModConfigSpec.BooleanValue ALLOW_SELF_DELIVERY_FOR_TESTING;
    private static final ModConfigSpec.BooleanValue ALLOW_LOOPBACK_FREIGHT_FOR_TESTING;
    private static final ModConfigSpec.BooleanValue ALLOW_LOOPBACK_CONTAINER_FREIGHT_FOR_TESTING;
    private static final ModConfigSpec.BooleanValue DEBUG_MULTIPLAYER_NETWORK_LOGGING;
    private static final ModConfigSpec.BooleanValue DEBUG_CONTRACT_LIFECYCLE_LOGGING;

    /** Runtime-only overrides for in-world test sessions. */
    private static Boolean overrideAutoGenerateMarketJobs;
    private static Boolean overrideAutoGenerateInventoryJobs;
    private static Boolean overrideAutoGenerateContainerJobs;
    private static Integer overrideMarketTestInventoryJobCapPerColony;
    private static Integer overrideMarketTestContainerJobCapPerColony;
    private static Boolean overrideAllowSelfDeliveryForTesting;
    private static Boolean overrideAllowLoopbackFreightForTesting;
    private static Boolean overrideAllowLoopbackContainerFreightForTesting;

    private static final ResourceLocation DEFAULT_LIGHTMANS_BASE_COIN = ResourceLocation.fromNamespaceAndPath("lightmanscurrency", "coin_copper");
    private static final ResourceLocation DEFAULT_LIGHTMANS_GOLD_COIN = ResourceLocation.fromNamespaceAndPath("lightmanscurrency", "coin_gold");
    private static final ResourceLocation DEFAULT_LIGHTMANS_DIAMOND_COIN = ResourceLocation.fromNamespaceAndPath("lightmanscurrency", "coin_diamond");
    private static final ResourceLocation LEGACY_TRADE_POST_CURRENCY = ResourceLocation.fromNamespaceAndPath("mctradepost", "mctp_coin");
    private static final ResourceLocation LEGACY_TRADE_POST_GOLD_COIN = ResourceLocation.fromNamespaceAndPath("mctradepost", "mctp_coin_gold");
    private static final ResourceLocation LEGACY_TRADE_POST_DIAMOND_COIN = ResourceLocation.fromNamespaceAndPath("mctradepost", "mctp_coin_diamond");
    private static final ResourceLocation LEGACY_PLACEHOLDER_CURRENCY = ResourceLocation.fromNamespaceAndPath("tradepost", "coin");
    private static final ResourceLocation LEGACY_EMERALD_FALLBACK = ResourceLocation.withDefaultNamespace("emerald");

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("currency");
        LEGACY_TRADE_POST_CURRENCY_ITEM = builder
                .comment(
                        "Legacy key kept so old configs still load. Prefer baseCoinItem for new packs.",
                        "Old Trade Post values are normalized to Lightman's Currency coins at runtime."
                )
                .define("tradePostCurrencyItem", "lightmanscurrency:coin_copper");
        FALLBACK_CURRENCY_ITEM = builder
                .comment(
                        "Optional fallback reward item when the configured Lightman's Currency item is not registered.",
                        "Old minecraft:emerald and Trade Post values are normalized to Lightman's Currency coins."
                )
                .define("fallbackCurrencyItem", "lightmanscurrency:coin_copper");
        USE_FALLBACK_CURRENCY_WHEN_MISSING = builder
                .comment(
                        "If false, payouts fail instead of silently using a fallback when the configured currency is missing.",
                        "Default false keeps missing Lightman's Currency installs visible during multiplayer prep."
                )
                .define("useFallbackCurrencyWhenMissing", false);
        PLAYER_TRADE_REWARDS_MUST_BE_CURRENCY = builder
                .comment(
                        "When true, Trade Terminal escrow rewards must be a configured currency denomination only.",
                        "This is the normal multiplayer-safe mode: players can still request any item, but rewards are paid as Lightman's Currency coins.",
                        "Set false only for temporary debugging of legacy full-item escrow saves."
                )
                .define("playerTradeRewardsMustBeCurrency", true);
        CURRENCY_EXCHANGE_ENABLED = builder
                .comment(
                        "When true, reward amounts are treated as base-coin value and paid with the largest registered denominations first.",
                        "Default Lightman's Currency denomination values are copper=1, gold=100, diamond=10000."
                )
                .define("currencyExchangeEnabled", true);
        BASE_COIN_ITEM = builder
                .comment("Base coin item id. Generated contract rewards are stored in this unit.")
                .define("baseCoinItem", "lightmanscurrency:coin_copper");
        GOLD_COIN_ITEM = builder
                .comment("Gold coin denomination item id. Set to an unregistered id or disable currencyExchangeEnabled to ignore it.")
                .define("goldCoinItem", "lightmanscurrency:coin_gold");
        GOLD_COIN_VALUE = builder
                .comment("Base-coin value of one gold coin denomination.")
                .defineInRange("goldCoinValue", 100, 1, 1_000_000);
        DIAMOND_COIN_ITEM = builder
                .comment("Diamond coin denomination item id. Set to an unregistered id or disable currencyExchangeEnabled to ignore it.")
                .define("diamondCoinItem", "lightmanscurrency:coin_diamond");
        DIAMOND_COIN_VALUE = builder
                .comment("Base-coin value of one diamond coin denomination.")
                .defineInRange("diamondCoinValue", 10000, 1, 1_000_000);
        builder.pop();

        builder.push("dock");
        DOCK_DELIVERY_RADIUS = builder
                .comment("Sable-aware delivery radius used by Container Dock delivery checks.")
                .defineInRange("deliveryRadius", 18.0D, 1.0D, 64.0D);
        DOCK_CONTAINER_RECOGNITION_RADIUS = builder
                .comment("Radius used by the Container Dock GUI when scanning for nearby sealed container cores.")
                .defineInRange("containerRecognitionRadius", 18.0D, 1.0D, 96.0D);
        DOCK_CONTAINER_SPAWN_DOCK_HALF_X = builder
                .comment("Fallback half-width of the occupied Container Dock footprint along X, measured from the Hut block.")
                .defineInRange("containerSpawnDockHalfX", 8, 0, 32);
        DOCK_CONTAINER_SPAWN_DOCK_HALF_Z = builder
                .comment("Fallback half-depth of the occupied Container Dock footprint along Z, measured from the Hut block.")
                .defineInRange("containerSpawnDockHalfZ", 8, 0, 32);
        DOCK_CONTAINER_SPAWN_DOCK_OCCUPIED_HEIGHT = builder
                .comment("Fallback vertical occupied height of the Container Dock staging structure above the Hut block base.")
                .defineInRange("containerSpawnDockOccupiedHeight", 7, 1, 32);
        DOCK_CONTAINER_SPAWN_HORIZONTAL_GAP = builder
                .comment("Horizontal air gap between occupied Dock blocks and a spawned container footprint.")
                .defineInRange("containerSpawnHorizontalGap", 1, 0, 16);
        DOCK_CONTAINER_SPAWN_BOTTOM_Y_OFFSET = builder
                .comment("Bottom Y offset for side-spawned containers relative to the Dock block position.")
                .defineInRange("containerSpawnBottomYOffset", 1, -16, 32);
        DOCK_CONTAINER_SPAWN_TOP_GAP = builder
                .comment("Vertical air gap above the occupied Dock height before trying top-layer spawn candidates.")
                .defineInRange("containerSpawnTopGap", 1, 0, 16);
        DOCK_CONTAINER_SPAWN_EXTRA_RING_GAP = builder
                .comment("Additional horizontal gap used for the second spawn candidate ring.")
                .defineInRange("containerSpawnExtraRingGap", 2, 0, 32);
        DOCK_CONTAINER_SPAWN_APRON_GRID_ENABLED = builder
                .comment("If true, Container Dock spawning tries this configurable indoor apron grid after the hardcoded nearest-first indoor safety lane.")
                .define("containerSpawnApronGridEnabled", true);
        DOCK_CONTAINER_SPAWN_APRON_START_X = builder
                .comment("First apron-grid core X offset from the Dock block.")
                .defineInRange("containerSpawnApronStartX", 4, -32, 32);
        DOCK_CONTAINER_SPAWN_APRON_START_Z = builder
                .comment("First apron-grid core Z offset from the Dock block.")
                .defineInRange("containerSpawnApronStartZ", 2, -32, 32);
        DOCK_CONTAINER_SPAWN_APRON_COLUMNS = builder
                .comment("Number of apron-grid columns to try along X before falling back to rings.")
                .defineInRange("containerSpawnApronColumns", 3, 1, 8);
        DOCK_CONTAINER_SPAWN_APRON_ROWS = builder
                .comment("Number of apron-grid rows to try along Z before falling back to rings.")
                .defineInRange("containerSpawnApronRows", 2, 1, 8);
        DOCK_CONTAINER_SPAWN_APRON_SPACING_X = builder
                .comment("Requested apron-grid core spacing along X. Runtime clamps this to at least container depth + containerSpawnHorizontalGap.")
                .defineInRange("containerSpawnApronSpacingX", 4, 1, 32);
        DOCK_CONTAINER_SPAWN_APRON_SPACING_Z = builder
                .comment("Requested apron-grid core spacing along Z. Runtime clamps this to at least container width + containerSpawnHorizontalGap.")
                .defineInRange("containerSpawnApronSpacingZ", 8, 1, 32);
        builder.pop();

        builder.push("market");
        AUTO_GENERATE_MARKET_JOBS = builder
                .comment("Automatically top up generated freight jobs on server tick. Disable for command-only debugging.")
                .define("autoGenerateMarketJobs", true);
        MARKET_GENERATION_INTERVAL_TICKS = builder
                .comment("How often the freight market tries to top up generated jobs. 24000 ticks = one Minecraft day.")
                .defineInRange("generationIntervalTicks", 12000, 1200, 240000);
        AUTO_GENERATE_INVENTORY_JOBS = builder
                .comment("Auto-generate inventory parcel jobs for Logistics Office level 1+ colonies.")
                .define("autoGenerateInventoryJobs", true);
        AUTO_GENERATE_CONTAINER_JOBS = builder
                .comment("Auto-generate container jobs for colonies with Dock access.")
                .define("autoGenerateContainerJobs", true);
        MARKET_TEST_INVENTORY_JOB_CAP_PER_COLONY = builder
                .comment(
                        "Target open generated inventory freight jobs per origin colony.",
                        "This used to be a test-only cap; Phase 17.10 keeps the old key name for world compatibility and treats it as a normal balance knob."
                )
                .defineInRange("testInventoryJobCapPerColony", 12, 0, 100);
        MARKET_TEST_CONTAINER_JOB_CAP_PER_COLONY = builder
                .comment(
                        "Target open generated container freight jobs per origin colony.",
                        "This used to be a test-only cap; Phase 17.10 keeps the old key name for world compatibility and treats it as a normal balance knob."
                )
                .defineInRange("testContainerJobCapPerColony", 6, 0, 100);
        MARKET_LOW_DIFFICULTY_INVENTORY_PERCENT = builder
                .comment(
                        "Approximate percent of newly auto-generated inventory jobs that should prefer low difficulty cargo.",
                        "This affects new top-up generation only; existing open contracts are not rebalanced."
                )
                .defineInRange("lowDifficultyInventoryPercent", 50, 0, 100);
        MARKET_LOW_DIFFICULTY_CONTAINER_PERCENT = builder
                .comment(
                        "Approximate percent of newly auto-generated container jobs that should prefer low difficulty Standard cargo.",
                        "Use this to keep easy/normal jobs visible after raising the generated job caps."
                )
                .defineInRange("lowDifficultyContainerPercent", 45, 0, 100);
        MARKET_LOW_DIFFICULTY_INVENTORY_MAX = builder
                .comment("Highest difficulty treated as low difficulty for the inventory low-difficulty mix: easy, normal, hard, or expert.")
                .define("lowDifficultyInventoryMax", "normal");
        MARKET_LOW_DIFFICULTY_CONTAINER_MAX = builder
                .comment("Highest difficulty treated as low difficulty for the container low-difficulty mix: easy, normal, hard, or expert.")
                .define("lowDifficultyContainerMax", "normal");
        MARKET_PURGE_INTERVAL_TICKS = builder
                .comment("How often completed/expired generated freight contracts are purged from active market storage.")
                .defineInRange("purgeIntervalTicks", 72000, 24000, 720000);
        builder.pop();

        builder.push("balance");

        builder.push("buildingLevels");
        int[] openFreightDefaults = {0, 6, 10, 14, 20, 30};
        int[] dockDefaults = {0, 0, 1, 2, 3, 4};
        int[] activeContainerDefaults = {0, 0, 2, 4, 6, 10};
        int[] playerTradeDefaults = {0, 3, 5, 8, 12, 20};
        String[] maxContainerDefaults = {"none", "none", "standard", "standard", "large", "heavy"};
        boolean[] inventoryDefaults = {false, true, true, true, true, true};
        boolean[] containerDefaults = {false, false, true, true, true, true};
        for (int level = 0; level <= 5; level++) {
            builder.push("level" + level);
            LEVEL_MAX_OPEN_FREIGHT_JOBS[level] = builder
                    .comment("Maximum generated inventory freight contracts this Logistics Office level can keep open.")
                    .defineInRange("maxOpenFreightJobs", openFreightDefaults[level], 0, 200);
            LEVEL_MAX_CONTAINER_DOCKS[level] = builder
                    .comment("Maximum Container Docks this Logistics Office level can register.")
                    .defineInRange("maxContainerDocks", dockDefaults[level], 0, 32);
            LEVEL_MAX_ACTIVE_CONTAINER_JOBS[level] = builder
                    .comment("Maximum active container jobs that can be spawned from this origin colony.")
                    .defineInRange("maxActiveContainerJobs", activeContainerDefaults[level], 0, 200);
            LEVEL_MAX_PLAYER_TRADE_CONTRACTS[level] = builder
                    .comment("Maximum open player Trade Terminal contracts intended for this Logistics Office level.")
                    .defineInRange("maxPlayerTradeContracts", playerTradeDefaults[level], 0, 200);
            LEVEL_MAX_CONTAINER_STANDARD[level] = builder
                    .comment("Highest container standard allowed at this Logistics Office level: none, standard, large, or heavy.")
                    .define("maxContainerStandard", maxContainerDefaults[level]);
            LEVEL_INVENTORY_FREIGHT_ENABLED[level] = builder
                    .comment("Whether generated inventory parcel jobs are enabled at this Logistics Office level.")
                    .define("inventoryFreightEnabled", inventoryDefaults[level]);
            LEVEL_CONTAINER_FREIGHT_ENABLED[level] = builder
                    .comment("Whether generated container jobs are enabled at this Logistics Office level.")
                    .define("containerFreightEnabled", containerDefaults[level]);
            builder.pop();
        }
        builder.pop();

        builder.push("carrierRequirements");
        INVENTORY_REQUIRED_CARRIER_LEVEL = builder
                .comment("Carrier level required to accept generated inventory parcel jobs.")
                .defineInRange("inventoryRequiredCarrierLevel", 1, 1, 5);
        STANDARD_REQUIRED_CARRIER_LEVEL = builder
                .comment("Carrier level required to accept Standard container jobs.")
                .defineInRange("standardRequiredCarrierLevel", 2, 1, 5);
        LARGE_REQUIRED_CARRIER_LEVEL = builder
                .comment("Carrier level required to accept Large container jobs.")
                .defineInRange("largeRequiredCarrierLevel", 4, 1, 5);
        HEAVY_REQUIRED_CARRIER_LEVEL = builder
                .comment("Carrier level required to accept Heavy container jobs.")
                .defineInRange("heavyRequiredCarrierLevel", 5, 1, 5);
        builder.pop();

        builder.push("rewards");
        builder.push("inventory");
        INVENTORY_REWARD_BASE = builder.defineInRange("base", 20, 0, 1_000_000);
        INVENTORY_REWARD_MIN_DISTANCE_BONUS = builder.defineInRange("minDistanceBonus", 1, 0, 1_000_000);
        INVENTORY_REWARD_DISTANCE_DIVISOR = builder
                .comment("Higher values reduce distance reward. Distance bonus is max(minDistanceBonus, routeDistance / divisor).")
                .defineInRange("distanceDivisor", 20, 1, 1_000_000);
        INVENTORY_REWARD_CARGO_VALUE_MULTIPLIER = builder.defineInRange("cargoValueMultiplier", 1.0D, 0.0D, 1000.0D);
        INVENTORY_REWARD_CARGO_WEIGHT_MULTIPLIER = builder.defineInRange("cargoWeightMultiplier", 2.0D, 0.0D, 1000.0D);
        INVENTORY_REWARD_FRAGILITY_MULTIPLIER = builder.defineInRange("fragilityMultiplier", 8.0D, 0.0D, 1000.0D);
        INVENTORY_REWARD_GLOBAL_MULTIPLIER = builder.defineInRange("globalMultiplier", 1.0D, 0.0D, 1000.0D);
        builder.pop();

        builder.push("container");
        CONTAINER_REWARD_BASE = builder.defineInRange("base", 100, 0, 10_000_000);
        CONTAINER_REWARD_MIN_DISTANCE_BONUS = builder.defineInRange("minDistanceBonus", 10, 0, 10_000_000);
        CONTAINER_REWARD_DISTANCE_DIVISOR = builder
                .comment("Higher values reduce distance reward. Distance bonus is max(minDistanceBonus, routeDistance / divisor).")
                .defineInRange("distanceDivisor", 8, 1, 1_000_000);
        CONTAINER_REWARD_CARGO_VALUE_MULTIPLIER = builder.defineInRange("cargoValueMultiplier", 1.0D, 0.0D, 1000.0D);
        CONTAINER_REWARD_CARGO_WEIGHT_MULTIPLIER = builder.defineInRange("cargoWeightMultiplier", 2.0D, 0.0D, 1000.0D);
        CONTAINER_REWARD_SIZE_VOLUME_MULTIPLIER = builder.defineInRange("sizeVolumeMultiplier", 2.0D, 0.0D, 1000.0D);
        CONTAINER_REWARD_SIZE_BASE_WEIGHT_MULTIPLIER = builder.defineInRange("sizeBaseWeightMultiplier", 0.5D, 0.0D, 1000.0D);
        CONTAINER_REWARD_EMPTY_WEIGHT_BONUS = builder.defineInRange("emptyWeightClassBonus", 0, 0, 10_000_000);
        CONTAINER_REWARD_LIGHT_WEIGHT_BONUS = builder.defineInRange("lightWeightClassBonus", 25, 0, 10_000_000);
        CONTAINER_REWARD_MEDIUM_WEIGHT_BONUS = builder.defineInRange("mediumWeightClassBonus", 75, 0, 10_000_000);
        CONTAINER_REWARD_HEAVY_WEIGHT_BONUS = builder.defineInRange("heavyWeightClassBonus", 150, 0, 10_000_000);
        CONTAINER_REWARD_SUPER_HEAVY_WEIGHT_BONUS = builder.defineInRange("superHeavyWeightClassBonus", 300, 0, 10_000_000);
        CONTAINER_REWARD_EXTREME_WEIGHT_BONUS = builder.defineInRange("extremeWeightClassBonus", 600, 0, 10_000_000);
        STANDARD_REWARD_MULTIPLIER = builder.defineInRange("standardMultiplier", 1.0D, 0.0D, 1000.0D);
        LARGE_REWARD_MULTIPLIER = builder.defineInRange("largeMultiplier", 3.0D, 0.0D, 1000.0D);
        HEAVY_REWARD_MULTIPLIER = builder.defineInRange("heavyMultiplier", 5.0D, 0.0D, 1000.0D);
        CONTAINER_REWARD_GLOBAL_MULTIPLIER = builder.defineInRange("globalMultiplier", 1.0D, 0.0D, 1000.0D);
        builder.pop();
        builder.pop();

        builder.push("deadlines");
        PICKUP_WINDOW_TICKS = builder
                .comment(
                        "Pickup window for generated jobs. 24000 ticks = one Minecraft day.",
                        "Default is 0, meaning no pickup deadline. Set a positive tick value to enable expiration."
                )
                .defineInRange("pickupWindowTicks", 0, 0, 2_400_000);
        INVENTORY_DELIVERY_WINDOW_TICKS = builder
                .comment("Inventory delivery window. Default 0 means no delivery deadline.")
                .defineInRange("inventoryDeliveryWindowTicks", 0, 0, 2_400_000);
        STANDARD_DELIVERY_WINDOW_TICKS = builder
                .comment("Standard container delivery window. Default 0 means no delivery deadline.")
                .defineInRange("standardContainerDeliveryWindowTicks", 0, 0, 2_400_000);
        LARGE_DELIVERY_WINDOW_TICKS = builder
                .comment("Large container delivery window. Default 0 means no delivery deadline.")
                .defineInRange("largeContainerDeliveryWindowTicks", 0, 0, 2_400_000);
        HEAVY_DELIVERY_WINDOW_TICKS = builder
                .comment("Heavy container delivery window. Default 0 means no delivery deadline.")
                .defineInRange("heavyContainerDeliveryWindowTicks", 0, 0, 2_400_000);
        GENERATED_JOBS_ALLOW_LATE_DELIVERY = builder
                .comment("If true, late generated deliveries can still complete with lateDeliveryRewardPercent payout. Ignored when the delivery window is 0.")
                .define("generatedJobsAllowLateDelivery", true);
        LATE_DELIVERY_REWARD_PERCENT = builder
                .comment("Percentage of the original reward paid for late but allowed deliveries.")
                .defineInRange("lateDeliveryRewardPercent", 50, 0, 100);
        GENERATED_JOBS_ALLOW_CARRIER_CANCEL = builder
                .comment("Allow the assigned carrier to cancel accepted or picked-up generated freight from the Logistics Office.")
                .define("generatedJobsAllowCarrierCancel", true);
        GENERATED_JOBS_CANCEL_COUNTS_AS_FAILED = builder
                .comment("If true, carrier-cancelled generated freight records a failed job and applies the failed-job reputation penalty.")
                .define("generatedJobsCancelCountsAsFailed", false);
        GENERATED_JOBS_ALLOW_CANCEL_AFTER_CONTAINER_SPAWN = builder
                .comment(
                        "Allow cancelling a container contract after one or more physical containers have spawned.",
                        "Nearby spawned containers are removed when found, but containers already moved far away become undeliverable because the contract is cancelled."
                )
                .define("generatedJobsAllowCancelAfterContainerSpawn", true);
        builder.pop();

        builder.push("containerGeneration");
        STANDARD_GENERATION_WEIGHT = builder
                .comment("Relative generation weight for Standard container jobs. Set 0 to disable automatic generation.")
                .defineInRange("standardGenerationWeight", 6, 0, 1000);
        LARGE_GENERATION_WEIGHT = builder
                .comment("Relative generation weight for Large container jobs. Set 0 to disable automatic generation.")
                .defineInRange("largeGenerationWeight", 3, 0, 1000);
        HEAVY_GENERATION_WEIGHT = builder
                .comment("Relative generation weight for Heavy container jobs. Set 0 to disable automatic generation.")
                .defineInRange("heavyGenerationWeight", 1, 0, 1000);
        STANDARD_DEFAULT_CONTAINER_COUNT = builder.defineInRange("standardDefaultContainerCount", 2, 1, 16);
        LARGE_DEFAULT_CONTAINER_COUNT = builder.defineInRange("largeDefaultContainerCount", 2, 1, 16);
        HEAVY_DEFAULT_CONTAINER_COUNT = builder.defineInRange("heavyDefaultContainerCount", 3, 1, 16);
        LARGE_FRAGILE_CONTAINER_COUNT = builder
                .comment("Container count for fragile Large contracts. Set equal to largeDefaultContainerCount to disable this special case.")
                .defineInRange("largeFragileContainerCount", 3, 1, 16);
        builder.pop();

        builder.push("carrierProfile");
        REPUTATION_GAIN_REWARD_DIVISOR = builder
                .comment("Completed job reputation gain is max(minReputationGainPerJob, reward / this divisor).")
                .defineInRange("reputationGainRewardDivisor", 10, 1, 1_000_000);
        MIN_REPUTATION_GAIN_PER_JOB = builder.defineInRange("minReputationGainPerJob", 1, 0, 1_000_000);
        FAILED_JOB_REPUTATION_PENALTY = builder.defineInRange("failedJobReputationPenalty", 10, 0, 1_000_000);
        CARRIER_LEVEL_2_COMPLETED_JOBS = builder.defineInRange("level2CompletedJobs", 5, 0, 1_000_000);
        CARRIER_LEVEL_2_REPUTATION = builder.defineInRange("level2Reputation", 25, 0, 1_000_000);
        CARRIER_LEVEL_3_COMPLETED_JOBS = builder.defineInRange("level3CompletedJobs", 15, 0, 1_000_000);
        CARRIER_LEVEL_3_REPUTATION = builder.defineInRange("level3Reputation", 100, 0, 1_000_000);
        CARRIER_LEVEL_4_COMPLETED_JOBS = builder.defineInRange("level4CompletedJobs", 35, 0, 1_000_000);
        CARRIER_LEVEL_4_REPUTATION = builder.defineInRange("level4Reputation", 250, 0, 1_000_000);
        CARRIER_LEVEL_5_COMPLETED_JOBS = builder.defineInRange("level5CompletedJobs", 75, 0, 1_000_000);
        CARRIER_LEVEL_5_REPUTATION = builder.defineInRange("level5Reputation", 600, 0, 1_000_000);
        builder.pop();

        builder.pop(); // balance

        builder.push("testing");
        ALLOW_SELF_DELIVERY_FOR_TESTING = builder
                .comment(
                        "Testing helper for single-player Trade Terminal checks.",
                        "Normal multiplayer default is false: the creator cannot fulfill their own trade."
                )
                .define("allowSelfDeliveryForTesting", false);
        ALLOW_LOOPBACK_FREIGHT_FOR_TESTING = builder
                .comment(
                        "Testing helper for single-colony inventory freight checks.",
                        "Normal multiplayer default is false: generated freight requires a different destination colony."
                )
                .define("allowLoopbackFreightForTesting", false);
        ALLOW_LOOPBACK_CONTAINER_FREIGHT_FOR_TESTING = builder
                .comment(
                        "Testing helper for same-colony Container Dock checks and /colonylogistics market localtest.",
                        "Normal multiplayer default is false: generated container freight requires a different destination colony."
                )
                .define("allowLoopbackContainerFreightForTesting", false);
        DEBUG_MULTIPLAYER_NETWORK_LOGGING = builder
                .comment(
                        "Multiplayer beta logging for Colony Logistics C2S packets and server-side validation rejects.",
                        "Logs use the [CL-MP][network] prefix in the dedicated server log. Disable after beta if too noisy."
                )
                .define("debugMultiplayerNetworkLogging", true);
        DEBUG_CONTRACT_LIFECYCLE_LOGGING = builder
                .comment(
                        "Multiplayer beta logging for generated freight, accepted/cancelled/delivered contracts, containers, and Trade Terminal escrow trades.",
                        "Logs use [CL-MP][contract], [CL-MP][container], and [CL-MP][trade] prefixes."
                )
                .define("debugContractLifecycleLogging", true);
        builder.pop();

        SPEC = builder.build();
    }

    public static ResourceLocation defaultCurrencyItemId() { return DEFAULT_LIGHTMANS_BASE_COIN; }
    public static ResourceLocation legacyTradePostCurrencyItemId() { return normalizeLegacyCurrencyId(parseLocation(LEGACY_TRADE_POST_CURRENCY_ITEM.get(), DEFAULT_LIGHTMANS_BASE_COIN)); }
    public static ResourceLocation fallbackCurrencyItemId() { return normalizeLegacyFallbackCurrencyId(parseLocation(FALLBACK_CURRENCY_ITEM.get(), DEFAULT_LIGHTMANS_BASE_COIN)); }
    public static boolean useFallbackCurrencyWhenMissing() { return USE_FALLBACK_CURRENCY_WHEN_MISSING.get(); }
    public static boolean playerTradeRewardsMustBeCurrency() { return PLAYER_TRADE_REWARDS_MUST_BE_CURRENCY.get(); }
    public static boolean currencyExchangeEnabled() { return CURRENCY_EXCHANGE_ENABLED.get(); }
    public static ResourceLocation currencyBaseCoinItemId() {
        String raw = BASE_COIN_ITEM.get();
        if (raw == null || raw.isBlank()) {
            return legacyTradePostCurrencyItemId();
        }
        return normalizeLegacyCurrencyId(parseLocation(raw, legacyTradePostCurrencyItemId()));
    }
    public static ResourceLocation currencyGoldCoinItemId() { return normalizeLegacyCurrencyId(parseLocation(GOLD_COIN_ITEM.get(), DEFAULT_LIGHTMANS_GOLD_COIN)); }
    public static int currencyGoldCoinValue() {
        ResourceLocation rawItem = parseLocation(GOLD_COIN_ITEM.get(), DEFAULT_LIGHTMANS_GOLD_COIN);
        int value = GOLD_COIN_VALUE.get();
        return LEGACY_TRADE_POST_GOLD_COIN.equals(rawItem) && value == 8 ? 100 : value;
    }
    public static ResourceLocation currencyDiamondCoinItemId() { return normalizeLegacyCurrencyId(parseLocation(DIAMOND_COIN_ITEM.get(), DEFAULT_LIGHTMANS_DIAMOND_COIN)); }
    public static int currencyDiamondCoinValue() {
        ResourceLocation rawItem = parseLocation(DIAMOND_COIN_ITEM.get(), DEFAULT_LIGHTMANS_DIAMOND_COIN);
        int value = DIAMOND_COIN_VALUE.get();
        return LEGACY_TRADE_POST_DIAMOND_COIN.equals(rawItem) && value == 64 ? 10000 : value;
    }

    public static double dockDeliveryRadius() { return DOCK_DELIVERY_RADIUS.get(); }
    public static double dockContainerRecognitionRadius() { return DOCK_CONTAINER_RECOGNITION_RADIUS.get(); }
    public static int dockContainerSpawnDockHalfX() { return DOCK_CONTAINER_SPAWN_DOCK_HALF_X.get(); }
    public static int dockContainerSpawnDockHalfZ() { return DOCK_CONTAINER_SPAWN_DOCK_HALF_Z.get(); }
    public static int dockContainerSpawnDockOccupiedHeight() { return DOCK_CONTAINER_SPAWN_DOCK_OCCUPIED_HEIGHT.get(); }
    public static int dockContainerSpawnHorizontalGap() { return DOCK_CONTAINER_SPAWN_HORIZONTAL_GAP.get(); }
    public static int dockContainerSpawnBottomYOffset() { return DOCK_CONTAINER_SPAWN_BOTTOM_Y_OFFSET.get(); }
    public static int dockContainerSpawnTopGap() { return DOCK_CONTAINER_SPAWN_TOP_GAP.get(); }
    public static int dockContainerSpawnExtraRingGap() { return DOCK_CONTAINER_SPAWN_EXTRA_RING_GAP.get(); }
    public static boolean dockContainerSpawnApronGridEnabled() { return DOCK_CONTAINER_SPAWN_APRON_GRID_ENABLED.get(); }
    public static int dockContainerSpawnApronStartX() { return DOCK_CONTAINER_SPAWN_APRON_START_X.get(); }
    public static int dockContainerSpawnApronStartZ() { return DOCK_CONTAINER_SPAWN_APRON_START_Z.get(); }
    public static int dockContainerSpawnApronColumns() { return DOCK_CONTAINER_SPAWN_APRON_COLUMNS.get(); }
    public static int dockContainerSpawnApronRows() { return DOCK_CONTAINER_SPAWN_APRON_ROWS.get(); }
    public static int dockContainerSpawnApronSpacingX() { return DOCK_CONTAINER_SPAWN_APRON_SPACING_X.get(); }
    public static int dockContainerSpawnApronSpacingZ() { return DOCK_CONTAINER_SPAWN_APRON_SPACING_Z.get(); }

    public static boolean autoGenerateMarketJobs() { return overrideAutoGenerateMarketJobs != null ? overrideAutoGenerateMarketJobs : AUTO_GENERATE_MARKET_JOBS.get(); }
    public static int marketGenerationIntervalTicks() { return MARKET_GENERATION_INTERVAL_TICKS.get(); }
    public static boolean autoGenerateInventoryJobs() { return overrideAutoGenerateInventoryJobs != null ? overrideAutoGenerateInventoryJobs : AUTO_GENERATE_INVENTORY_JOBS.get(); }
    public static boolean autoGenerateContainerJobs() { return overrideAutoGenerateContainerJobs != null ? overrideAutoGenerateContainerJobs : AUTO_GENERATE_CONTAINER_JOBS.get(); }
    public static int marketTestInventoryJobCapPerColony() { return overrideMarketTestInventoryJobCapPerColony != null ? overrideMarketTestInventoryJobCapPerColony : MARKET_TEST_INVENTORY_JOB_CAP_PER_COLONY.get(); }
    public static int marketTestContainerJobCapPerColony() { return overrideMarketTestContainerJobCapPerColony != null ? overrideMarketTestContainerJobCapPerColony : MARKET_TEST_CONTAINER_JOB_CAP_PER_COLONY.get(); }
    public static int marketLowDifficultyInventoryPercent() { return MARKET_LOW_DIFFICULTY_INVENTORY_PERCENT.get(); }
    public static int marketLowDifficultyContainerPercent() { return MARKET_LOW_DIFFICULTY_CONTAINER_PERCENT.get(); }
    public static FreightDifficulty marketLowDifficultyInventoryMax() { return parseFreightDifficulty(MARKET_LOW_DIFFICULTY_INVENTORY_MAX.get(), FreightDifficulty.NORMAL); }
    public static FreightDifficulty marketLowDifficultyContainerMax() { return parseFreightDifficulty(MARKET_LOW_DIFFICULTY_CONTAINER_MAX.get(), FreightDifficulty.NORMAL); }
    public static int marketPurgeIntervalTicks() { return MARKET_PURGE_INTERVAL_TICKS.get(); }

    public static int levelMaxOpenFreightJobs(int level) { return LEVEL_MAX_OPEN_FREIGHT_JOBS[levelIndex(level)].get(); }
    public static int levelMaxContainerDocks(int level) { return LEVEL_MAX_CONTAINER_DOCKS[levelIndex(level)].get(); }
    public static int levelMaxActiveContainerJobs(int level) { return LEVEL_MAX_ACTIVE_CONTAINER_JOBS[levelIndex(level)].get(); }
    public static int levelMaxPlayerTradeContracts(int level) { return LEVEL_MAX_PLAYER_TRADE_CONTRACTS[levelIndex(level)].get(); }
    public static ContainerSize levelMaxContainerSize(int level) { return parseContainerStandard(LEVEL_MAX_CONTAINER_STANDARD[levelIndex(level)].get()).map(ContainerStandard::physicalSize).orElse(ContainerSize.NONE); }
    public static boolean levelInventoryFreightEnabled(int level) { return LEVEL_INVENTORY_FREIGHT_ENABLED[levelIndex(level)].get(); }
    public static boolean levelContainerFreightEnabled(int level) { return LEVEL_CONTAINER_FREIGHT_ENABLED[levelIndex(level)].get(); }

    public static int inventoryRequiredCarrierLevel() { return INVENTORY_REQUIRED_CARRIER_LEVEL.get(); }
    public static int requiredCarrierLevel(ContainerSize size) {
        return switch (ContainerStandard.fromSize(size)) {
            case STANDARD -> STANDARD_REQUIRED_CARRIER_LEVEL.get();
            case LARGE -> LARGE_REQUIRED_CARRIER_LEVEL.get();
            case HEAVY -> HEAVY_REQUIRED_CARRIER_LEVEL.get();
        };
    }

    public static int inventoryRewardBase() { return INVENTORY_REWARD_BASE.get(); }
    public static int inventoryRewardMinDistanceBonus() { return INVENTORY_REWARD_MIN_DISTANCE_BONUS.get(); }
    public static int inventoryRewardDistanceDivisor() { return INVENTORY_REWARD_DISTANCE_DIVISOR.get(); }
    public static double inventoryRewardCargoValueMultiplier() { return INVENTORY_REWARD_CARGO_VALUE_MULTIPLIER.get(); }
    public static double inventoryRewardCargoWeightMultiplier() { return INVENTORY_REWARD_CARGO_WEIGHT_MULTIPLIER.get(); }
    public static double inventoryRewardFragilityMultiplier() { return INVENTORY_REWARD_FRAGILITY_MULTIPLIER.get(); }
    public static double inventoryRewardGlobalMultiplier() { return INVENTORY_REWARD_GLOBAL_MULTIPLIER.get(); }

    public static int containerRewardBase() { return CONTAINER_REWARD_BASE.get(); }
    public static int containerRewardMinDistanceBonus() { return CONTAINER_REWARD_MIN_DISTANCE_BONUS.get(); }
    public static int containerRewardDistanceDivisor() { return CONTAINER_REWARD_DISTANCE_DIVISOR.get(); }
    public static double containerRewardCargoValueMultiplier() { return CONTAINER_REWARD_CARGO_VALUE_MULTIPLIER.get(); }
    public static double containerRewardCargoWeightMultiplier() { return CONTAINER_REWARD_CARGO_WEIGHT_MULTIPLIER.get(); }
    public static double containerRewardSizeVolumeMultiplier() { return CONTAINER_REWARD_SIZE_VOLUME_MULTIPLIER.get(); }
    public static double containerRewardSizeBaseWeightMultiplier() { return CONTAINER_REWARD_SIZE_BASE_WEIGHT_MULTIPLIER.get(); }
    public static double containerRewardGlobalMultiplier() { return CONTAINER_REWARD_GLOBAL_MULTIPLIER.get(); }
    public static int containerWeightClassBonus(String weightClassName) {
        return switch (weightClassName) {
            case "EMPTY" -> CONTAINER_REWARD_EMPTY_WEIGHT_BONUS.get();
            case "LIGHT" -> CONTAINER_REWARD_LIGHT_WEIGHT_BONUS.get();
            case "MEDIUM" -> CONTAINER_REWARD_MEDIUM_WEIGHT_BONUS.get();
            case "HEAVY" -> CONTAINER_REWARD_HEAVY_WEIGHT_BONUS.get();
            case "SUPER_HEAVY" -> CONTAINER_REWARD_SUPER_HEAVY_WEIGHT_BONUS.get();
            case "EXTREME" -> CONTAINER_REWARD_EXTREME_WEIGHT_BONUS.get();
            default -> 0;
        };
    }
    public static double containerStandardRewardMultiplier(ContainerSize size) {
        return switch (ContainerStandard.fromSize(size)) {
            case STANDARD -> STANDARD_REWARD_MULTIPLIER.get();
            case LARGE -> LARGE_REWARD_MULTIPLIER.get();
            case HEAVY -> HEAVY_REWARD_MULTIPLIER.get();
        };
    }

    public static long pickupWindowTicks() { return PICKUP_WINDOW_TICKS.get(); }
    public static long inventoryDeliveryWindowTicks() { return INVENTORY_DELIVERY_WINDOW_TICKS.get(); }
    public static long deliveryWindowTicks(ContainerSize size) {
        return switch (ContainerStandard.fromSize(size)) {
            case STANDARD -> STANDARD_DELIVERY_WINDOW_TICKS.get();
            case LARGE -> LARGE_DELIVERY_WINDOW_TICKS.get();
            case HEAVY -> HEAVY_DELIVERY_WINDOW_TICKS.get();
        };
    }
    public static boolean generatedJobsAllowLateDelivery() { return GENERATED_JOBS_ALLOW_LATE_DELIVERY.get(); }
    public static int lateDeliveryRewardPercent() { return LATE_DELIVERY_REWARD_PERCENT.get(); }
    public static int lateAdjustedReward(int originalAmount) { return Math.max(1, (int) Math.round(Math.max(0, originalAmount) * lateDeliveryRewardPercent() / 100.0D)); }
    public static boolean generatedJobsAllowCarrierCancel() { return GENERATED_JOBS_ALLOW_CARRIER_CANCEL.get(); }
    public static boolean generatedJobsCancelCountsAsFailed() { return GENERATED_JOBS_CANCEL_COUNTS_AS_FAILED.get(); }
    public static boolean generatedJobsAllowCancelAfterContainerSpawn() { return GENERATED_JOBS_ALLOW_CANCEL_AFTER_CONTAINER_SPAWN.get(); }

    public static int containerGenerationWeight(ContainerStandard standard) {
        return switch (standard) {
            case STANDARD -> STANDARD_GENERATION_WEIGHT.get();
            case LARGE -> LARGE_GENERATION_WEIGHT.get();
            case HEAVY -> HEAVY_GENERATION_WEIGHT.get();
        };
    }
    public static int defaultContainerCount(ContainerSize size) {
        return switch (ContainerStandard.fromSize(size)) {
            case STANDARD -> STANDARD_DEFAULT_CONTAINER_COUNT.get();
            case LARGE -> LARGE_DEFAULT_CONTAINER_COUNT.get();
            case HEAVY -> HEAVY_DEFAULT_CONTAINER_COUNT.get();
        };
    }
    public static int largeFragileContainerCount() { return LARGE_FRAGILE_CONTAINER_COUNT.get(); }

    public static int reputationGainForReward(int rewardAmount) { return Math.max(MIN_REPUTATION_GAIN_PER_JOB.get(), Math.max(0, rewardAmount) / REPUTATION_GAIN_REWARD_DIVISOR.get()); }
    public static int failedJobReputationPenalty() { return FAILED_JOB_REPUTATION_PENALTY.get(); }
    public static int carrierLevelFor(int completedJobs, int reputation) {
        int next = 1;
        if (completedJobs >= CARRIER_LEVEL_2_COMPLETED_JOBS.get() && reputation >= CARRIER_LEVEL_2_REPUTATION.get()) next = 2;
        if (completedJobs >= CARRIER_LEVEL_3_COMPLETED_JOBS.get() && reputation >= CARRIER_LEVEL_3_REPUTATION.get()) next = 3;
        if (completedJobs >= CARRIER_LEVEL_4_COMPLETED_JOBS.get() && reputation >= CARRIER_LEVEL_4_REPUTATION.get()) next = 4;
        if (completedJobs >= CARRIER_LEVEL_5_COMPLETED_JOBS.get() && reputation >= CARRIER_LEVEL_5_REPUTATION.get()) next = 5;
        return next;
    }
    public static int carrierCompletedJobsForLevel(int level) {
        return switch (Math.max(1, Math.min(5, level))) {
            case 5 -> CARRIER_LEVEL_5_COMPLETED_JOBS.get();
            case 4 -> CARRIER_LEVEL_4_COMPLETED_JOBS.get();
            case 3 -> CARRIER_LEVEL_3_COMPLETED_JOBS.get();
            case 2 -> CARRIER_LEVEL_2_COMPLETED_JOBS.get();
            default -> 0;
        };
    }
    public static int carrierReputationForLevel(int level) {
        return switch (Math.max(1, Math.min(5, level))) {
            case 5 -> CARRIER_LEVEL_5_REPUTATION.get();
            case 4 -> CARRIER_LEVEL_4_REPUTATION.get();
            case 3 -> CARRIER_LEVEL_3_REPUTATION.get();
            case 2 -> CARRIER_LEVEL_2_REPUTATION.get();
            default -> 0;
        };
    }

    public static boolean allowSelfDeliveryForTesting() { return overrideAllowSelfDeliveryForTesting != null ? overrideAllowSelfDeliveryForTesting : ALLOW_SELF_DELIVERY_FOR_TESTING.get(); }
    public static boolean allowLoopbackFreightForTesting() { return overrideAllowLoopbackFreightForTesting != null ? overrideAllowLoopbackFreightForTesting : ALLOW_LOOPBACK_FREIGHT_FOR_TESTING.get(); }
    public static boolean allowLoopbackContainerFreightForTesting() { return overrideAllowLoopbackContainerFreightForTesting != null ? overrideAllowLoopbackContainerFreightForTesting : ALLOW_LOOPBACK_CONTAINER_FREIGHT_FOR_TESTING.get(); }
    public static boolean debugMultiplayerNetworkLogging() { return DEBUG_MULTIPLAYER_NETWORK_LOGGING.get(); }
    public static boolean debugContractLifecycleLogging() { return DEBUG_CONTRACT_LIFECYCLE_LOGGING.get(); }

    public static void setAutoGenerateMarketJobsOverride(boolean value) { overrideAutoGenerateMarketJobs = value; }
    public static void setAutoGenerateInventoryJobsOverride(boolean value) { overrideAutoGenerateInventoryJobs = value; }
    public static void setAutoGenerateContainerJobsOverride(boolean value) { overrideAutoGenerateContainerJobs = value; }
    public static void setMarketTestInventoryJobCapOverride(int value) { overrideMarketTestInventoryJobCapPerColony = Math.max(0, value); }
    public static void setMarketTestContainerJobCapOverride(int value) { overrideMarketTestContainerJobCapPerColony = Math.max(0, value); }
    public static void setAllowSelfDeliveryForTestingOverride(boolean value) { overrideAllowSelfDeliveryForTesting = value; }
    public static void setAllowLoopbackFreightForTestingOverride(boolean value) { overrideAllowLoopbackFreightForTesting = value; }
    public static void setAllowLoopbackContainerFreightForTestingOverride(boolean value) { overrideAllowLoopbackContainerFreightForTesting = value; }

    public static void clearRuntimeTestOverrides() {
        overrideAutoGenerateMarketJobs = null;
        overrideAutoGenerateInventoryJobs = null;
        overrideAutoGenerateContainerJobs = null;
        overrideMarketTestInventoryJobCapPerColony = null;
        overrideMarketTestContainerJobCapPerColony = null;
        overrideAllowSelfDeliveryForTesting = null;
        overrideAllowLoopbackFreightForTesting = null;
        overrideAllowLoopbackContainerFreightForTesting = null;
    }

    public static boolean hasRuntimeTestOverrides() {
        return overrideAutoGenerateMarketJobs != null
                || overrideAutoGenerateInventoryJobs != null
                || overrideAutoGenerateContainerJobs != null
                || overrideMarketTestInventoryJobCapPerColony != null
                || overrideMarketTestContainerJobCapPerColony != null
                || overrideAllowSelfDeliveryForTesting != null
                || overrideAllowLoopbackFreightForTesting != null
                || overrideAllowLoopbackContainerFreightForTesting != null;
    }

    public static String runtimeTestOverrideLabel() { return hasRuntimeTestOverrides() ? "runtime overrides active" : "TOML defaults"; }

    public static boolean isLegacyDefaultRewardItem(ResourceLocation itemId) { return legacyCurrencyBaseValue(itemId) > 0; }

    public static int legacyCurrencyBaseValue(ResourceLocation itemId) {
        if (LEGACY_TRADE_POST_GOLD_COIN.equals(itemId)) return 8;
        if (LEGACY_TRADE_POST_DIAMOND_COIN.equals(itemId)) return 64;
        if (LEGACY_PLACEHOLDER_CURRENCY.equals(itemId) || LEGACY_EMERALD_FALLBACK.equals(itemId) || LEGACY_TRADE_POST_CURRENCY.equals(itemId)) return 1;
        return 0;
    }

    private static int levelIndex(int level) { return Math.max(0, Math.min(5, level)); }
    private static java.util.Optional<ContainerStandard> parseContainerStandard(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("none")) {
            return java.util.Optional.empty();
        }
        return ContainerStandard.parse(value);
    }

    private static FreightDifficulty parseFreightDifficulty(String value, FreightDifficulty fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return FreightDifficulty.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static ResourceLocation normalizeLegacyCurrencyId(ResourceLocation itemId) {
        if (LEGACY_TRADE_POST_GOLD_COIN.equals(itemId)) return DEFAULT_LIGHTMANS_GOLD_COIN;
        if (LEGACY_TRADE_POST_DIAMOND_COIN.equals(itemId)) return DEFAULT_LIGHTMANS_DIAMOND_COIN;
        return isLegacyDefaultRewardItem(itemId) ? DEFAULT_LIGHTMANS_BASE_COIN : itemId;
    }

    private static ResourceLocation normalizeLegacyFallbackCurrencyId(ResourceLocation itemId) {
        return LEGACY_EMERALD_FALLBACK.equals(itemId) ? DEFAULT_LIGHTMANS_BASE_COIN : normalizeLegacyCurrencyId(itemId);
    }

    private static ResourceLocation parseLocation(String raw, ResourceLocation fallback) {
        try {
            return ResourceLocation.parse(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private ColonyLogisticsConfig() {}
}
