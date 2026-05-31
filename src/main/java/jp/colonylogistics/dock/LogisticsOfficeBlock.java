package jp.colonylogistics.dock;

import jp.colonylogistics.chat.SafeSystemChat;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import jp.colonylogistics.board.FreightBoardSnapshots;
import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.menu.FreightBoardMenu;
import jp.colonylogistics.menu.FreightBoardRow;
import jp.colonylogistics.minecolonies.block.AbstractColonyLogisticsHutBlock;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import jp.colonylogistics.service.FreightMarketService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MineColonies hut block for the logistics office.
 *
 * <p>Phase 17.1 makes the Logistics Office the colony-bound entry point for the
 * freight board. This prevents the standalone board block from being placed
 * anywhere and used as an unbound global market terminal.</p>
 */
public class LogisticsOfficeBlock extends AbstractColonyLogisticsHutBlock<LogisticsOfficeBlock> {
    public LogisticsOfficeBlock(BlockBehaviour.Properties properties) {
        super(properties.strength(4.0F, 12.0F).noOcclusion());
    }

    @NotNull
    @Override
    public String getHutName() {
        return "blockhutlogisticsoffice";
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return ModMineColoniesBuildings.LOGISTICS_OFFICE.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Phase 17.9.11 moves Colony Logistics screens into MineColonies'
     * building-window tab. Direct core-block interactions now remain MineColonies
     * interactions so players can always reach upgrade/repair/building controls.
     */
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    public static InteractionResult openLogisticsOfficeBoard(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ResolvedLogisticsBuilding building = ResolvedLogisticsBuilding.resolve(serverLevel, pos);
        if (!building.usable() || building.colonyId() < 0) {
            SafeSystemChat.send(serverPlayer, Component.translatable("message.colonylogistics.logistics_office.inactive"));
            return InteractionResult.CONSUME;
        }

        FreightMarketService market = new FreightMarketService();
        market.ensureMinimumInventoryJobs(serverLevel);
        market.ensureMinimumContainerJobs(serverLevel);

        List<FreightBoardRow> rows = FreightBoardSnapshots.rowsForOffice(serverLevel, serverPlayer, building.colonyId());
        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> FreightBoardMenu.server(containerId, inventory, pos, rows),
                        Component.translatable("menu.colonylogistics.logistics_office_board", building.colonyId())
                ),
                buf -> FreightBoardMenu.writeSnapshot(buf, pos, rows)
        );
        return InteractionResult.CONSUME;
    }
}
