package jp.colonylogistics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public record FreightParcelData(
        UUID contractId,
        UUID assignedPlayer,
        int originColonyId,
        int destinationColonyId,
        BlockPos originPos,
        BlockPos destinationPos,
        ResourceLocation cargoId,
        long createdGameTime
) {
    private static final String ROOT = "ColonyLogisticsParcel";

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag root = new CompoundTag();
        root.putUUID("ContractId", contractId);
        root.putUUID("AssignedPlayer", assignedPlayer);
        root.putInt("OriginColonyId", originColonyId);
        root.putInt("DestinationColonyId", destinationColonyId);
        root.putLong("OriginPos", originPos.asLong());
        root.putLong("DestinationPos", destinationPos.asLong());
        root.putString("CargoId", cargoId.toString());
        root.putLong("CreatedGameTime", createdGameTime);
        tag.put(ROOT, root);
        return tag;
    }

    public static Optional<FreightParcelData> fromTag(CompoundTag tag) {
        if (!tag.contains(ROOT)) {
            return Optional.empty();
        }
        CompoundTag root = tag.getCompound(ROOT);
        if (!root.hasUUID("ContractId") || !root.hasUUID("AssignedPlayer")) {
            return Optional.empty();
        }
        ResourceLocation cargoId = ResourceLocation.tryParse(root.getString("CargoId"));
        if (cargoId == null) {
            return Optional.empty();
        }
        return Optional.of(new FreightParcelData(
                root.getUUID("ContractId"),
                root.getUUID("AssignedPlayer"),
                root.getInt("OriginColonyId"),
                root.getInt("DestinationColonyId"),
                BlockPos.of(root.getLong("OriginPos")),
                BlockPos.of(root.getLong("DestinationPos")),
                cargoId,
                root.getLong("CreatedGameTime")
        ));
    }
}
