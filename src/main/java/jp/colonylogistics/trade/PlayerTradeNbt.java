package jp.colonylogistics.trade;

import jp.colonylogistics.contract.ContractStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.UUID;

public final class PlayerTradeNbt {
    public static CompoundTag save(PlayerTradeContract contract, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", contract.id());
        tag.putString("Status", contract.status().name());
        tag.putUUID("CreatorPlayer", contract.creatorPlayer());
        contract.deliveredBy().ifPresent(uuid -> tag.putUUID("DeliveredBy", uuid));
        tag.putInt("ColonyId", contract.colonyId());
        tag.putLong("TerminalPos", contract.terminalPos().asLong());
        tag.put("RequestedStack", contract.requestedTemplateCopy().save(registries));
        tag.put("EscrowedReward", contract.escrowedRewardCopy().save(registries));
        tag.putString("MatchMode", contract.matchMode().name());

        // Keep legacy fields as a human-readable fallback and for migration/debugging.
        tag.putString("RequestedItemId", contract.requestedItemId().toString());
        tag.putInt("RequestedCount", contract.requestedCount());
        tag.putString("RewardItemId", contract.rewardItemId().toString());
        tag.putInt("RewardCount", contract.rewardCount());
        tag.putLong("CreatedGameTime", contract.createdGameTime());
        tag.putLong("ExpiresGameTime", contract.expiresGameTime());
        return tag;
    }

    public static PlayerTradeContract load(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack requested = loadStack(tag, "RequestedStack", "RequestedItemId", "RequestedCount", registries);
        ItemStack reward = loadStack(tag, "EscrowedReward", "RewardItemId", "RewardCount", registries);

        if (requested.isEmpty()) requested = new ItemStack(Items.BARRIER, 1);
        if (reward.isEmpty()) reward = new ItemStack(Items.BARRIER, 1);

        return new PlayerTradeContract(
                tag.hasUUID("Id") ? tag.getUUID("Id") : new UUID(0L, 0L),
                safeEnum(ContractStatus.class, tag.getString("Status"), ContractStatus.OPEN),
                tag.hasUUID("CreatorPlayer") ? tag.getUUID("CreatorPlayer") : new UUID(0L, 0L),
                tag.hasUUID("DeliveredBy") ? Optional.of(tag.getUUID("DeliveredBy")) : Optional.empty(),
                tag.getInt("ColonyId"),
                BlockPos.of(tag.getLong("TerminalPos")),
                requested,
                reward,
                ItemMatchMode.fromName(tag.getString("MatchMode")),
                tag.getLong("CreatedGameTime"),
                tag.getLong("ExpiresGameTime")
        );
    }

    private static ItemStack loadStack(CompoundTag owner, String stackKey, String idKey, String countKey, HolderLookup.Provider registries) {
        if (owner.contains(stackKey, Tag.TAG_COMPOUND)) {
            return ItemStack.parseOptional(registries, owner.getCompound(stackKey));
        }

        ResourceLocation id = ResourceLocation.tryParse(owner.getString(idKey));
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item, Math.max(1, owner.getInt(countKey)));
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private PlayerTradeNbt() {}
}
