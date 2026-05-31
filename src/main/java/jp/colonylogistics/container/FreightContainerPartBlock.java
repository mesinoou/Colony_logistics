package jp.colonylogistics.container;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;

public class FreightContainerPartBlock extends Block {
    public static final MapCodec<FreightContainerPartBlock> CODEC = simpleCodec(FreightContainerPartBlock::new);
    public static final EnumProperty<ContainerWeightClass> WEIGHT_CLASS = EnumProperty.create("weight_class", ContainerWeightClass.class);

    public FreightContainerPartBlock(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion().strength(50.0f, 3600000.0f).pushReaction(PushReaction.NORMAL));
        // Keep a positive destroy speed so Create / Create Aeronautics can treat
        // runtime containers as movable/assemblable blocks. Player mining is still
        // blocked by getDestroyProgress returning 0 and playerDestroy doing no
        // vanilla destruction side effects.
        registerDefaultState(stateDefinition.any().setValue(WEIGHT_CLASS, ContainerWeightClass.MEDIUM));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WEIGHT_CLASS);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity, net.minecraft.world.item.ItemStack tool) {
        // Runtime container blocks are removed only by Colony Logistics service code
        // such as delivery/cancel cleanup.  They must not be mineable by
        // ordinary multiplayer interaction. The block itself intentionally keeps
        // positive hardness so Create / Create Aeronautics assembly can include it.
    }
}
