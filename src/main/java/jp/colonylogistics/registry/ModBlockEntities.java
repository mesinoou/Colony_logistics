package jp.colonylogistics.registry;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ColonyLogistics.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FreightContainerCoreBlockEntity>> FREIGHT_CONTAINER_CORE =
            BLOCK_ENTITIES.register("freight_container_core", () -> BlockEntityType.Builder.of(
                    FreightContainerCoreBlockEntity::new,
                    ModBlocks.FREIGHT_CONTAINER_CORE.get()
            ).build(null));


    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    private ModBlockEntities() {}
}
