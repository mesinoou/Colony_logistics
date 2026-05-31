package jp.colonylogistics.freight;

import jp.colonylogistics.container.ContainerRequirement;
import jp.colonylogistics.contract.DeliveryUnitType;

import java.util.List;

public record FreightJobSpec(
        DeliveryUnitType deliveryUnitType,
        List<VirtualCargo> cargo,
        ContainerRequirement containerRequirement,
        FreightDifficulty difficulty,
        int requiredCarrierLevel,
        long pickupDeadline,
        long deliveryDeadline,
        boolean allowLateDelivery
) {
    public int totalGameplayWeight() {
        return cargo.stream().mapToInt(c -> c.gameplayWeight() * c.amount()).sum();
    }

    public int totalVolume() {
        return cargo.stream().mapToInt(c -> c.volume() * c.amount()).sum();
    }

    public int totalValue() {
        return cargo.stream().mapToInt(c -> c.value() * c.amount()).sum();
    }
}
