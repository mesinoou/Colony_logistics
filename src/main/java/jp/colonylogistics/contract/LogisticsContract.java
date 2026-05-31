package jp.colonylogistics.contract;

import jp.colonylogistics.freight.FreightJobSpec;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;

public record LogisticsContract(
        UUID id,
        ContractType type,
        ContractStatus status,
        int originColonyId,
        int destinationColonyId,
        Optional<BlockPos> originDockPos,
        Optional<BlockPos> destinationDockPos,
        Optional<UUID> assignedPlayer,
        Optional<FreightJobSpec> freightSpec,
        RewardSpec reward,
        long createdGameTime,
        long expiresGameTime,
        int requiredContainerCount,
        int spawnedContainerCount,
        int deliveredContainerCount
) {
    public LogisticsContract {
        requiredContainerCount = Math.max(0, requiredContainerCount);
        spawnedContainerCount = Math.max(0, Math.min(spawnedContainerCount, Math.max(0, requiredContainerCount)));
        deliveredContainerCount = Math.max(0, Math.min(deliveredContainerCount, Math.max(0, spawnedContainerCount)));
    }

    public LogisticsContract withStatus(ContractStatus next) {
        return new LogisticsContract(
                id, type, next, originColonyId, destinationColonyId,
                originDockPos, destinationDockPos, assignedPlayer,
                freightSpec, reward, createdGameTime, expiresGameTime,
                requiredContainerCount, spawnedContainerCount, deliveredContainerCount
        );
    }

    public LogisticsContract assign(UUID playerId) {
        return new LogisticsContract(
                id, type, ContractStatus.ACCEPTED, originColonyId, destinationColonyId,
                originDockPos, destinationDockPos, Optional.of(playerId),
                freightSpec, reward, createdGameTime, expiresGameTime,
                requiredContainerCount, spawnedContainerCount, deliveredContainerCount
        );
    }

    public LogisticsContract withReward(RewardSpec nextReward) {
        return new LogisticsContract(
                id, type, status, originColonyId, destinationColonyId,
                originDockPos, destinationDockPos, assignedPlayer,
                freightSpec, nextReward, createdGameTime, expiresGameTime,
                requiredContainerCount, spawnedContainerCount, deliveredContainerCount
        );
    }

    public int effectiveContainerCount() {
        return Math.max(1, requiredContainerCount);
    }

    public boolean requiresMultipleContainers() {
        return requiredContainerCount > 1;
    }

    public boolean canSpawnMoreContainers() {
        return freightSpec.isPresent()
                && freightSpec.get().containerRequirement().requiresContainer()
                && spawnedContainerCount < effectiveContainerCount();
    }

    public LogisticsContract withSpawnedContainer() {
        int nextSpawned = Math.min(effectiveContainerCount(), spawnedContainerCount + 1);
        return new LogisticsContract(
                id, type, ContractStatus.PICKED_UP, originColonyId, destinationColonyId,
                originDockPos, destinationDockPos, assignedPlayer,
                freightSpec, reward, createdGameTime, expiresGameTime,
                effectiveContainerCount(), nextSpawned, deliveredContainerCount
        );
    }

    public LogisticsContract withDeliveredContainer() {
        int nextDelivered = Math.min(effectiveContainerCount(), deliveredContainerCount + 1);
        ContractStatus nextStatus = nextDelivered >= effectiveContainerCount() ? ContractStatus.COMPLETED : ContractStatus.PICKED_UP;
        return new LogisticsContract(
                id, type, nextStatus, originColonyId, destinationColonyId,
                originDockPos, destinationDockPos, assignedPlayer,
                freightSpec, reward, createdGameTime, expiresGameTime,
                effectiveContainerCount(), spawnedContainerCount, nextDelivered
        );
    }

    public String containerProgressText() {
        if (requiredContainerCount <= 1) {
            return "";
        }
        return deliveredContainerCount + "/" + requiredContainerCount;
    }
}
