package jp.colonylogistics.item;

import jp.colonylogistics.service.ContractService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Optional;

public class FreightParcelItem extends Item {
    public FreightParcelItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(FreightParcelData data) {
        ItemStack stack = new ItemStack(jp.colonylogistics.registry.ModItems.FREIGHT_PARCEL.get());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data.toTag()));
        return stack;
    }

    public static Optional<FreightParcelData> read(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return FreightParcelData.fromTag(customData.copyTag());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        boolean delivered = new ContractService().completeInventoryFreightAt(player, stack, context.getClickedPos());
        return delivered ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        read(stack).ifPresentOrElse(data -> {
            tooltip.add(Component.translatable("tooltip.colonylogistics.freight_parcel.contract", data.contractId().toString()));
            tooltip.add(Component.translatable("tooltip.colonylogistics.freight_parcel.route", data.originColonyId(), data.destinationColonyId()));
            tooltip.add(Component.translatable("tooltip.colonylogistics.freight_parcel.destination", data.destinationPos().toShortString()));
            tooltip.add(Component.translatable("tooltip.colonylogistics.freight_parcel.cargo", data.cargoId().toString()));
        }, () -> tooltip.add(Component.translatable("tooltip.colonylogistics.freight_parcel.invalid")));
    }
}
