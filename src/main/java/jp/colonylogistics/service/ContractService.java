package jp.colonylogistics.service;

import jp.colonylogistics.chat.SafeSystemChat;
import jp.colonylogistics.container.ContainerManifest;
import jp.colonylogistics.container.ContainerMultiblockBuilder;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.ContractType;
import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.currency.CurrencyService;
import jp.colonylogistics.debug.MultiplayerDebugLog;
import jp.colonylogistics.contract.RewardSpec;
import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.freight.FreightJobSpec;
import jp.colonylogistics.item.FreightParcelData;
import jp.colonylogistics.item.FreightParcelItem;
import jp.colonylogistics.profile.CarrierProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ContractService {
    private static final int MAX_ACCEPTED_JOBS_PER_PLAYER = 3;
    private final ContainerMultiblockBuilder containerBuilder = new ContainerMultiblockBuilder();

    public Optional<ItemStack> acceptInventoryFreight(ServerPlayer player, UUID contractId) {
        ServerLevel level = player.serverLevel();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<LogisticsContract> optionalContract = data.contract(contractId);
        if (optionalContract.isEmpty()) {
            MultiplayerDebugLog.contractRejected(player, "accept_inventory", contractId.toString(), "UNKNOWN_CONTRACT", "");
            SafeSystemChat.send(player, Component.literal("Unknown freight contract."));
            return Optional.empty();
        }

        LogisticsContract contract = optionalContract.get();
        if (contract.status() != ContractStatus.OPEN || contract.freightSpec().isEmpty()) {
            MultiplayerDebugLog.contractAction(player, "accept_inventory", contract, "REJECTED_NOT_OPEN", "");
            SafeSystemChat.send(player, Component.literal("That freight contract is no longer open."));
            return Optional.empty();
        }

        FreightJobSpec spec = contract.freightSpec().get();
        if (spec.deliveryUnitType() != DeliveryUnitType.INVENTORY_ITEM) {
            MultiplayerDebugLog.contractAction(player, "accept_inventory", contract, "REJECTED_WRONG_TYPE", "unit=" + spec.deliveryUnitType());
            SafeSystemChat.send(player, Component.literal("This contract requires a container dock, not a parcel pickup."));
            return Optional.empty();
        }

        CarrierProfile profile = data.carrierProfile(player.getUUID());
        if (profile.carrierLevel() < spec.requiredCarrierLevel()) {
            MultiplayerDebugLog.contractAction(player, "accept_inventory", contract, "REJECTED_CARRIER_LEVEL", "current=" + profile.carrierLevel() + " required=" + spec.requiredCarrierLevel());
            SafeSystemChat.send(player, Component.literal("Carrier level too low. Required: " + spec.requiredCarrierLevel()));
            return Optional.empty();
        }

        if (data.acceptedJobsForPlayer(player.getUUID()) >= MAX_ACCEPTED_JOBS_PER_PLAYER) {
            MultiplayerDebugLog.contractAction(player, "accept_inventory", contract, "REJECTED_PLAYER_JOB_CAP", "accepted=" + data.acceptedJobsForPlayer(player.getUUID()) + " max=" + MAX_ACCEPTED_JOBS_PER_PLAYER);
            SafeSystemChat.send(player, Component.literal("You already have the maximum number of accepted freight jobs."));
            return Optional.empty();
        }

        long now = level.getGameTime();
        if (ColonyLogisticsConfig.pickupWindowTicks() > 0 && spec.pickupDeadline() > 0 && now > spec.pickupDeadline()) {
            data.replaceContract(contract.withStatus(ContractStatus.EXPIRED));
            MultiplayerDebugLog.contractAction(player, "accept_inventory", contract, "REJECTED_EXPIRED", "now=" + now + " pickupDeadline=" + spec.pickupDeadline());
            SafeSystemChat.send(player, Component.literal("That freight contract expired before pickup."));
            return Optional.empty();
        }

        LogisticsContract accepted = contract.assign(player.getUUID()).withStatus(ContractStatus.PICKED_UP);
        data.replaceContract(accepted);
        MultiplayerDebugLog.contractAction(player, "accept_inventory", accepted, "SUCCESS", "");

        ResourceLocation cargoId = accepted.freightSpec()
                .flatMap(job -> job.cargo().stream().findFirst())
                .map(cargo -> cargo.cargoId())
                .orElse(ResourceLocation.fromNamespaceAndPath("colonylogistics", "unknown"));

        FreightParcelData parcelData = new FreightParcelData(
                accepted.id(),
                player.getUUID(),
                accepted.originColonyId(),
                accepted.destinationColonyId(),
                accepted.originDockPos().orElse(player.blockPosition()),
                accepted.destinationDockPos().orElse(player.blockPosition()),
                cargoId,
                level.getGameTime()
        );
        return Optional.of(FreightParcelItem.create(parcelData));
    }


    public boolean acceptContainerFreight(ServerPlayer player, UUID contractId) {
        ServerLevel level = player.serverLevel();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<LogisticsContract> optionalContract = data.contract(contractId);
        if (optionalContract.isEmpty()) {
            MultiplayerDebugLog.contractRejected(player, "accept_container", contractId.toString(), "UNKNOWN_CONTRACT", "");
            SafeSystemChat.send(player, Component.literal("Unknown freight contract."));
            return false;
        }

        LogisticsContract contract = optionalContract.get();
        if (contract.status() != ContractStatus.OPEN || contract.freightSpec().isEmpty()) {
            MultiplayerDebugLog.contractAction(player, "accept_container", contract, "REJECTED_NOT_OPEN", "");
            SafeSystemChat.send(player, Component.literal("That freight contract is no longer open."));
            return false;
        }

        FreightJobSpec spec = contract.freightSpec().get();
        if (spec.deliveryUnitType() != DeliveryUnitType.CONTAINER_MULTIBLOCK) {
            MultiplayerDebugLog.contractAction(player, "accept_container", contract, "REJECTED_WRONG_TYPE", "unit=" + spec.deliveryUnitType());
            SafeSystemChat.send(player, Component.literal("This contract is not a container freight job."));
            return false;
        }

        CarrierProfile profile = data.carrierProfile(player.getUUID());
        if (profile.carrierLevel() < spec.requiredCarrierLevel()) {
            MultiplayerDebugLog.contractAction(player, "accept_container", contract, "REJECTED_CARRIER_LEVEL", "current=" + profile.carrierLevel() + " required=" + spec.requiredCarrierLevel());
            SafeSystemChat.send(player, Component.literal("Carrier level too low. Required: " + spec.requiredCarrierLevel()));
            return false;
        }

        if (data.acceptedJobsForPlayer(player.getUUID()) >= MAX_ACCEPTED_JOBS_PER_PLAYER) {
            MultiplayerDebugLog.contractAction(player, "accept_container", contract, "REJECTED_PLAYER_JOB_CAP", "accepted=" + data.acceptedJobsForPlayer(player.getUUID()) + " max=" + MAX_ACCEPTED_JOBS_PER_PLAYER);
            SafeSystemChat.send(player, Component.literal("You already have the maximum number of accepted freight jobs."));
            return false;
        }

        long now = level.getGameTime();
        if (ColonyLogisticsConfig.pickupWindowTicks() > 0 && spec.pickupDeadline() > 0 && now > spec.pickupDeadline()) {
            data.replaceContract(contract.withStatus(ContractStatus.EXPIRED));
            MultiplayerDebugLog.contractAction(player, "accept_container", contract, "REJECTED_EXPIRED", "now=" + now + " pickupDeadline=" + spec.pickupDeadline());
            SafeSystemChat.send(player, Component.literal("That freight contract expired before pickup."));
            return false;
        }

        LogisticsContract accepted = contract.assign(player.getUUID());
        data.replaceContract(accepted);
        data.setDirty();
        MultiplayerDebugLog.contractAction(player, "accept_container", accepted, "SUCCESS", "");
        SafeSystemChat.send(player, Component.literal("Accepted container freight job. Use a Container Dock to spawn the sealed container."));
        return true;
    }



    public boolean cancelAcceptedFreight(ServerPlayer player, UUID contractId) {
        ServerLevel level = player.serverLevel();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<LogisticsContract> optionalContract = data.contract(contractId);
        if (optionalContract.isEmpty()) {
            MultiplayerDebugLog.contractRejected(player, "cancel_freight", contractId.toString(), "UNKNOWN_CONTRACT", "");
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_unknown"));
            return false;
        }

        LogisticsContract contract = optionalContract.get();
        if (contract.type() != ContractType.GENERATED_FREIGHT || contract.freightSpec().isEmpty()) {
            MultiplayerDebugLog.contractAction(player, "cancel_freight", contract, "REJECTED_INVALID_TYPE", "");
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_invalid"));
            return false;
        }
        if (!ColonyLogisticsConfig.generatedJobsAllowCarrierCancel()) {
            MultiplayerDebugLog.contractAction(player, "cancel_freight", contract, "REJECTED_DISABLED", "");
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_disabled"));
            return false;
        }
        if (contract.assignedPlayer().filter(player.getUUID()::equals).isEmpty()) {
            MultiplayerDebugLog.contractAction(player, "cancel_freight", contract, "REJECTED_NOT_ASSIGNED", "");
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_not_assigned"));
            return false;
        }
        if (!isCarrierCancelableStatus(contract.status())) {
            MultiplayerDebugLog.contractAction(player, "cancel_freight", contract, "REJECTED_STATUS", "status=" + contract.status());
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_not_active"));
            return false;
        }

        FreightJobSpec spec = contract.freightSpec().get();
        boolean containerJob = spec.deliveryUnitType() == DeliveryUnitType.CONTAINER_MULTIBLOCK;
        if (containerJob && contract.spawnedContainerCount() > 0 && !ColonyLogisticsConfig.generatedJobsAllowCancelAfterContainerSpawn()) {
            MultiplayerDebugLog.contractAction(player, "cancel_freight", contract, "REJECTED_CONTAINER_SPAWNED", "spawned=" + contract.spawnedContainerCount());
            SafeSystemChat.send(player, Component.translatable("message.colonylogistics.freight.cancel_spawned_disabled"));
            return false;
        }

        int removedParcels = spec.deliveryUnitType() == DeliveryUnitType.INVENTORY_ITEM
                ? removeAssignedParcels(player, contract.id())
                : 0;
        int removedContainers = containerJob ? removeNearbyContainersForContract(level, contract) : 0;

        if (containerJob && contract.spawnedContainerCount() > 0) {
            data.colonyState(contract.originColonyId()).decrementActiveContainerJobs();
        }
        if (ColonyLogisticsConfig.generatedJobsCancelCountsAsFailed()) {
            data.carrierProfile(player.getUUID()).recordFailedJob();
        }

        LogisticsContract cancelled = contract.withStatus(ContractStatus.CANCELLED);
        data.replaceContract(cancelled);
        data.setDirty();
        MultiplayerDebugLog.contractAction(player, "cancel_freight", cancelled, "SUCCESS", "removedParcels=" + removedParcels + " removedContainers=" + removedContainers);
        SafeSystemChat.send(player, Component.translatable(
                "message.colonylogistics.freight.cancelled",
                contract.id().toString(),
                removedParcels,
                removedContainers
        ));
        return true;
    }

    private boolean isCarrierCancelableStatus(ContractStatus status) {
        return status == ContractStatus.ACCEPTED || status == ContractStatus.PICKED_UP || status == ContractStatus.DELIVERED;
    }

    private int removeAssignedParcels(ServerPlayer player, UUID contractId) {
        int removed = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Optional<FreightParcelData> parcel = FreightParcelItem.read(stack);
            if (parcel.isPresent() && parcel.get().contractId().equals(contractId)) {
                stack.shrink(1);
                removed++;
            }
        }
        return removed;
    }

    private int removeNearbyContainersForContract(ServerLevel level, LogisticsContract contract) {
        Set<BlockPos> centers = new LinkedHashSet<>();
        contract.originDockPos().ifPresent(centers::add);
        contract.destinationDockPos().ifPresent(centers::add);
        if (centers.isEmpty()) {
            return 0;
        }

        int removed = 0;
        int radius = Math.max(8, (int) Math.ceil(ContainerDockService.containerRecognitionRadius()) + 8);
        Set<BlockPos> removedCores = new LinkedHashSet<>();
        for (BlockPos center : centers) {
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
                if (removedCores.contains(pos.immutable())) {
                    continue;
                }
                if (!(level.getBlockEntity(pos) instanceof FreightContainerCoreBlockEntity core)) {
                    continue;
                }
                Optional<ContainerManifest> manifest = core.manifest();
                if (manifest.isPresent() && manifest.get().contractId().equals(contract.id())) {
                    if (containerBuilder.remove(level, core.getBlockPos(), manifest.get())) {
                        removed++;
                        removedCores.add(core.getBlockPos().immutable());
                    }
                }
            }
        }
        return removed;
    }


    public boolean completeInventoryFreight(ServerPlayer player, ItemStack parcelStack) {
        return completeInventoryFreight(player, parcelStack, Optional.empty());
    }

    public boolean completeInventoryFreightAt(ServerPlayer player, ItemStack parcelStack, net.minecraft.core.BlockPos clickedPos) {
        return completeInventoryFreight(player, parcelStack, Optional.of(clickedPos.immutable()));
    }

    private boolean completeInventoryFreight(ServerPlayer player, ItemStack parcelStack, Optional<net.minecraft.core.BlockPos> clickedPos) {
        Optional<FreightParcelData> parcel = FreightParcelItem.read(parcelStack);
        if (parcel.isEmpty() || !parcel.get().assignedPlayer().equals(player.getUUID())) {
            MultiplayerDebugLog.contractRejected(player, "complete_inventory", parcel.map(p -> p.contractId().toString()).orElse("missing_parcel_data"), "PARCEL_NOT_ASSIGNED", "");
            SafeSystemChat.send(player, Component.literal("This parcel is not assigned to you."));
            return false;
        }
        ServerLevel level = player.serverLevel();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        Optional<LogisticsContract> optionalContract = data.contract(parcel.get().contractId());
        if (optionalContract.isEmpty()) {
            MultiplayerDebugLog.contractRejected(player, "complete_inventory", parcel.get().contractId().toString(), "UNKNOWN_CONTRACT", "");
            SafeSystemChat.send(player, Component.literal("The parcel contract no longer exists."));
            return false;
        }
        LogisticsContract contract = optionalContract.get();
        if (contract.status() != ContractStatus.PICKED_UP || contract.assignedPlayer().filter(player.getUUID()::equals).isEmpty()) {
            MultiplayerDebugLog.contractAction(player, "complete_inventory", contract, "REJECTED_NOT_READY", "");
            SafeSystemChat.send(player, Component.literal("The parcel contract is not ready for delivery."));
            return false;
        }
        if (contract.freightSpec().isEmpty() || contract.freightSpec().get().deliveryUnitType() != DeliveryUnitType.INVENTORY_ITEM) {
            MultiplayerDebugLog.contractAction(player, "complete_inventory", contract, "REJECTED_WRONG_TYPE", "");
            SafeSystemChat.send(player, Component.literal("This is not an inventory freight contract."));
            return false;
        }
        if (contract.destinationColonyId() != parcel.get().destinationColonyId()) {
            MultiplayerDebugLog.contractAction(player, "complete_inventory", contract, "REJECTED_DESTINATION_MISMATCH", "parcelDest=" + parcel.get().destinationColonyId());
            SafeSystemChat.send(player, Component.literal("Parcel destination data does not match the contract."));
            return false;
        }
        if (!clickedPos.map(pos -> isValidInventoryDeliveryTarget(level, contract, parcel.get(), pos)).orElse(true)) {
            MultiplayerDebugLog.contractAction(player, "complete_inventory", contract, "REJECTED_WRONG_TARGET", "clickedPos=" + clickedPos.map(MultiplayerDebugLog::pos).orElse("-"));
            SafeSystemChat.send(player, Component.literal("This parcel must be delivered to the destination Logistics Office or another logistics building in colony " + contract.destinationColonyId() + "."));
            return false;
        }

        long now = level.getGameTime();
        FreightJobSpec spec = contract.freightSpec().get();
        int rewardAmount = contract.reward().currencyAmount();
        if (ColonyLogisticsConfig.inventoryDeliveryWindowTicks() > 0 && spec.deliveryDeadline() > 0 && now > spec.deliveryDeadline()) {
            if (!spec.allowLateDelivery()) {
                data.replaceContract(contract.withStatus(ContractStatus.FAILED));
                data.carrierProfile(player.getUUID()).recordFailedJob();
                data.setDirty();
                MultiplayerDebugLog.contractAction(player, "complete_inventory", contract, "FAILED_EXPIRED", "now=" + now + " deliveryDeadline=" + spec.deliveryDeadline());
                SafeSystemChat.send(player, Component.literal("Delivery deadline missed. Contract failed."));
                return false;
            }
            rewardAmount = ColonyLogisticsConfig.lateAdjustedReward(rewardAmount);
        }

        CurrencyService currencyService = new CurrencyService();
        Optional<RewardSpec> payableReward = currencyService.payableReward(contract.reward(), rewardAmount);
        if (payableReward.isEmpty()) {
            MultiplayerDebugLog.contractAction(player, "complete_inventory", contract, "REJECTED_REWARD_UNAVAILABLE", "reward=" + contract.reward().currencyAmount() + "x" + contract.reward().currencyItemId());
            SafeSystemChat.send(player, Component.translatable(
                    "message.colonylogistics.currency.unavailable",
                    contract.reward().currencyItemId()
            ));
            return false;
        }

        parcelStack.shrink(1);
        LogisticsContract delivered = contract.withStatus(ContractStatus.COMPLETED);
        data.replaceContract(delivered);

        CurrencyService.PaymentResult payment = currencyService.payResolvedToPlayer(player, payableReward.get());
        int paidAmount = payment.paid() ? payment.amount() : 0;
        int cargoAmount = spec.cargo().stream().mapToInt(cargo -> cargo.amount()).sum();
        long estimatedDistance = contract.originDockPos().flatMap(origin -> contract.destinationDockPos().map(origin::distManhattan)).orElse(0);
        data.carrierProfile(player.getUUID()).recordCompletedJob(paidAmount, cargoAmount, estimatedDistance);
        data.setDirty();
        MultiplayerDebugLog.contractAction(player, "complete_inventory", delivered, payment.paid() ? "SUCCESS" : "COMPLETED_PAYMENT_FAILED", "payment=" + payment.displayText());
        SafeSystemChat.send(player, Component.literal("Freight delivered. Reward paid: " + payment.displayText()));
        return true;
    }

    private boolean isValidInventoryDeliveryTarget(ServerLevel level, LogisticsContract contract, FreightParcelData parcel, net.minecraft.core.BlockPos clickedPos) {
        net.minecraft.core.BlockPos destinationPos = contract.destinationDockPos().orElse(parcel.destinationPos());
        if (clickedPos.equals(destinationPos)) {
            return true;
        }

        ResolvedLogisticsBuilding resolved = ResolvedLogisticsBuilding.resolve(level, clickedPos);
        if (resolved.usable() && resolved.colonyId() == contract.destinationColonyId()) {
            return true;
        }

        return LogisticsMarketSavedData.get(level)
                .allColonies()
                .stream()
                .filter(state -> state.colonyId() == contract.destinationColonyId())
                .anyMatch(state -> state.logisticsOfficePos().equals(clickedPos) || state.dockPositions().contains(clickedPos));
    }

}
