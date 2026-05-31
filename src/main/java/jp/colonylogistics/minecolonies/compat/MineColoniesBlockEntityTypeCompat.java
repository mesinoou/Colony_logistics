package jp.colonylogistics.minecolonies.compat;

import jp.colonylogistics.ColonyLogistics;
import jp.colonylogistics.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Compatibility shim for MineColonies hut blocks on Minecraft 1.21.1.
 *
 * <p>MineColonies uses a shared {@code minecolonies:colonybuilding}
 * BlockEntityType for hut anchors. In 1.21.1 Minecraft validates that a
 * BlockEntityType is allowed for the block state before constructing the
 * BlockEntity. Third-party hut blocks registered through the MineColonies
 * building registry are not automatically present in that vanilla valid-block
 * set, so Build Tool placement can crash with:</p>
 *
 * <pre>
 * Invalid block entity minecolonies:colonybuilding state ..., got Block{colonylogistics:...}
 * </pre>
 *
 * <p>This class adds our MineColonies hut blocks to the valid-block set at
 * common setup time. It is intentionally isolated so it can be removed if
 * MineColonies exposes an official API for this in a later version.</p>
 */
public final class MineColoniesBlockEntityTypeCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger(MineColoniesBlockEntityTypeCompat.class);
    private static final ResourceLocation COLONY_BUILDING_BE = ResourceLocation.fromNamespaceAndPath("minecolonies", "colonybuilding");

    public static void patchColonyBuildingValidBlocks() {
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(COLONY_BUILDING_BE);
        if (type == null) {
            LOGGER.warn("Could not find MineColonies block entity type {}. Hut placement compatibility patch skipped.", COLONY_BUILDING_BE);
            return;
        }

        Set<Block> validBlocks = findValidBlocksSet(type);
        if (validBlocks == null) {
            LOGGER.warn("Could not locate BlockEntityType valid block set for {}. Hut placement compatibility patch skipped.", COLONY_BUILDING_BE);
            return;
        }

        addBlock(validBlocks, ModBlocks.LOGISTICS_OFFICE.get());
        addBlock(validBlocks, ModBlocks.CONTAINER_DOCK.get());
        addBlock(validBlocks, ModBlocks.TRADE_TERMINAL.get());
        LOGGER.info("Patched MineColonies colonybuilding BlockEntityType with Colony Logistics hut blocks.");
    }

    @SuppressWarnings("unchecked")
    private static Set<Block> findValidBlocksSet(BlockEntityType<?> type) {
        for (Field field : BlockEntityType.class.getDeclaredFields()) {
            if (!Set.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(type);
                if (!(value instanceof Set<?> set)) {
                    continue;
                }

                // The vanilla field is a Set<Block>. Make it mutable if needed.
                Set<Block> copy = new HashSet<>();
                for (Object entry : set) {
                    if (entry instanceof Block block) {
                        copy.add(block);
                    }
                }

                try {
                    field.set(type, copy);
                    return copy;
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    // Some JVMs allow mutating the existing set but not replacing the final field.
                    try {
                        ((Set<Block>) set).add(ModBlocks.LOGISTICS_OFFICE.get());
                        ((Set<Block>) set).add(ModBlocks.CONTAINER_DOCK.get());
                        ((Set<Block>) set).add(ModBlocks.TRADE_TERMINAL.get());
                        return (Set<Block>) set;
                    } catch (UnsupportedOperationException ignored) {
                        LOGGER.warn("Found valid block set on {} but it could not be replaced or mutated.", field.getName(), ex);
                    }
                }
            } catch (ReflectiveOperationException | SecurityException ex) {
                LOGGER.debug("Failed while inspecting BlockEntityType field {}", field.getName(), ex);
            }
        }
        return null;
    }

    private static void addBlock(Set<Block> validBlocks, Block block) {
        validBlocks.add(block);
    }

    private MineColoniesBlockEntityTypeCompat() {}
}
