package jp.colonylogistics.buildingstate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public record LogisticsBuildingKey(ResourceLocation dimension, BlockPos pos) {
    public static LogisticsBuildingKey of(ServerLevel level, BlockPos pos) {
        return new LogisticsBuildingKey(level.dimension().location(), pos.immutable());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", dimension.toString());
        tag.putLong("Pos", pos.asLong());
        return tag;
    }

    public static LogisticsBuildingKey load(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("Dimension"));
        if (dimension == null) {
            dimension = ResourceLocation.withDefaultNamespace("overworld");
        }
        return new LogisticsBuildingKey(dimension, BlockPos.of(tag.getLong("Pos")));
    }
}
