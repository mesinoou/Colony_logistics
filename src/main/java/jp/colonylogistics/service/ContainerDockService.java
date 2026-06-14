package jp.colonylogistics.service;

import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.colony.ColonyLogisticsLimits;
import jp.colonylogistics.colony.ColonyLogisticsState;
import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.container.ContainerManifest;
import jp.colonylogistics.container.ContainerMultiblockBuilder;
import jp.colonylogistics.container.ContainerRequirement;
import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.contract.RewardSpec;
import jp.colonylogistics.currency.CurrencyService;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.dock.DockMode;
import jp.colonylogistics.freight.FreightJobSpec;
import jp.colonylogistics.physics.DeliveryRangeResolvers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ContainerDockService {
    /**
     * Phase 17.8.2 moves spawning back inside the production Container Dock.
     * The deterministic interior lane places the first three 3x7x3 containers
     * close to the Hut anchor/control block, so the original 18 block test radius
     * is enough again even for worlds that still have older TOML files.
     */
    public static final double MIN_UNIFIED_CONTAINER_LANE_RADIUS = 18.0D;

    public static double deliveryRadius() {
        return Math.max(ColonyLogisticsConfig.dockDeliveryRadius(), MIN_UNIFIED_CONTAINER_LANE_RADIUS);
    }

    public static double containerRecognitionRadius() {
        return Math.max(ColonyLogisticsConfig.dockContainerRecognitionRadius(), MIN_UNIFIED_CONTAINER_LANE_RADIUS);
    }

    public static final double DEFAULT_DELIVERY_RADIUS = MIN_UNIFIED_CONTAINER_LANE_RADIUS;

    private final ContainerMultiblockBuilder builder = new ContainerMultiblockBuilder();

    public SpawnResult spawnForAcceptedContract(ServerPlayer player, BlockPos dockPos, UUID contractId, BlockPos corePos) {
        ServerLevel level = player.serverLevel();
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        if (!dock.usable()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.INVALID_DOCK.name(), dockPos, corePos, "contract=" + contractId);
            return SpawnResult.INVALID_DOCK;
        }
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        DockMode mode = data.dockMode(dock.key());
        if (!mode.canExport()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.EXPORT_DISABLED.name(), dockPos, corePos, "contract=" + contractId + " mode=" + mode);
            return SpawnResult.EXPORT_DISABLED;
        }
        if (dock.colonyId() < 0) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.NO_COLONY.name(), dockPos, corePos, "contract=" + contractId);
            return SpawnResult.NO_COLONY;
        }

        Optional<LogisticsContract> optionalContract = data.contract(contractId);
        if (optionalContract.isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.INVALID_CONTRACT.name(), dockPos, corePos, "contract=" + contractId);
            return SpawnResult.INVALID_CONTRACT;
        }

        LogisticsContract contract = optionalContract.get();
        if ((contract.status() != ContractStatus.ACCEPTED && contract.status() != ContractStatus.PICKED_UP)
                || contract.assignedPlayer().filter(player.getUUID()::equals).isEmpty()
                || !contract.canSpawnMoreContainers()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.INVALID_STATUS.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract));
            return SpawnResult.INVALID_STATUS;
        }
        if (contract.originColonyId() != dock.colonyId()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.WRONG_COLONY.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract) + " dockColony=" + dock.colonyId());
            return SpawnResult.WRONG_COLONY;
        }
        if (contract.originDockPos().filter(pos -> pos.equals(dockPos)).isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.WRONG_DOCK.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract));
            return SpawnResult.WRONG_DOCK;
        }
        if (contract.freightSpec().isEmpty() || !contract.freightSpec().get().containerRequirement().requiresContainer()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.NOT_CONTAINER_JOB.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract));
            return SpawnResult.NOT_CONTAINER_JOB;
        }

        ColonyLogisticsState colony = data.colonyState(dock.colonyId());
        ColonyLogisticsLimits limits = colony.limits();
        ContainerRequirement requirement = contract.freightSpec().get().containerRequirement();
        if (!limits.containerFreightEnabled() || requirement.size().ordinal() > limits.maxContainerSize().ordinal()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.NOT_UNLOCKED.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract) + " maxSize=" + limits.maxContainerSize());
            return SpawnResult.NOT_UNLOCKED;
        }
        if (!colony.canStartContainerJob()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.LIMIT_REACHED.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract));
            return SpawnResult.LIMIT_REACHED;
        }
        Direction dockForward = dock.cargoForward();
        Optional<BlockPos> spawnCorePos = findAvailableContractCorePos(level, dockPos, requirement.size(), dockForward, dock.buildingLevel());
        if (spawnCorePos.isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.NO_SPACE.name(), dockPos, corePos, MultiplayerDebugLog.contractSummary(contract) + " requestedCore=" + MultiplayerDebugLog.pos(corePos));
            return SpawnResult.NO_SPACE;
        }
        BlockPos actualCorePos = spawnCorePos.get();

        int batchIndex = contract.spawnedContainerCount() + 1;
        int batchCount = contract.effectiveContainerCount();
        ContainerManifest manifest = new ContainerManifest(
                UUID.randomUUID(),
                contract.id(),
                contract.originColonyId(),
                contract.destinationColonyId(),
                player.getUUID(),
                dockPos.immutable(),
                contract.destinationDockPos().orElse(BlockPos.ZERO),
                requirement.size(),
                requirement.weightClass(),
                requirement.cargoGameplayWeight(),
                true,
                level.getGameTime(),
                batchIndex,
                batchCount,
                dockForward
        );
        if (!builder.place(level, actualCorePos, manifest)) {
            MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.PLACEMENT_FAILED.name(), dockPos, actualCorePos, MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return SpawnResult.PLACEMENT_FAILED;
        }
        SafeSystemChat.send(player, Component.translatable("message.colonylogistics.dock.spawned_at", actualCorePos.toShortString()));
        if (contract.spawnedContainerCount() == 0) {
            colony.incrementActiveContainerJobs();
        }
        LogisticsContract spawned = contract.withSpawnedContainer();
        data.replaceContract(spawned);
        data.setDirty();
        MultiplayerDebugLog.containerAction(player, "spawn", SpawnResult.SUCCESS.name(), dockPos, actualCorePos, MultiplayerDebugLog.contractSummary(spawned) + " " + MultiplayerDebugLog.manifestSummary(manifest));
        return SpawnResult.SUCCESS;
    }

    public DeliveryResult deliverContainer(ServerPlayer player, BlockPos dockPos, FreightContainerCoreBlockEntity container) {
        ServerLevel level = player.serverLevel();
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        if (!dock.usable()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.INVALID_DOCK.name(), dockPos, container.getBlockPos(), "");
            return DeliveryResult.INVALID_DOCK;
        }
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        DockMode mode = data.dockMode(dock.key());
        if (!mode.canImport()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.IMPORT_DISABLED.name(), dockPos, container.getBlockPos(), "mode=" + mode);
            return DeliveryResult.IMPORT_DISABLED;
        }
        if (dock.colonyId() < 0) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.NO_COLONY.name(), dockPos, container.getBlockPos(), "");
            return DeliveryResult.NO_COLONY;
        }
        if (container.invalid() || container.manifest().isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.INVALID_CONTAINER.name(), dockPos, container.getBlockPos(), "invalid=" + container.invalid());
            return DeliveryResult.INVALID_CONTAINER;
        }

        ContainerManifest manifest = container.manifest().get();
        Optional<LogisticsContract> optionalContract = data.contract(manifest.contractId());
        if (optionalContract.isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.INVALID_CONTRACT.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.INVALID_CONTRACT;
        }

        LogisticsContract contract = optionalContract.get();
        if (contract.status() != ContractStatus.PICKED_UP) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.INVALID_STATUS.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.INVALID_STATUS;
        }
        if (contract.assignedPlayer().filter(player.getUUID()::equals).isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.WRONG_PLAYER.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.WRONG_PLAYER;
        }
        if (!manifest.assignedPlayer().equals(player.getUUID())) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.WRONG_PLAYER.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.WRONG_PLAYER;
        }
        if (contract.destinationColonyId() != dock.colonyId()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.WRONG_COLONY.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " dockColony=" + dock.colonyId());
            return DeliveryResult.WRONG_COLONY;
        }
        if (manifest.destinationColonyId() != dock.colonyId()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.WRONG_COLONY.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.manifestSummary(manifest) + " dockColony=" + dock.colonyId());
            return DeliveryResult.WRONG_COLONY;
        }
        if (!manifest.destinationDockPos().equals(dockPos)) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.WRONG_DOCK.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.WRONG_DOCK;
        }
        if (!manifest.sealed()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.NOT_SEALED.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.NOT_SEALED;
        }
        if (contract.freightSpec().isEmpty()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.INVALID_CONTRACT.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract));
            return DeliveryResult.INVALID_CONTRACT;
        }

        FreightJobSpec spec = contract.freightSpec().get();
        ContainerRequirement requirement = spec.containerRequirement();
        if (!requirement.requiresContainer()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.NOT_CONTAINER_JOB.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract));
            return DeliveryResult.NOT_CONTAINER_JOB;
        }
        if (requirement.size() != manifest.size()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.SIZE_MISMATCH.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.SIZE_MISMATCH;
        }
        if (requirement.weightClass() != manifest.weightClass()) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.WEIGHT_MISMATCH.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " " + MultiplayerDebugLog.manifestSummary(manifest));
            return DeliveryResult.WEIGHT_MISMATCH;
        }
        if (!isWithinDeliveryRange(level, dockPos, container.getBlockPos(), deliveryRadius())) {
            MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.TOO_FAR.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " distance=" + deliveryDistance(level, dockPos, container.getBlockPos()) + " radius=" + deliveryRadius());
            return DeliveryResult.TOO_FAR;
        }

        int rewardAmount = contract.reward().currencyAmount();
        long now = level.getGameTime();
        if (ColonyLogisticsConfig.deliveryWindowTicks(requirement.size()) > 0 && spec.deliveryDeadline() > 0 && now > spec.deliveryDeadline()) {
            if (!spec.allowLateDelivery()) {
                data.replaceContract(contract.withStatus(ContractStatus.FAILED));
                data.carrierProfile(player.getUUID()).recordFailedJob();
                data.colonyState(contract.originColonyId()).decrementActiveContainerJobs();
                data.setDirty();
                MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.EXPIRED.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract) + " now=" + now + " deadline=" + spec.deliveryDeadline());
                return DeliveryResult.EXPIRED;
            }
            rewardAmount = ColonyLogisticsConfig.lateAdjustedReward(rewardAmount);
        }

        LogisticsContract progressed = contract.withDeliveredContainer();
        CurrencyService currencyService = new CurrencyService();
        Optional<RewardSpec> payableReward = Optional.empty();
        if (progressed.status() == ContractStatus.COMPLETED) {
            payableReward = currencyService.payableReward(contract.reward(), rewardAmount);
            if (payableReward.isEmpty()) {
                SafeSystemChat.send(player, Component.translatable(
                        "message.colonylogistics.currency.unavailable",
                        contract.reward().currencyItemId()
                ));
                MultiplayerDebugLog.containerAction(player, "deliver", DeliveryResult.REWARD_UNAVAILABLE.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(contract));
                return DeliveryResult.REWARD_UNAVAILABLE;
            }
        }

        ContainerMultiblockBuilder.RemovalResult removal = builder.removeWithResult(level, container.getBlockPos(), manifest);
        data.replaceContract(progressed);
        if (progressed.status() == ContractStatus.COMPLETED) {
            data.colonyState(contract.originColonyId()).decrementActiveContainerJobs();
            CurrencyService.PaymentResult payment = currencyService.payResolvedToPlayer(player, payableReward.get());
            long estimatedDistance = contract.originDockPos().flatMap(origin -> contract.destinationDockPos().map(origin::distManhattan)).orElse(0);
            int cargoAmount = spec.cargo().stream().mapToInt(cargo -> cargo.amount()).sum();
            data.carrierProfile(player.getUUID()).recordCompletedJob(payment.paid() ? payment.amount() : 0, cargoAmount, estimatedDistance);
        } else {
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.dock.partial_delivery", progressed.deliveredContainerCount(), progressed.requiredContainerCount()));
        }
        data.setDirty();
        DeliveryResult result = removal.removedAny() ? DeliveryResult.SUCCESS : DeliveryResult.REMOVED_NOTHING;
        MultiplayerDebugLog.containerAction(player, "deliver", result.name(), dockPos, container.getBlockPos(), MultiplayerDebugLog.contractSummary(progressed) + " " + MultiplayerDebugLog.manifestSummary(manifest) + " " + removal.debugSummary());
        return result;
    }

    public Optional<FreightContainerCoreBlockEntity> findCoreForContainerBlock(ServerLevel level, BlockPos containerPos) {
        return builder.findCoreForContainerBlock(level, containerPos);
    }

    /**
     * Builds a read-only diagnostic snapshot for a nearby container at a Dock.
     *
     * <p>This intentionally mirrors the hard validation in {@link #deliverContainer}
     * without mutating contracts or removing blocks. It is used by both the Dock
     * GUI and the /colonylogistics container diagnose command so test failures
     * have the same status wording everywhere.</p>
     */
    public ContainerDeliveryAnalysis analyzeDelivery(ServerLevel level, ServerPlayer player, BlockPos dockPos, FreightContainerCoreBlockEntity container) {
        double distance = deliveryDistance(level, dockPos, container.getBlockPos());

        if (container.invalid() || container.manifest().isEmpty()) {
            return new ContainerDeliveryAnalysis(false, "INVALID_CONTAINER", distance, "MISSING", "unknown", "-", "No valid sealed manifest was found on this core.");
        }

        ContainerManifest manifest = container.manifest().get();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<LogisticsContract> optionalContract = data.contract(manifest.contractId());

        String contractStatus = optionalContract.map(contract -> contract.status().name()).orElse("MISSING");
        String cargoName = optionalContract
                .flatMap(LogisticsContract::freightSpec)
                .flatMap(spec -> spec.cargo().stream().findFirst())
                .map(cargo -> cargo.cargoId().toString())
                .orElse("unknown");
        String expectedContainer = optionalContract
                .flatMap(LogisticsContract::freightSpec)
                .map(spec -> spec.containerRequirement().size().name() + " / " + spec.containerRequirement().weightClass().name())
                .orElse("-");

        if (optionalContract.isEmpty()) {
            return new ContainerDeliveryAnalysis(false, "INVALID_CONTRACT", distance, contractStatus, cargoName, expectedContainer, "No saved contract exists for this manifest id.");
        }

        LogisticsContract contract = optionalContract.get();
        if (contract.status() != ContractStatus.PICKED_UP) {
            return new ContainerDeliveryAnalysis(false, "STATUS_" + contract.status().name(), distance, contractStatus, cargoName, expectedContainer, "Contract must be PICKED_UP before dock delivery.");
        }
        if (contract.assignedPlayer().filter(player.getUUID()::equals).isEmpty() || !manifest.assignedPlayer().equals(player.getUUID())) {
            return new ContainerDeliveryAnalysis(false, "WRONG_PLAYER", distance, contractStatus, cargoName, expectedContainer, "This container is assigned to another carrier.");
        }
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        if (contract.destinationColonyId() != dock.colonyId() || manifest.destinationColonyId() != dock.colonyId()) {
            return new ContainerDeliveryAnalysis(false, "WRONG_COLONY", distance, contractStatus, cargoName, expectedContainer, "Move this container to a Dock in the destination colony.");
        }
        if (!manifest.destinationDockPos().equals(dockPos)) {
            return new ContainerDeliveryAnalysis(false, "WRONG_DOCK", distance, contractStatus, cargoName, expectedContainer, "Destination Dock is " + manifest.destinationDockPos().toShortString() + ".");
        }
        if (!manifest.sealed()) {
            return new ContainerDeliveryAnalysis(false, "NOT_SEALED", distance, contractStatus, cargoName, expectedContainer, "This container manifest is not sealed.");
        }
        if (contract.freightSpec().isEmpty()) {
            return new ContainerDeliveryAnalysis(false, "INVALID_CONTRACT", distance, contractStatus, cargoName, expectedContainer, "The saved contract has no freight specification.");
        }

        FreightJobSpec spec = contract.freightSpec().get();
        ContainerRequirement requirement = spec.containerRequirement();
        if (!requirement.requiresContainer()) {
            return new ContainerDeliveryAnalysis(false, "NOT_CONTAINER_JOB", distance, contractStatus, cargoName, expectedContainer, "The saved contract is not a container freight job.");
        }
        if (requirement.size() != manifest.size()) {
            return new ContainerDeliveryAnalysis(false, "SIZE_MISMATCH", distance, contractStatus, cargoName, expectedContainer, "Manifest size does not match the contract.");
        }
        if (requirement.weightClass() != manifest.weightClass()) {
            return new ContainerDeliveryAnalysis(false, "WEIGHT_MISMATCH", distance, contractStatus, cargoName, expectedContainer, "Manifest weight class does not match the contract.");
        }
        if (!isWithinDeliveryRange(level, dockPos, container.getBlockPos(), deliveryRadius())) {
            return new ContainerDeliveryAnalysis(false, "TOO_FAR", distance, contractStatus, cargoName, expectedContainer, "Move within delivery radius " + String.format(java.util.Locale.ROOT, "%.1f", deliveryRadius()) + ".");
        }

        return new ContainerDeliveryAnalysis(true, "OK", distance, contractStatus, cargoName, expectedContainer, "Ready to deliver.");
    }



    /**
     * Finds a nearby free core position for test-friendly container spawning.
     *
     * <p>The GUI sends a suggested position, but production-style blueprints
     * contain floors, rails, gantries, and control booth details. Candidate
     * offsets therefore try a deterministic nearest-first indoor lane first.
     * Debug-only fallback rings remain available for the /container candidates
     * command, but real contract spawning is intentionally restricted to the
     * building interior so it never silently places a contract container outside
     * the production Dock frame when one indoor pad is blocked.</p>
     */
    public Optional<BlockPos> findAvailableCorePos(ServerLevel level, BlockPos dockPos, BlockPos preferredCorePos, ContainerSize size) {
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        return findAvailableCorePos(level, dockPos, preferredCorePos, size, dock.cargoForward(), dock.buildingLevel());
    }

    public Optional<BlockPos> findAvailableContractCorePos(ServerLevel level, BlockPos dockPos, ContainerSize size) {
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        return findAvailableContractCorePos(level, dockPos, size, dock.cargoForward(), dock.buildingLevel());
    }

    public Optional<BlockPos> findAvailableContractCorePos(ServerLevel level, BlockPos dockPos, ContainerSize size, Direction dockForward) {
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        return findAvailableContractCorePos(level, dockPos, size, dockForward, dock.buildingLevel());
    }

    public Optional<BlockPos> findAvailableContractCorePos(ServerLevel level, BlockPos dockPos, ContainerSize size, Direction dockForward, int buildingLevel) {
        Direction forward = horizontal(dockForward);
        for (BlockPos candidate : contractSpawnCandidateCorePositions(dockPos, size, forward, buildingLevel)) {
            if (isWithinDeliveryRange(level, dockPos, candidate, deliveryRadius()) && builder.hasSpace(level, candidate, size, forward)) {
                return Optional.of(candidate.immutable());
            }
        }
        return Optional.empty();
    }

    private Optional<BlockPos> findAvailableCorePos(ServerLevel level, BlockPos dockPos, BlockPos preferredCorePos, ContainerSize size, Direction dockForward, int buildingLevel) {
        Direction forward = horizontal(dockForward);
        for (BlockPos candidate : spawnCandidateCorePositions(dockPos, preferredCorePos, size, forward, buildingLevel)) {
            if (isWithinDeliveryRange(level, dockPos, candidate, deliveryRadius()) && builder.hasSpace(level, candidate, size, forward)) {
                return Optional.of(candidate.immutable());
            }
        }
        return Optional.empty();
    }

    public boolean hasSpaceForContainer(ServerLevel level, BlockPos corePos, ContainerSize size) {
        return builder.hasSpace(level, corePos, size);
    }

    public boolean hasSpaceForContainer(ServerLevel level, BlockPos dockPos, BlockPos corePos, ContainerSize size) {
        return builder.hasSpace(level, corePos, size, dockCargoForward(level, dockPos));
    }

    public Direction dockCargoForward(ServerLevel level, BlockPos dockPos) {
        return horizontal(ResolvedLogisticsBuilding.resolve(level, dockPos).cargoForward());
    }

    public List<BlockPos> spawnCandidateCorePositions(ServerLevel level, BlockPos dockPos, BlockPos preferredCorePos, ContainerSize size) {
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        return spawnCandidateCorePositions(dockPos, preferredCorePos, size, dock.cargoForward(), dock.buildingLevel());
    }

    public List<BlockPos> spawnCandidateCorePositions(BlockPos dockPos, BlockPos preferredCorePos, ContainerSize size) {
        return spawnCandidateCorePositions(dockPos, preferredCorePos, size, Direction.SOUTH, 5);
    }

    public List<BlockPos> contractSpawnCandidateCorePositions(ServerLevel level, BlockPos dockPos, ContainerSize size) {
        ResolvedLogisticsBuilding dock = ResolvedLogisticsBuilding.resolve(level, dockPos);
        return contractSpawnCandidateCorePositions(dockPos, size, dock.cargoForward(), dock.buildingLevel());
    }

    public List<BlockPos> contractSpawnCandidateCorePositions(BlockPos dockPos, ContainerSize size, Direction dockForward) {
        return contractSpawnCandidateCorePositions(dockPos, size, dockForward, 5);
    }

    public List<BlockPos> contractSpawnCandidateCorePositions(BlockPos dockPos, ContainerSize size, Direction dockForward, int buildingLevel) {
        Direction forward = horizontal(dockForward);
        Set<BlockPos> candidates = new LinkedHashSet<>();
        addInteriorDockLaneCandidates(candidates, dockPos, size, forward, buildingLevel);
        return new ArrayList<>(candidates);
    }

    public List<BlockPos> spawnCandidateCorePositions(BlockPos dockPos, BlockPos preferredCorePos, ContainerSize size, Direction dockForward) {
        return spawnCandidateCorePositions(dockPos, preferredCorePos, size, dockForward, 5);
    }

    public List<BlockPos> spawnCandidateCorePositions(BlockPos dockPos, BlockPos preferredCorePos, ContainerSize size, Direction dockForward, int buildingLevel) {
        Direction forward = horizontal(dockForward);
        Set<BlockPos> candidates = new LinkedHashSet<>();
        candidates.addAll(contractSpawnCandidateCorePositions(dockPos, size, forward, buildingLevel));

        /*
         * Client-provided suggestions are kept only for diagnostics after the
         * production-safe indoor list. Contract spawning ignores this value and
         * calls findAvailableContractCorePos(), which prevents stale GUI/config
         * payloads from placing containers outside the building.
         */
        if (preferredCorePos != null) {
            candidates.add(preferredCorePos.immutable());
        }

        addApronGridCandidates(candidates, dockPos, size, forward);

        int xClearance = sideCoreOffset(
                ColonyLogisticsConfig.dockContainerSpawnDockHalfX(),
                size.halfWidth(),
                ColonyLogisticsConfig.dockContainerSpawnHorizontalGap()
        );
        int zClearance = sideCoreOffset(
                ColonyLogisticsConfig.dockContainerSpawnDockHalfZ(),
                size.halfDepth(),
                ColonyLogisticsConfig.dockContainerSpawnHorizontalGap()
        );
        int sideCoreY = ColonyLogisticsConfig.dockContainerSpawnBottomYOffset() + size.halfHeight();
        int topCoreY = ColonyLogisticsConfig.dockContainerSpawnDockOccupiedHeight()
                + ColonyLogisticsConfig.dockContainerSpawnTopGap()
                + size.halfHeight();
        int secondRingX = xClearance + size.width() + ColonyLogisticsConfig.dockContainerSpawnExtraRingGap();
        int secondRingZ = zClearance + size.depth() + ColonyLogisticsConfig.dockContainerSpawnExtraRingGap();

        addHorizontalRing(candidates, dockPos, sideCoreY, xClearance, zClearance);
        addHorizontalRing(candidates, dockPos, topCoreY, xClearance, zClearance);

        // A second ring is useful for small/medium containers on cluttered test pads.
        addHorizontalRing(candidates, dockPos, sideCoreY, secondRingX, secondRingZ);

        return new ArrayList<>(candidates);
    }

    private void addInteriorDockLaneCandidates(Set<BlockPos> candidates, BlockPos dockPos, ContainerSize size, Direction dockForward, int buildingLevel) {
        int coreY = ColonyLogisticsConfig.dockContainerSpawnBottomYOffset() + size.halfHeight();

        /*
         * Phase 17.9.23 uses the seven offsets measured in-game against the
         * collaborator Container Dock. Offsets are expressed from the Hut /
         * container center using the player's convention:
         *
         *   localX: right side is positive
         *   localZ: the direction reported by
         *           /colonylogistics minecolonies resolve <dock> is positive
         *
         * The old 17.9.22 extra four-block front shift is removed because the
         * measured values below already include the final desired position.
         * Do not discard these pads with the blueprint-footprint filter: the
         * collaborator Dock intentionally uses seven coarse-dirt staging pads,
         * and correctly placing those seven pads is the target for this build.
         */
        List<LocalContainerPad> pads = new ArrayList<>();
        int[][] markerPads = {
                {7, 2}, {11, 2}, {15, 2},
                {3, -8}, {7, -8}, {11, -8}, {15, -8}
        };
        for (int[] pad : markerPads) {
            pads.add(new LocalContainerPad(pad[0], coreY, pad[1]));
        }
        for (LocalContainerPad pad : pads) {
            addIndoorOffset(candidates, dockPos, dockForward, pad.localX(), pad.localY(), pad.localZ());
        }
    }

    private boolean isInsideProductionDockFootprint(int localXOffset, int localZOffset, ContainerSize size, int buildingLevel) {
        // Phase 17.9.19 collaborator scans use a 21x22 Container Dock footprint
        // and place the Hut anchor at local (2,1,8). Keep pads inside this
        // scanned buildable interior rather than the old 19x19 mock-up.
        int dimensionX = 21;
        int dimensionZ = 22;
        int anchorX = 2;
        int anchorZ = 8;
        int coreLocalX = anchorX + localXOffset;
        int coreLocalZ = anchorZ + localZOffset;
        int minX = coreLocalX - size.halfDepth();
        int maxX = coreLocalX + size.halfDepth();
        int minZ = coreLocalZ - size.halfWidth();
        int maxZ = coreLocalZ + size.halfWidth();

        // Keep contract spawn pads inside the buildable interior, not on or
        // beyond the structural border where canBeReplaced() can see open air.
        return minX >= 1 && maxX <= dimensionX - 2 && minZ >= 1 && maxZ <= dimensionZ - 2;
    }

    private record LocalContainerPad(int localX, int localY, int localZ) {
    }

    private void addIndoorOffset(Set<BlockPos> candidates, BlockPos dockPos, Direction dockForward, int localX, int localY, int localZ) {
        candidates.add(offsetLocal(dockPos, localX, localY, localZ, dockForward));
    }

    private void addApronGridCandidates(Set<BlockPos> candidates, BlockPos dockPos, ContainerSize size, Direction dockForward) {
        if (!ColonyLogisticsConfig.dockContainerSpawnApronGridEnabled()) {
            return;
        }

        int coreY = ColonyLogisticsConfig.dockContainerSpawnBottomYOffset() + size.halfHeight();
        int startX = ColonyLogisticsConfig.dockContainerSpawnApronStartX();
        int startZ = ColonyLogisticsConfig.dockContainerSpawnApronStartZ();
        int columns = ColonyLogisticsConfig.dockContainerSpawnApronColumns();
        int rows = ColonyLogisticsConfig.dockContainerSpawnApronRows();
        int spacingX = effectiveApronSpacingX(size);
        int spacingZ = effectiveApronSpacingZ(size);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                candidates.add(offsetLocal(
                        dockPos,
                        startX + column * spacingX,
                        coreY,
                        startZ + row * spacingZ,
                        dockForward
                ));
            }
        }
    }

    private BlockPos offsetLocal(BlockPos origin, int localX, int localY, int localZ, Direction dockForward) {
        Direction forward = horizontal(dockForward);
        // Structurize/MineColonies places the collaborator scan so that
        // blueprint-local +X is clockwise from the resolved cargo-forward axis.
        // Using counter-clockwise mirrored the pads outside the Dock in-game.
        Direction right = forward.getClockWise();
        return origin.offset(
                right.getStepX() * localX + forward.getStepX() * localZ,
                localY,
                right.getStepZ() * localX + forward.getStepZ() * localZ
        ).immutable();
    }

    private static Direction horizontal(Direction direction) {
        if (direction == null) {
            return Direction.SOUTH;
        }
        return switch (direction) {
            case NORTH, SOUTH, EAST, WEST -> direction;
            default -> Direction.SOUTH;
        };
    }

    private int effectiveApronSpacingX(ContainerSize size) {
        return Math.max(
                ColonyLogisticsConfig.dockContainerSpawnApronSpacingX(),
                size.depth() + ColonyLogisticsConfig.dockContainerSpawnHorizontalGap()
        );
    }

    private int effectiveApronSpacingZ(ContainerSize size) {
        return Math.max(
                ColonyLogisticsConfig.dockContainerSpawnApronSpacingZ(),
                size.width() + ColonyLogisticsConfig.dockContainerSpawnHorizontalGap()
        );
    }

    private int sideCoreOffset(int dockHalfExtent, int containerHalfExtent, int gap) {
        return Math.max(0, dockHalfExtent) + Math.max(0, containerHalfExtent) + Math.max(0, gap) + 1;
    }

    private void addHorizontalRing(Set<BlockPos> candidates, BlockPos dockPos, int y, int x, int z) {
        candidates.add(dockPos.offset(x, y, 0).immutable());
        candidates.add(dockPos.offset(-x, y, 0).immutable());
        candidates.add(dockPos.offset(0, y, z).immutable());
        candidates.add(dockPos.offset(0, y, -z).immutable());
        candidates.add(dockPos.offset(x, y, z).immutable());
        candidates.add(dockPos.offset(x, y, -z).immutable());
        candidates.add(dockPos.offset(-x, y, z).immutable());
        candidates.add(dockPos.offset(-x, y, -z).immutable());
    }

    public double deliveryDistance(ServerLevel level, BlockPos dockPos, BlockPos containerPos) {
        Vec3 dockCenter = Vec3.atCenterOf(dockPos);
        Vec3 containerCenter = Vec3.atCenterOf(containerPos);
        double distanceSq = DeliveryRangeResolvers.current().distanceSquared(level, dockCenter, containerCenter);
        return Math.sqrt(Math.max(0.0D, distanceSq));
    }

    public boolean isWithinDeliveryRange(ServerLevel level, BlockPos dockPos, BlockPos containerPos, double radius) {
        return deliveryDistance(level, dockPos, containerPos) <= radius;
    }

    public static Component spawnResultMessage(SpawnResult result) {
        return Component.translatable("message.colonylogistics.dock.spawn_result." + result.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static Component deliveryResultMessage(DeliveryResult result) {
        return Component.translatable("message.colonylogistics.dock.delivery_result." + result.name().toLowerCase(java.util.Locale.ROOT));
    }

    public record ContainerDeliveryAnalysis(
            boolean deliverable,
            String deliveryStatus,
            double distance,
            String contractStatus,
            String cargoName,
            String expectedContainerText,
            String issueHint
    ) {}


    public enum SpawnResult { SUCCESS, INVALID_DOCK, NO_COLONY, INVALID_CONTRACT, INVALID_STATUS, WRONG_COLONY, WRONG_DOCK, NOT_CONTAINER_JOB, NOT_UNLOCKED, LIMIT_REACHED, NO_SPACE, PLACEMENT_FAILED, EXPORT_DISABLED }
    public enum DeliveryResult { SUCCESS, REMOVED_NOTHING, INVALID_DOCK, NO_COLONY, INVALID_CONTAINER, INVALID_CONTRACT, INVALID_STATUS, WRONG_PLAYER, WRONG_COLONY, WRONG_DOCK, NOT_SEALED, NOT_CONTAINER_JOB, SIZE_MISMATCH, WEIGHT_MISMATCH, EXPIRED, TOO_FAR, IMPORT_DISABLED, REWARD_UNAVAILABLE }
}
