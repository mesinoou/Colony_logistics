package jp.colonylogistics.container;

import jp.colonylogistics.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class FreightContainerCoreBlockEntity extends BlockEntity {
    private ContainerManifest manifest;
    private boolean invalid;

    public FreightContainerCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FREIGHT_CONTAINER_CORE.get(), pos, blockState);
    }

    public Optional<ContainerManifest> manifest() {
        return Optional.ofNullable(manifest);
    }

    public void setManifest(ContainerManifest manifest) {
        this.manifest = manifest;
        setChanged();
    }

    public boolean invalid() {
        return invalid;
    }

    public void markInvalid() {
        this.invalid = true;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Invalid", invalid);
        if (manifest != null) {
            CompoundTag m = new CompoundTag();
            m.putUUID("ContainerId", manifest.containerId());
            m.putUUID("ContractId", manifest.contractId());
            m.putInt("OriginColonyId", manifest.originColonyId());
            m.putInt("DestinationColonyId", manifest.destinationColonyId());
            m.putUUID("AssignedPlayer", manifest.assignedPlayer());
            m.putLong("OriginDockPos", manifest.originDockPos().asLong());
            m.putLong("DestinationDockPos", manifest.destinationDockPos().asLong());
            m.putString("Size", manifest.size().name());
            m.putString("WeightClass", manifest.weightClass().name());
            m.putInt("GameplayWeight", manifest.gameplayWeight());
            m.putBoolean("Sealed", manifest.sealed());
            m.putLong("CreatedGameTime", manifest.createdGameTime());
            m.putInt("BatchIndex", manifest.batchIndex());
            m.putInt("BatchCount", manifest.batchCount());
            m.putString("Facing", manifest.facing().name());
            tag.put("Manifest", m);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        invalid = tag.getBoolean("Invalid");
        if (tag.contains("Manifest")) {
            CompoundTag m = tag.getCompound("Manifest");
            manifest = new ContainerManifest(
                    readUuidOrZero(m, "ContainerId"),
                    readUuidOrZero(m, "ContractId"),
                    m.getInt("OriginColonyId"),
                    m.getInt("DestinationColonyId"),
                    readUuidOrZero(m, "AssignedPlayer"),
                    BlockPos.of(m.getLong("OriginDockPos")),
                    BlockPos.of(m.getLong("DestinationDockPos")),
                    safeEnum(ContainerSize.class, m.getString("Size"), ContainerSize.SMALL),
                    safeEnum(ContainerWeightClass.class, m.getString("WeightClass"), ContainerWeightClass.MEDIUM),
                    m.getInt("GameplayWeight"),
                    m.getBoolean("Sealed"),
                    m.getLong("CreatedGameTime"),
                    m.contains("BatchIndex") ? m.getInt("BatchIndex") : 1,
                    m.contains("BatchCount") ? m.getInt("BatchCount") : 1,
                    safeDirection(m.contains("Facing") ? m.getString("Facing") : "SOUTH")
            );
        }
    }

    private static UUID readUuidOrZero(CompoundTag tag, String key) {
        return tag.hasUUID(key) ? tag.getUUID(key) : new UUID(0L, 0L);
    }

    private static Direction safeDirection(String name) {
        try {
            Direction direction = Direction.valueOf(name.toUpperCase(Locale.ROOT));
            return switch (direction) {
                case NORTH, SOUTH, EAST, WEST -> direction;
                default -> Direction.SOUTH;
            };
        } catch (IllegalArgumentException ex) {
            return Direction.SOUTH;
        }
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
