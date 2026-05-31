package jp.colonylogistics.service;

import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.colony.ColonyLogisticsLimits;
import jp.colonylogistics.colony.ColonyLogisticsState;
import jp.colonylogistics.container.ContainerRequirement;
import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.ContainerStandard;
import jp.colonylogistics.container.ContainerWeightClass;
import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.ContractType;
import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.contract.RewardSpec;
import jp.colonylogistics.currency.CurrencyService;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.freight.FreightDifficulty;
import jp.colonylogistics.freight.FreightJobSpec;
import jp.colonylogistics.freight.VirtualCargo;
import jp.colonylogistics.physics.WeightClassifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generates and queries the server-side freight market.
 *
 * <p>Inventory freight remains the level-1 onboarding loop. Container freight is now
 * spec-aware: player-facing Standard, Large, and Heavy jobs can be generated when
 * both the origin and destination colonies have a high enough Logistics Office level
 * and at least one active Dock. The physical ContainerSize enum remains in NBT,
 * but MEDIUM is no longer generated automatically.</p>
 */
public final class FreightMarketService {
    private static final List<VirtualCargo> INVENTORY_CARGO_CATALOG = List.of(
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "documents"), 1, 1, 1, 8, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "food_box"), 1, 2, 2, 14, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "parts_box"), 1, 2, 3, 20, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "valuable_case"), 1, 1, 1, 45, 2)
    );

    private static final List<VirtualCargo> SMALL_CONTAINER_CARGO_CATALOG = List.of(
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "timber_container"), 12, 2, 8, 18, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "stone_container"), 16, 2, 14, 16, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "food_crates_container"), 10, 2, 6, 24, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "machine_parts_container"), 8, 3, 10, 38, 1)
    );

    private static final List<VirtualCargo> MEDIUM_CONTAINER_CARGO_CATALOG = List.of(
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "processed_lumber_container"), 18, 2, 9, 24, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "tools_and_parts_container"), 12, 3, 12, 44, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "preserved_food_container"), 16, 2, 7, 30, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "construction_supply_container"), 20, 2, 11, 28, 0)
    );

    private static final List<VirtualCargo> LARGE_CONTAINER_CARGO_CATALOG = List.of(
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "bulk_stone_container"), 48, 2, 13, 22, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "industrial_components_container"), 32, 3, 14, 58, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "regional_food_stockpile"), 44, 2, 8, 36, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "luxury_trade_goods_container"), 24, 2, 8, 90, 2)
    );

    private static final List<VirtualCargo> HEAVY_CONTAINER_CARGO_CATALOG = List.of(
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "ore_barge_container"), 64, 2, 18, 42, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "heavy_machinery_container"), 40, 3, 22, 95, 1),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "fortification_materials_container"), 72, 2, 20, 54, 0),
            new VirtualCargo(ResourceLocation.fromNamespaceAndPath("colonylogistics", "high_value_heavy_cargo"), 36, 3, 16, 140, 2)
    );

    public int ensureMinimumInventoryJobs(ServerLevel level) {
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        List<ColonyLogisticsState> active = data.activeColonies()
                .filter(state -> state.limits().inventoryFreightEnabled())
                .sorted(Comparator.comparingInt(ColonyLogisticsState::colonyId))
                .toList();

        if (active.isEmpty()) {
            return 0;
        }
        if (active.size() < 2 && !ColonyLogisticsConfig.allowLoopbackFreightForTesting()) {
            MultiplayerDebugLog.contractGenerationSkipped(
                    level,
                    "auto_inventory_topup",
                    "NEED_TWO_INVENTORY_COLONIES",
                    "activeInventoryColonies=" + active.size() + " loopback=false"
            );
            return 0;
        }

        int generated = 0;
        for (ColonyLogisticsState origin : active) {
            ColonyLogisticsLimits limits = origin.limits();
            int targetOpenJobs = Math.min(limits.maxOpenFreightJobs(), ColonyLogisticsConfig.marketTestInventoryJobCapPerColony());
            long existing = data.openGeneratedJobsForColony(origin.colonyId());
            long existingLowDifficulty = openLowDifficultyJobsForColony(
                    data,
                    origin.colonyId(),
                    DeliveryUnitType.INVENTORY_ITEM,
                    ColonyLogisticsConfig.marketLowDifficultyInventoryMax()
            );
            long missing = Math.max(0, targetOpenJobs - existing);
            for (int i = 0; i < missing; i++) {
                int salt = data.contracts().size() + i;
                Optional<ColonyLogisticsState> destination = pickInventoryDestination(active, origin, salt);
                if (destination.isEmpty()) break;
                boolean preferLowDifficulty = shouldPreferLowDifficultyForNext(
                        existingLowDifficulty,
                        existing + i + 1,
                        ColonyLogisticsConfig.marketLowDifficultyInventoryPercent()
                );
                LogisticsContract contract = createInventoryFreight(level, origin, destination.get(), salt, preferLowDifficulty);
                if (isLowDifficulty(contract, ColonyLogisticsConfig.marketLowDifficultyInventoryMax())) {
                    existingLowDifficulty++;
                }
                data.putContract(contract);
                MultiplayerDebugLog.contractGenerated(level, contract, "auto_inventory_topup");
                generated++;
            }
        }
        if (generated > 0) {
            data.setDirty();
        }
        return generated;
    }

    /** Compatibility alias kept for older debug commands and docs. */
    public int ensureMinimumSmallContainerJobs(ServerLevel level) {
        return ensureMinimumContainerJobs(level, Optional.of(ContainerSize.SMALL));
    }

    public int ensureMinimumContainerJobs(ServerLevel level) {
        return ensureMinimumContainerJobs(level, Optional.empty());
    }

    public int ensureMinimumContainerJobs(ServerLevel level, Optional<ContainerSize> forcedSize) {
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        List<ColonyLogisticsState> active = data.activeColonies()
                .filter(state -> state.limits().containerFreightEnabled())
                .filter(ColonyLogisticsState::hasDock)
                .filter(state -> state.limits().maxContainerSize().ordinal() >= ContainerSize.SMALL.ordinal())
                .sorted(Comparator.comparingInt(ColonyLogisticsState::colonyId))
                .toList();

        if (active.isEmpty()) {
            return 0;
        }
        if (active.size() < 2 && !ColonyLogisticsConfig.allowLoopbackContainerFreightForTesting()) {
            MultiplayerDebugLog.contractGenerationSkipped(
                    level,
                    "auto_container_topup",
                    "NEED_TWO_CONTAINER_COLONIES_WITH_DOCKS",
                    "activeContainerColonies=" + active.size() + " loopback=false"
            );
            return 0;
        }

        int generated = 0;
        for (ColonyLogisticsState origin : active) {
            ColonyLogisticsLimits limits = origin.limits();
            int targetOpenJobs = Math.min(limits.maxActiveContainerJobs(), ColonyLogisticsConfig.marketTestContainerJobCapPerColony());
            long existingOpen = data.openContainerJobsForColony(origin.colonyId());
            long existingLowDifficulty = openLowDifficultyJobsForColony(
                    data,
                    origin.colonyId(),
                    DeliveryUnitType.CONTAINER_MULTIBLOCK,
                    ColonyLogisticsConfig.marketLowDifficultyContainerMax()
            );
            long missing = Math.max(0, targetOpenJobs - existingOpen);
            for (int i = 0; i < missing; i++) {
                int salt = data.contracts().size() + i;
                Optional<ColonyLogisticsState> destination = pickContainerDestination(active, origin, salt);
                if (destination.isEmpty()) break;

                boolean preferLowDifficulty = forcedSize.isEmpty() && shouldPreferLowDifficultyForNext(
                        existingLowDifficulty,
                        existingOpen + i + 1,
                        ColonyLogisticsConfig.marketLowDifficultyContainerPercent()
                );
                Optional<ContainerSize> size = forcedSize
                        .filter(ContainerSize::isContainer)
                        .filter(requested -> canGenerateSize(origin, destination.get(), requested))
                        .or(() -> forcedSize.isPresent() ? Optional.empty() : pickContainerSize(origin, destination.get(), salt, preferLowDifficulty));
                if (size.isEmpty()) continue;

                LogisticsContract contract = createContainerFreight(level, origin, destination.get(), size.get(), salt, preferLowDifficulty);
                if (isLowDifficulty(contract, ColonyLogisticsConfig.marketLowDifficultyContainerMax())) {
                    existingLowDifficulty++;
                }
                data.putContract(contract);
                MultiplayerDebugLog.contractGenerated(level, contract, forcedSize.map(sizeValue -> "forced_container_" + sizeValue.name()).orElse("auto_container_topup"));
                generated++;
            }
        }
        if (generated > 0) {
            data.setDirty();
        }
        return generated;
    }

    /**
     * Creates a same-dock container freight contract for early single-player testing.
     *
     * <p>This deliberately keeps origin and destination on the same Container Dock so
     * the player can verify the Container Dock spawn/deliver state machine before
     * Create/Aeronautics/Sable physical transport is involved.</p>
     */
    public Optional<LogisticsContract> createLocalContainerTestJob(ServerLevel level, BlockPos dockPos, ContainerSize size) {
        if (!ColonyLogisticsConfig.allowLoopbackContainerFreightForTesting()) {
            MultiplayerDebugLog.contractGenerationSkipped(
                    level,
                    "local_container_test_job",
                    "LOOPBACK_CONTAINER_TESTING_DISABLED",
                    "dockPos=" + MultiplayerDebugLog.pos(dockPos) + " size=" + size
            );
            return Optional.empty();
        }
        if (size == null || !size.isContainer()) {
            return Optional.empty();
        }

        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        if (!dock.usable() || dock.colonyId() < 0) {
            return Optional.empty();
        }

        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        ColonyLogisticsState colony = data.colonyState(dock.colonyId());
        colony.registerDock(dockPos);

        if (!colony.limits().containerFreightEnabled() || colony.limits().maxContainerSize().ordinal() < size.ordinal()) {
            return Optional.empty();
        }

        List<VirtualCargo> catalog = catalogFor(size);
        int salt = data.contracts().size();
        VirtualCargo cargo = catalog.get(Math.floorMod(dock.colonyId() + salt, catalog.size()));
        int distance = 1;
        int gameplayWeight = cargo.gameplayWeight() * cargo.amount();
        int requiredVolume = cargo.volume() * cargo.amount();
        ContainerWeightClass weightClass = new WeightClassifier().classify(gameplayWeight, size);
        int reward = calculateContainerReward(cargo, distance, size, weightClass);
        int containerCount = containerCountFor(size, cargo, salt);
        long now = level.getGameTime();

        long pickupDeadline = deadlineFromWindow(now, ColonyLogisticsConfig.pickupWindowTicks());
        long deliveryDeadline = deadlineFromWindow(now, deliveryWindowFor(size));

        FreightJobSpec spec = new FreightJobSpec(
                DeliveryUnitType.CONTAINER_MULTIBLOCK,
                List.of(cargo),
                new ContainerRequirement(size, requiredVolume, gameplayWeight, weightClass),
                difficultyForContainerCargo(cargo, distance, size, weightClass),
                ColonyLogisticsConfig.requiredCarrierLevel(size),
                pickupDeadline,
                deliveryDeadline,
                ColonyLogisticsConfig.generatedJobsAllowLateDelivery()
        );

        LogisticsContract contract = new LogisticsContract(
                UUID.randomUUID(),
                ContractType.GENERATED_FREIGHT,
                ContractStatus.OPEN,
                dock.colonyId(),
                dock.colonyId(),
                Optional.of(dockPos.immutable()),
                Optional.of(dockPos.immutable()),
                Optional.empty(),
                Optional.of(spec),
                new RewardSpec(rewardCurrency(), reward),
                now,
                deliveryDeadline,
                containerCount,
                0,
                0
        );
        data.putContract(contract);
        MultiplayerDebugLog.contractGenerated(level, contract, "local_container_test_job");
        data.setDirty();
        return Optional.of(contract);
    }

    public List<LogisticsContract> openContracts(ServerLevel level) {
        return LogisticsMarketSavedData.get(level).contracts().stream()
                .filter(contract -> contract.type() == ContractType.GENERATED_FREIGHT)
                .filter(contract -> contract.status() == ContractStatus.OPEN)
                .sorted(Comparator.comparingLong(FreightMarketService::deadlineSortKey))
                .toList();
    }

    public List<LogisticsContract> boardContracts(ServerLevel level) {
        return boardContracts(level, Optional.empty());
    }

    /**
     * Returns the freight board snapshot ordered for gameplay visibility.
     *
     * <p>Open jobs are still shown, but contracts already assigned to the viewer are
     * promoted above newly generated OPEN jobs. Without this ordering, the board can
     * be filled by the generated-open-job cap and accepted/picked-up contracts appear
     * to vanish from the UI even though they are present in SavedData.</p>
     */
    public List<LogisticsContract> boardContracts(ServerLevel level, UUID viewerId) {
        return boardContracts(level, Optional.of(viewerId));
    }

    private List<LogisticsContract> boardContracts(ServerLevel level, Optional<UUID> viewerId) {
        return LogisticsMarketSavedData.get(level).contracts().stream()
                .filter(contract -> contract.type() == ContractType.GENERATED_FREIGHT)
                .filter(contract -> contract.status() != ContractStatus.DRAFT)
                .sorted(Comparator
                        .comparingInt((LogisticsContract contract) -> boardViewerOrder(contract, viewerId))
                        .thenComparingLong(FreightMarketService::deadlineSortKey)
                        .thenComparing(LogisticsContract::createdGameTime))
                .toList();
    }

    private int boardViewerOrder(LogisticsContract contract, Optional<UUID> viewerId) {
        boolean assignedToViewer = viewerId.isPresent() && contract.assignedPlayer().filter(viewerId.get()::equals).isPresent();
        boolean assignedToOther = contract.assignedPlayer().isPresent() && !assignedToViewer;

        if (assignedToViewer && isActiveBoardStatus(contract.status())) {
            return 0;
        }
        if (assignedToViewer && isFinishedBoardStatus(contract.status())) {
            return 1;
        }
        if (assignedToOther && isActiveBoardStatus(contract.status())) {
            return 2;
        }
        if (contract.status() == ContractStatus.OPEN) {
            return 3;
        }
        if (assignedToOther && isFinishedBoardStatus(contract.status())) {
            return 4;
        }
        return 5;
    }

    private boolean isActiveBoardStatus(ContractStatus status) {
        return status == ContractStatus.ACCEPTED
                || status == ContractStatus.PICKED_UP
                || status == ContractStatus.DELIVERED;
    }

    private boolean isFinishedBoardStatus(ContractStatus status) {
        return status == ContractStatus.COMPLETED
                || status == ContractStatus.EXPIRED
                || status == ContractStatus.FAILED
                || status == ContractStatus.CANCELLED;
    }

    public List<LogisticsContract> playerContracts(ServerLevel level, UUID playerId) {
        return LogisticsMarketSavedData.get(level).contracts().stream()
                .filter(contract -> contract.assignedPlayer().filter(playerId::equals).isPresent())
                .filter(contract -> contract.status() == ContractStatus.ACCEPTED || contract.status() == ContractStatus.PICKED_UP)
                .sorted(Comparator.comparingLong(FreightMarketService::deadlineSortKey))
                .toList();
    }


    private Optional<ColonyLogisticsState> pickInventoryDestination(List<ColonyLogisticsState> active, ColonyLogisticsState origin, int salt) {
        if (active.size() == 1 && ColonyLogisticsConfig.allowLoopbackFreightForTesting()) {
            return Optional.of(origin);
        }
        return pickDestination(active, origin, salt);
    }

    private Optional<ColonyLogisticsState> pickDestination(List<ColonyLogisticsState> active, ColonyLogisticsState origin, int salt) {
        List<ColonyLogisticsState> candidates = new ArrayList<>(active.stream()
                .filter(state -> state.colonyId() != origin.colonyId())
                .toList());
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        int index = Math.floorMod(origin.colonyId() + salt, candidates.size());
        return Optional.of(candidates.get(index));
    }

    private Optional<ColonyLogisticsState> pickContainerDestination(List<ColonyLogisticsState> active, ColonyLogisticsState origin, int salt) {
        if (active.size() == 1 && ColonyLogisticsConfig.allowLoopbackContainerFreightForTesting()) {
            return Optional.of(origin);
        }
        Optional<ColonyLogisticsState> destination = pickDestination(active, origin, salt);
        if (destination.isPresent()) {
            return destination;
        }
        return ColonyLogisticsConfig.allowLoopbackContainerFreightForTesting() ? Optional.of(origin) : Optional.empty();
    }

    private Optional<ContainerSize> pickContainerSize(ColonyLogisticsState origin, ColonyLogisticsState destination, int salt, boolean preferLowDifficulty) {
        ContainerSize max = commonMaxSize(origin, destination);
        List<ContainerStandard> candidates = ContainerStandard.availableFor(max);
        if (candidates.isEmpty()) return Optional.empty();

        if (preferLowDifficulty
                && candidates.contains(ContainerStandard.STANDARD)
                && ColonyLogisticsConfig.containerGenerationWeight(ContainerStandard.STANDARD) > 0) {
            return Optional.of(ContainerStandard.STANDARD.physicalSize());
        }

        List<ContainerStandard> weighted = new ArrayList<>();
        for (ContainerStandard candidate : candidates) {
            for (int i = 0; i < ColonyLogisticsConfig.containerGenerationWeight(candidate); i++) weighted.add(candidate);
        }
        if (weighted.isEmpty()) return Optional.empty();
        ContainerStandard selected = weighted.get(Math.floorMod(origin.colonyId() * 31 + destination.colonyId() + salt, weighted.size()));
        return Optional.of(selected.physicalSize());
    }

    private boolean canGenerateSize(ColonyLogisticsState origin, ColonyLogisticsState destination, ContainerSize requested) {
        return requested.isContainer()
                && origin.limits().maxContainerSize().ordinal() >= requested.ordinal()
                && destination.limits().maxContainerSize().ordinal() >= requested.ordinal();
    }

    private ContainerSize commonMaxSize(ColonyLogisticsState origin, ColonyLogisticsState destination) {
        ContainerSize originMax = origin.limits().maxContainerSize();
        ContainerSize destinationMax = destination.limits().maxContainerSize();
        return originMax.ordinal() <= destinationMax.ordinal() ? originMax : destinationMax;
    }

    private LogisticsContract createInventoryFreight(ServerLevel level, ColonyLogisticsState origin, ColonyLogisticsState destination, int salt, boolean preferLowDifficulty) {
        BlockPos originPos = origin.logisticsOfficePos();
        BlockPos destinationPos = destination.logisticsOfficePos();
        int distance = Math.max(1, (int) Math.sqrt(originPos.distSqr(destinationPos)));
        VirtualCargo cargo = selectInventoryCargo(origin, destination, distance, salt, preferLowDifficulty);
        int reward = calculateInventoryReward(cargo, distance);
        long now = level.getGameTime();

        long pickupDeadline = deadlineFromWindow(now, ColonyLogisticsConfig.pickupWindowTicks());
        long deliveryDeadline = deadlineFromWindow(now, ColonyLogisticsConfig.inventoryDeliveryWindowTicks());

        FreightJobSpec spec = new FreightJobSpec(
                DeliveryUnitType.INVENTORY_ITEM,
                List.of(cargo),
                new ContainerRequirement(ContainerSize.NONE, 0, 0, ContainerWeightClass.EMPTY),
                difficultyForInventoryCargo(cargo, distance),
                ColonyLogisticsConfig.inventoryRequiredCarrierLevel(),
                pickupDeadline,
                deliveryDeadline,
                ColonyLogisticsConfig.generatedJobsAllowLateDelivery()
        );

        return new LogisticsContract(
                UUID.randomUUID(),
                ContractType.GENERATED_FREIGHT,
                ContractStatus.OPEN,
                origin.colonyId(),
                destination.colonyId(),
                Optional.of(originPos),
                Optional.of(destinationPos),
                Optional.empty(),
                Optional.of(spec),
                new RewardSpec(rewardCurrency(), reward),
                now,
                deliveryDeadline,
                0,
                0,
                0
        );
    }

    private LogisticsContract createContainerFreight(ServerLevel level, ColonyLogisticsState origin, ColonyLogisticsState destination, ContainerSize size, int salt) {
        return createContainerFreight(level, origin, destination, size, salt, false);
    }

    private LogisticsContract createContainerFreight(ServerLevel level, ColonyLogisticsState origin, ColonyLogisticsState destination, ContainerSize size, int salt, boolean preferLowDifficulty) {
        BlockPos originDock = selectOriginDock(origin, destination, salt);
        BlockPos destinationDock = selectDestinationDock(origin, destination, originDock, salt);
        int distance = Math.max(1, (int) Math.sqrt(originDock.distSqr(destinationDock)));
        VirtualCargo cargo = selectContainerCargo(size, distance, salt, preferLowDifficulty);
        int gameplayWeight = cargo.gameplayWeight() * cargo.amount();
        int requiredVolume = cargo.volume() * cargo.amount();
        ContainerWeightClass weightClass = new WeightClassifier().classify(gameplayWeight, size);
        int reward = calculateContainerReward(cargo, distance, size, weightClass);
        int containerCount = containerCountFor(size, cargo, salt);
        long now = level.getGameTime();

        long pickupDeadline = deadlineFromWindow(now, ColonyLogisticsConfig.pickupWindowTicks());
        long deliveryDeadline = deadlineFromWindow(now, deliveryWindowFor(size));

        FreightJobSpec spec = new FreightJobSpec(
                DeliveryUnitType.CONTAINER_MULTIBLOCK,
                List.of(cargo),
                new ContainerRequirement(size, requiredVolume, gameplayWeight, weightClass),
                difficultyForContainerCargo(cargo, distance, size, weightClass),
                ColonyLogisticsConfig.requiredCarrierLevel(size),
                pickupDeadline,
                deliveryDeadline,
                ColonyLogisticsConfig.generatedJobsAllowLateDelivery()
        );

        return new LogisticsContract(
                UUID.randomUUID(),
                ContractType.GENERATED_FREIGHT,
                ContractStatus.OPEN,
                origin.colonyId(),
                destination.colonyId(),
                Optional.of(originDock),
                Optional.of(destinationDock),
                Optional.empty(),
                Optional.of(spec),
                new RewardSpec(rewardCurrency(), reward),
                now,
                deliveryDeadline,
                containerCount,
                0,
                0
        );
    }


    private static long deadlineFromWindow(long now, long windowTicks) {
        return windowTicks <= 0L ? 0L : now + windowTicks;
    }

    private static long deadlineSortKey(LogisticsContract contract) {
        return contract.expiresGameTime() <= 0L ? Long.MAX_VALUE : contract.expiresGameTime();
    }

    private int containerCountFor(ContainerSize size, VirtualCargo cargo, int salt) {
        ContainerStandard standard = ContainerStandard.fromSize(size);
        if (standard == ContainerStandard.LARGE && cargo.fragility() > 0) {
            return ColonyLogisticsConfig.largeFragileContainerCount();
        }
        return ColonyLogisticsConfig.defaultContainerCount(size);
    }

    private BlockPos selectOriginDock(ColonyLogisticsState origin, ColonyLogisticsState destination, int salt) {
        List<BlockPos> docks = origin.dockPositions();
        if (docks.isEmpty()) {
            return origin.primaryDockPos();
        }
        if (origin.colonyId() == destination.colonyId() && docks.size() > 1) {
            return docks.get(Math.floorMod(salt, docks.size()));
        }
        return origin.primaryDockPos();
    }

    private BlockPos selectDestinationDock(ColonyLogisticsState origin, ColonyLogisticsState destination, BlockPos originDock, int salt) {
        List<BlockPos> docks = destination.dockPositions();
        if (docks.isEmpty()) {
            return destination.primaryDockPos();
        }
        if (origin.colonyId() == destination.colonyId()) {
            if (docks.size() == 1) {
                return docks.get(0);
            }
            for (int offset = 1; offset <= docks.size(); offset++) {
                BlockPos candidate = docks.get(Math.floorMod(salt + offset, docks.size()));
                if (!candidate.equals(originDock)) {
                    return candidate;
                }
            }
        }
        return destination.primaryDockPos();
    }

    private List<VirtualCargo> catalogFor(ContainerSize size) {
        return switch (size) {
            case SMALL -> SMALL_CONTAINER_CARGO_CATALOG;
            case MEDIUM -> SMALL_CONTAINER_CARGO_CATALOG;
            case LARGE -> LARGE_CONTAINER_CARGO_CATALOG;
            case HEAVY -> HEAVY_CONTAINER_CARGO_CATALOG;
            default -> SMALL_CONTAINER_CARGO_CATALOG;
        };
    }


    private VirtualCargo selectInventoryCargo(ColonyLogisticsState origin, ColonyLogisticsState destination, int distance, int salt, boolean preferLowDifficulty) {
        if (!preferLowDifficulty) {
            return INVENTORY_CARGO_CATALOG.get(Math.floorMod(origin.colonyId() + destination.colonyId() + salt, INVENTORY_CARGO_CATALOG.size()));
        }

        List<VirtualCargo> lowDifficulty = INVENTORY_CARGO_CATALOG.stream()
                .filter(cargo -> difficultyForInventoryCargo(cargo, distance).ordinal() <= ColonyLogisticsConfig.marketLowDifficultyInventoryMax().ordinal())
                .toList();
        List<VirtualCargo> pool = lowDifficulty.isEmpty() ? INVENTORY_CARGO_CATALOG : lowDifficulty;
        return pool.get(Math.floorMod(origin.colonyId() * 17 + destination.colonyId() + salt, pool.size()));
    }

    private VirtualCargo selectContainerCargo(ContainerSize size, int distance, int salt, boolean preferLowDifficulty) {
        List<VirtualCargo> catalog = catalogFor(size);
        if (!preferLowDifficulty) {
            return catalog.get(Math.floorMod(salt, catalog.size()));
        }

        List<VirtualCargo> lowDifficulty = catalog.stream()
                .filter(cargo -> {
                    int gameplayWeight = cargo.gameplayWeight() * cargo.amount();
                    ContainerWeightClass weightClass = new WeightClassifier().classify(gameplayWeight, size);
                    return difficultyForContainerCargo(cargo, distance, size, weightClass).ordinal()
                            <= ColonyLogisticsConfig.marketLowDifficultyContainerMax().ordinal();
                })
                .toList();
        List<VirtualCargo> pool = lowDifficulty.isEmpty() ? catalog : lowDifficulty;
        return pool.get(Math.floorMod(salt, pool.size()));
    }

    private long openLowDifficultyJobsForColony(LogisticsMarketSavedData data, int originColonyId, DeliveryUnitType unitType, FreightDifficulty maxDifficulty) {
        return data.contracts().stream()
                .filter(contract -> contract.type() == ContractType.GENERATED_FREIGHT)
                .filter(contract -> contract.status() == ContractStatus.OPEN)
                .filter(contract -> contract.originColonyId() == originColonyId)
                .filter(contract -> contract.freightSpec().isPresent())
                .filter(contract -> contract.freightSpec().get().deliveryUnitType() == unitType)
                .filter(contract -> contract.freightSpec().get().difficulty().ordinal() <= maxDifficulty.ordinal())
                .count();
    }

    private boolean isLowDifficulty(LogisticsContract contract, FreightDifficulty maxDifficulty) {
        return contract.freightSpec()
                .map(spec -> spec.difficulty().ordinal() <= maxDifficulty.ordinal())
                .orElse(false);
    }

    private boolean shouldPreferLowDifficultyForNext(long existingLowDifficulty, long projectedTotal, int percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return existingLowDifficulty * 100L < Math.max(1L, projectedTotal) * (long) percent;
    }

    private long deliveryWindowFor(ContainerSize size) {
        return switch (size) {
            case SMALL, MEDIUM -> ColonyLogisticsConfig.deliveryWindowTicks(ContainerSize.SMALL);
            case LARGE -> ColonyLogisticsConfig.deliveryWindowTicks(ContainerSize.LARGE);
            case HEAVY -> ColonyLogisticsConfig.deliveryWindowTicks(ContainerSize.HEAVY);
            default -> ColonyLogisticsConfig.inventoryDeliveryWindowTicks();
        };
    }

    private ResourceLocation rewardCurrency() {
        return new CurrencyService().defaultRewardCurrencyItemId();
    }

    private int calculateInventoryReward(VirtualCargo cargo, int distance) {
        int distanceBonus = Math.max(
                ColonyLogisticsConfig.inventoryRewardMinDistanceBonus(),
                distance / ColonyLogisticsConfig.inventoryRewardDistanceDivisor()
        );
        double cargoBonus = cargo.value() * ColonyLogisticsConfig.inventoryRewardCargoValueMultiplier()
                + cargo.gameplayWeight() * ColonyLogisticsConfig.inventoryRewardCargoWeightMultiplier()
                + cargo.fragility() * ColonyLogisticsConfig.inventoryRewardFragilityMultiplier();
        double total = (ColonyLogisticsConfig.inventoryRewardBase() + distanceBonus + cargoBonus)
                * ColonyLogisticsConfig.inventoryRewardGlobalMultiplier();
        return Math.max(1, (int) Math.round(total));
    }

    private int calculateContainerReward(VirtualCargo cargo, int distance, ContainerSize size, ContainerWeightClass weightClass) {
        int distanceBonus = Math.max(
                ColonyLogisticsConfig.containerRewardMinDistanceBonus(),
                distance / ColonyLogisticsConfig.containerRewardDistanceDivisor()
        );
        double cargoBonus = cargo.value() * cargo.amount() * ColonyLogisticsConfig.containerRewardCargoValueMultiplier()
                + cargo.gameplayWeight() * cargo.amount() * ColonyLogisticsConfig.containerRewardCargoWeightMultiplier();
        double sizeBonus = size.volume() * ColonyLogisticsConfig.containerRewardSizeVolumeMultiplier()
                + size.baseGameplayWeight() * ColonyLogisticsConfig.containerRewardSizeBaseWeightMultiplier();
        int physicsBonus = ColonyLogisticsConfig.containerWeightClassBonus(weightClass.name());
        double total = (ColonyLogisticsConfig.containerRewardBase() + distanceBonus + cargoBonus + sizeBonus + physicsBonus)
                * ColonyLogisticsConfig.containerStandardRewardMultiplier(size)
                * ColonyLogisticsConfig.containerRewardGlobalMultiplier();
        return Math.max(1, (int) Math.round(total));
    }

    private FreightDifficulty difficultyForInventoryCargo(VirtualCargo cargo, int distance) {
        int score = distance / 100 + cargo.value() / 20 + cargo.fragility() * 3;
        if (score >= 10) return FreightDifficulty.HARD;
        if (score >= 5) return FreightDifficulty.NORMAL;
        return FreightDifficulty.EASY;
    }

    private FreightDifficulty difficultyForContainerCargo(VirtualCargo cargo, int distance, ContainerSize size, ContainerWeightClass weightClass) {
        int score = 15 + distance / 80 + size.volume() / 10 + cargo.gameplayWeight() * cargo.amount() / 20 + weightClass.ordinal() * 5;
        if (score >= 55) return FreightDifficulty.EXPERT;
        if (score >= 35) return FreightDifficulty.HARD;
        if (score >= 20) return FreightDifficulty.NORMAL;
        return FreightDifficulty.EASY;
    }
}
