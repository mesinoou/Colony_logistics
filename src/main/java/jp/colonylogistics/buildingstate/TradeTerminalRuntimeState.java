package jp.colonylogistics.buildingstate;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public record TradeTerminalRuntimeState(ItemStack requestTemplate, ItemStack escrowInput) {
    public static TradeTerminalRuntimeState empty() {
        return new TradeTerminalRuntimeState(ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public TradeTerminalRuntimeState {
        requestTemplate = requestTemplate == null ? ItemStack.EMPTY : requestTemplate.copy();
        escrowInput = escrowInput == null ? ItemStack.EMPTY : escrowInput.copy();
    }

    public TradeTerminalRuntimeState withRequestTemplate(ItemStack stack) {
        return new TradeTerminalRuntimeState(stack, escrowInput);
    }

    public TradeTerminalRuntimeState withEscrowInput(ItemStack stack) {
        return new TradeTerminalRuntimeState(requestTemplate, stack);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (!requestTemplate.isEmpty()) tag.put("Request", requestTemplate.save(registries));
        if (!escrowInput.isEmpty()) tag.put("Escrow", escrowInput.save(registries));
        return tag;
    }

    public static TradeTerminalRuntimeState load(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack request = tag.contains("Request", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound("Request")) : ItemStack.EMPTY;
        ItemStack escrow = tag.contains("Escrow", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound("Escrow")) : ItemStack.EMPTY;
        return new TradeTerminalRuntimeState(request, escrow);
    }
}
