package jp.colonylogistics.contract;

import jp.colonylogistics.config.ColonyLogisticsConfig;
import jp.colonylogistics.container.ContainerRequirement;
import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.ContainerWeightClass;
import jp.colonylogistics.freight.FreightDifficulty;
import jp.colonylogistics.freight.FreightJobSpec;
import jp.colonylogistics.freight.VirtualCargo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ContractNbt {
    public static CompoundTag save(LogisticsContract contract) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", contract.id());
        tag.putString("Type", contract.type().name());
        tag.putString("Status", contract.status().name());
        tag.putInt("OriginColonyId", contract.originColonyId());
        tag.putInt("DestinationColonyId", contract.destinationColonyId());
        contract.originDockPos().ifPresent(pos -> tag.putLong("OriginDockPos", pos.asLong()));
        contract.destinationDockPos().ifPresent(pos -> tag.putLong("DestinationDockPos", pos.asLong()));
        contract.assignedPlayer().ifPresent(uuid -> tag.putUUID("AssignedPlayer", uuid));
        contract.freightSpec().ifPresent(spec -> tag.put("FreightSpec", saveFreightSpec(spec)));
        tag.put("Reward", saveReward(contract.reward()));
        tag.putLong("CreatedGameTime", contract.createdGameTime());
        tag.putLong("ExpiresGameTime", contract.expiresGameTime());
        tag.putInt("RequiredContainerCount", contract.requiredContainerCount());
        tag.putInt("SpawnedContainerCount", contract.spawnedContainerCount());
        tag.putInt("DeliveredContainerCount", contract.deliveredContainerCount());
        return tag;
    }

    public static LogisticsContract load(CompoundTag tag) {
        return new LogisticsContract(
                readUuidOrZero(tag, "Id"),
                safeEnum(ContractType.class, tag.getString("Type"), ContractType.GENERATED_FREIGHT),
                safeEnum(ContractStatus.class, tag.getString("Status"), ContractStatus.OPEN),
                tag.getInt("OriginColonyId"),
                tag.getInt("DestinationColonyId"),
                tag.contains("OriginDockPos") ? Optional.of(BlockPos.of(tag.getLong("OriginDockPos"))) : Optional.empty(),
                tag.contains("DestinationDockPos") ? Optional.of(BlockPos.of(tag.getLong("DestinationDockPos"))) : Optional.empty(),
                tag.hasUUID("AssignedPlayer") ? Optional.of(tag.getUUID("AssignedPlayer")) : Optional.empty(),
                tag.contains("FreightSpec") ? Optional.of(loadFreightSpec(tag.getCompound("FreightSpec"))) : Optional.empty(),
                tag.contains("Reward") ? loadReward(tag.getCompound("Reward")) : new RewardSpec(ColonyLogisticsConfig.defaultCurrencyItemId(), 0),
                tag.getLong("CreatedGameTime"),
                tag.getLong("ExpiresGameTime"),
                tag.contains("RequiredContainerCount") ? tag.getInt("RequiredContainerCount") : 0,
                tag.contains("SpawnedContainerCount") ? tag.getInt("SpawnedContainerCount") : 0,
                tag.contains("DeliveredContainerCount") ? tag.getInt("DeliveredContainerCount") : 0
        );
    }

    private static CompoundTag saveReward(RewardSpec reward) {
        CompoundTag tag = new CompoundTag();
        tag.putString("CurrencyItemId", reward.currencyItemId().toString());
        tag.putInt("CurrencyAmount", reward.currencyAmount());
        return tag;
    }

    private static RewardSpec loadReward(CompoundTag tag) {
        ResourceLocation item = ResourceLocation.tryParse(tag.getString("CurrencyItemId"));
        int amount = tag.getInt("CurrencyAmount");
        int legacyValue = ColonyLogisticsConfig.legacyCurrencyBaseValue(item);
        if (item == null || legacyValue > 0) {
            item = ColonyLogisticsConfig.defaultCurrencyItemId();
            if (legacyValue > 1) {
                long converted = (long) Math.max(0, amount) * legacyValue;
                amount = converted > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) converted;
            }
        }
        return new RewardSpec(item, amount);
    }

    private static CompoundTag saveFreightSpec(FreightJobSpec spec) {
        CompoundTag tag = new CompoundTag();
        tag.putString("DeliveryUnitType", spec.deliveryUnitType().name());
        tag.put("ContainerRequirement", saveContainerRequirement(spec.containerRequirement()));
        tag.putString("Difficulty", spec.difficulty().name());
        tag.putInt("RequiredCarrierLevel", spec.requiredCarrierLevel());
        tag.putLong("PickupDeadline", spec.pickupDeadline());
        tag.putLong("DeliveryDeadline", spec.deliveryDeadline());
        tag.putBoolean("AllowLateDelivery", spec.allowLateDelivery());

        ListTag cargoList = new ListTag();
        for (VirtualCargo cargo : spec.cargo()) {
            CompoundTag cargoTag = new CompoundTag();
            cargoTag.putString("CargoId", cargo.cargoId().toString());
            cargoTag.putInt("Amount", cargo.amount());
            cargoTag.putInt("Volume", cargo.volume());
            cargoTag.putInt("GameplayWeight", cargo.gameplayWeight());
            cargoTag.putInt("Value", cargo.value());
            cargoTag.putInt("Fragility", cargo.fragility());
            cargoList.add(cargoTag);
        }
        tag.put("Cargo", cargoList);
        return tag;
    }

    private static FreightJobSpec loadFreightSpec(CompoundTag tag) {
        List<VirtualCargo> cargo = new ArrayList<>();
        ListTag cargoList = tag.getList("Cargo", Tag.TAG_COMPOUND);
        for (int i = 0; i < cargoList.size(); i++) {
            CompoundTag cargoTag = cargoList.getCompound(i);
            ResourceLocation cargoId = ResourceLocation.tryParse(cargoTag.getString("CargoId"));
            if (cargoId == null) cargoId = ResourceLocation.fromNamespaceAndPath("colonylogistics", "unknown");
            cargo.add(new VirtualCargo(
                    cargoId,
                    Math.max(1, cargoTag.getInt("Amount")),
                    cargoTag.getInt("Volume"),
                    cargoTag.getInt("GameplayWeight"),
                    cargoTag.getInt("Value"),
                    cargoTag.getInt("Fragility")
            ));
        }
        return new FreightJobSpec(
                safeEnum(DeliveryUnitType.class, tag.getString("DeliveryUnitType"), DeliveryUnitType.INVENTORY_ITEM),
                cargo,
                tag.contains("ContainerRequirement") ? loadContainerRequirement(tag.getCompound("ContainerRequirement")) : new ContainerRequirement(ContainerSize.NONE, 0, 0, ContainerWeightClass.EMPTY),
                safeEnum(FreightDifficulty.class, tag.getString("Difficulty"), FreightDifficulty.EASY),
                tag.getInt("RequiredCarrierLevel"),
                tag.getLong("PickupDeadline"),
                tag.getLong("DeliveryDeadline"),
                tag.getBoolean("AllowLateDelivery")
        );
    }

    private static CompoundTag saveContainerRequirement(ContainerRequirement requirement) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Size", requirement.size().name());
        tag.putInt("RequiredVolume", requirement.requiredVolume());
        tag.putInt("CargoGameplayWeight", requirement.cargoGameplayWeight());
        tag.putString("WeightClass", requirement.weightClass().name());
        return tag;
    }

    private static ContainerRequirement loadContainerRequirement(CompoundTag tag) {
        return new ContainerRequirement(
                safeEnum(ContainerSize.class, tag.getString("Size"), ContainerSize.NONE),
                tag.getInt("RequiredVolume"),
                tag.getInt("CargoGameplayWeight"),
                safeEnum(ContainerWeightClass.class, tag.getString("WeightClass"), ContainerWeightClass.EMPTY)
        );
    }

    private static UUID readUuidOrZero(CompoundTag tag, String key) {
        return tag.hasUUID(key) ? tag.getUUID(key) : new UUID(0L, 0L);
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private ContractNbt() {}
}
