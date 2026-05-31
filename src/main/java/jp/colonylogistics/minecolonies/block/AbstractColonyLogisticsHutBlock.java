package jp.colonylogistics.minecolonies.block;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for Colony Logistics MineColonies huts.
 *
 * MineColonies hut anchors must use MineColonies' own TileEntityColonyBuilding.
 * Do not attach a custom logistics BlockEntity to these blocks. Per-building
 * Dock/Terminal runtime state belongs in SavedData keyed by dimension + hut pos.
 */
public abstract class AbstractColonyLogisticsHutBlock<T extends AbstractColonyLogisticsHutBlock<T>> extends AbstractBlockHut<T> {
    private static final Pattern LEVEL_SUFFIX = Pattern.compile("(\\d+)$");

    protected AbstractColonyLogisticsHutBlock(final Properties properties) {
        super(properties);
    }

    /**
     * MineColonies' default AbstractBlockHut#setup intentionally returns early for
     * creative non-fancy placement. That is fine for some official flows, but in
     * development it can leave third-party huts pasted as plain blocks without a
     * MineColonies building registered behind them. Force the same registration
     * path for this case.
     */
    @Override
    public boolean setup(
            @NotNull final ServerPlayer player,
            @NotNull final Level world,
            @NotNull final BlockPos pos,
            @NotNull final Blueprint blueprint,
            @NotNull final RotationMirror rotationMirror,
            final boolean fancyPlacement,
            final String pack,
            final String path) {

        final boolean creativeInstant = player.isCreative() && !fancyPlacement;
        if (!creativeInstant) {
            return super.setup(player, world, pos, blueprint, rotationMirror, fancyPlacement, pack, path);
        }

        final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (!(anchor.getBlock() instanceof AbstractBlockHut<?> hut)) {
            return true;
        }

        // Place the MineColonies hut anchor through the normal block path so the
        // shared TileEntityColonyBuilding exists, then run the Build Tool hook.
        world.destroyBlock(pos, true);
        world.setBlockAndUpdate(pos, anchor);
        hut.onBlockPlacedByBuildTool(world, pos, anchor, player, null, rotationMirror, pack, path);

        if (IMinecoloniesAPI.getInstance().getConfig().getServer().blueprintBuildMode.get()) {
            return true;
        }

        final IBuilding building = IColonyManager.getInstance().getBuilding(world, pos);
        if (building == null) {
            SoundUtils.playErrorSound(player, player.blockPosition());
            Log.getLogger().error("BuildTool: Colony Logistics hut was pasted but no MineColonies building was registered at " + pos, new Exception());
            return false;
        }

        SoundUtils.playSuccessSound(player, player.blockPosition());
        if (building.getTileEntity() != null) {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
            if (colony == null) {
                Log.getLogger().info("No colony found for Colony Logistics hut placement by " + player.getName().getString());
                return false;
            }
            building.getTileEntity().setColony(colony);
        }

        building.setStructurePack(pack);
        building.setBlueprintPath(path);
        building.setBuildingLevel(parseLevel(path));
        building.setRotationMirror(rotationMirror);
        building.onUpgradeComplete(blueprint, building.getBuildingLevel());
        return true;
    }


    /**
     * Legacy helper retained for older call sites during the UI migration.
     *
     * <p>Phase 17.9.11 moves Colony Logistics controls into MineColonies'
     * building-window tabs. Direct hut interactions should normally delegate to
     * MineColonies rather than opening Colony Logistics screens themselves.</p>
     */
    protected boolean shouldOpenMineColoniesBuildingUi(final ItemStack stack, final Player player) {
        if (player.isShiftKeyDown() || stack.isEmpty()) {
            return false;
        }
        final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return false;
        }
        final String namespace = itemId.getNamespace();
        final String path = itemId.getPath();
        return ("structurize".equals(namespace) || "minecolonies".equals(namespace))
                && ("build_tool".equals(path) || "buildtool".equals(path));
    }

    protected static ItemInteractionResult toItemInteractionResult(final InteractionResult result) {
        if (result == InteractionResult.SUCCESS) {
            return ItemInteractionResult.SUCCESS;
        }
        if (result == InteractionResult.CONSUME || result == InteractionResult.CONSUME_PARTIAL) {
            return ItemInteractionResult.CONSUME;
        }
        if (result == InteractionResult.FAIL) {
            return ItemInteractionResult.FAIL;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static int parseLevel(final String path) {
        final String withoutExtension = path == null ? "" : path.replace(".blueprint", "");
        final Matcher matcher = LEVEL_SUFFIX.matcher(withoutExtension);
        if (matcher.find()) {
            try {
                return Math.max(1, Math.min(5, Integer.parseInt(matcher.group(1))));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        return 1;
    }
}
