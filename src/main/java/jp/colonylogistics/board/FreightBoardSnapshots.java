package jp.colonylogistics.board;

import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.menu.FreightBoardMenu;
import jp.colonylogistics.menu.FreightBoardRow;
import jp.colonylogistics.service.FreightMarketService;
import jp.colonylogistics.trade.PlayerTradeContract;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Builds the contract snapshot used by the Logistics Office board view.
 *
 * <p>Phase 17.1 moves the functional Freight Board into the Logistics Office.
 * Generated freight remains scoped to contracts related to the office colony,
 * while open player-to-player trades are intentionally global offers visible
 * from every active office during multiplayer play.</p>
 */
public final class FreightBoardSnapshots {
    public static List<FreightBoardRow> rowsForOffice(ServerLevel level, ServerPlayer viewer, int officeColonyId) {
        FreightMarketService market = new FreightMarketService();
        List<FreightBoardRow> rows = new ArrayList<>();

        LogisticsMarketSavedData.get(level).playerTrades().stream()
                // Player-to-player trades are global offers.  The originating terminal
                // and colony are still shown in the row details, but every active
                // Logistics Office must be able to discover them during multiplayer
                // testing instead of only the creator colony's office.
                .filter(trade -> trade.status() != ContractStatus.DRAFT)
                .filter(trade -> isVisiblePlayerTradeFromOffice(trade, officeColonyId, viewer.getUUID()))
                .sorted(Comparator
                        .comparingInt((PlayerTradeContract trade) -> tradeBoardOrder(trade, viewer.getUUID()))
                        .thenComparing(Comparator.comparingLong(PlayerTradeContract::createdGameTime).reversed()))
                .map(trade -> FreightBoardRow.fromPlayerTrade(trade, viewer.getUUID()))
                .forEach(rows::add);

        market.boardContracts(level, viewer.getUUID()).stream()
                .filter(contract -> isRelatedToColony(contract, officeColonyId))
                .map(contract -> FreightBoardRow.fromContract(contract, viewer.getUUID()))
                .forEach(rows::add);

        return rows.stream()
                .limit(FreightBoardMenu.MAX_ROWS)
                .toList();
    }

    private static boolean isVisiblePlayerTradeFromOffice(PlayerTradeContract trade, int officeColonyId, UUID viewerId) {
        if (trade.status() == ContractStatus.OPEN) {
            return true;
        }
        if (trade.colonyId() == officeColonyId) {
            return true;
        }
        return trade.creatorPlayer().equals(viewerId)
                || trade.deliveredBy().filter(viewerId::equals).isPresent();
    }

    public static boolean isRelatedToColony(LogisticsContract contract, int colonyId) {
        return contract.originColonyId() == colonyId || contract.destinationColonyId() == colonyId;
    }

    private static int tradeBoardOrder(PlayerTradeContract trade, UUID viewerId) {
        boolean viewerInvolved = trade.creatorPlayer().equals(viewerId)
                || trade.deliveredBy().filter(viewerId::equals).isPresent();
        if (viewerInvolved && trade.status() == ContractStatus.OPEN) {
            return 0;
        }
        if (viewerInvolved && isFinishedTradeStatus(trade.status())) {
            return 1;
        }
        if (trade.status() == ContractStatus.OPEN) {
            return 2;
        }
        if (isFinishedTradeStatus(trade.status())) {
            return 3;
        }
        return 4;
    }

    private static boolean isFinishedTradeStatus(ContractStatus status) {
        return status == ContractStatus.COMPLETED
                || status == ContractStatus.CANCELLED
                || status == ContractStatus.EXPIRED
                || status == ContractStatus.FAILED;
    }

    private FreightBoardSnapshots() {}
}
