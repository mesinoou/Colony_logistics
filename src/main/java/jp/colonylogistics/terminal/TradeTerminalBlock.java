package jp.colonylogistics.terminal;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.menu.SavedTradeTerminalContainer;
import jp.colonylogistics.menu.TradeTerminalMenu;
import jp.colonylogistics.menu.TradeTerminalRow;
import jp.colonylogistics.minecolonies.block.AbstractColonyLogisticsHutBlock;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/** MineColonies hut block for the player trade terminal. */
public class TradeTerminalBlock extends AbstractColonyLogisticsHutBlock<TradeTerminalBlock> {
    public TradeTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties.strength(4.0F, 12.0F).noOcclusion());
    }

    @NotNull
    @Override
    public String getHutName() {
        return "blockhuttradeterminal";
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return ModMineColoniesBuildings.TRADE_TERMINAL.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Trade Terminal controls are opened from the MineColonies building tab.
     * Direct block interactions remain MineColonies interactions for upgrade and
     * repair management.
     */
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    public static InteractionResult openTradeTerminalMenu(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ResolvedLogisticsBuilding building = ResolvedLogisticsBuilding.resolve(serverLevel, pos);
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(serverLevel);
        List<TradeTerminalRow> openRows = data
                .openPlayerTradesForTerminal(pos)
                .sorted(Comparator.comparingLong(trade -> trade.createdGameTime()))
                .limit(TradeTerminalMenu.MAX_OPEN_ROWS)
                .map(TradeTerminalRow::fromContract)
                .toList();
        List<TradeTerminalRow> historyRows = data
                .finishedPlayerTradesForTerminal(pos)
                .sorted(Comparator.comparingLong((jp.colonylogistics.trade.PlayerTradeContract trade) -> trade.createdGameTime()).reversed())
                .limit(TradeTerminalMenu.MAX_HISTORY_ROWS)
                .map(TradeTerminalRow::fromContract)
                .toList();
        SavedTradeTerminalContainer setupInventory = new SavedTradeTerminalContainer(serverLevel, building.key());
        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> TradeTerminalMenu.server(containerId, inventory, pos, building.colonyId(), building.buildingLevel(), building.usable(), openRows, historyRows, setupInventory),
                        Component.translatable("menu.colonylogistics.trade_terminal")
                ),
                buf -> TradeTerminalMenu.writeSnapshot(buf, pos, building.colonyId(), building.buildingLevel(), building.usable(), openRows, historyRows)
        );
        return InteractionResult.CONSUME;
    }
}
