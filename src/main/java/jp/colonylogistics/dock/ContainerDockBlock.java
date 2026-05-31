package jp.colonylogistics.dock;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import jp.colonylogistics.buildingstate.LogisticsBuildingKey;
import jp.colonylogistics.buildingstate.ResolvedLogisticsBuilding;
import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.FreightContainerCoreBlockEntity;
import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.contract.DeliveryUnitType;
import jp.colonylogistics.contract.LogisticsMarketSavedData;
import jp.colonylogistics.contract.LogisticsContract;
import jp.colonylogistics.menu.ContainerDockMenu;
import jp.colonylogistics.menu.DockContainerRow;
import jp.colonylogistics.menu.DockContractRow;
import jp.colonylogistics.minecolonies.block.AbstractColonyLogisticsHutBlock;
import jp.colonylogistics.minecolonies.registry.ModMineColoniesBuildings;
import jp.colonylogistics.service.ContainerDockService;
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

/** MineColonies hut block for a functional Container Dock. */
public class ContainerDockBlock extends AbstractColonyLogisticsHutBlock<ContainerDockBlock> {
    public ContainerDockBlock(BlockBehaviour.Properties properties) {
        super(properties.strength(4.0F, 12.0F).noOcclusion());
    }

    @NotNull
    @Override
    public String getHutName() {
        return "blockhutcontainerdock";
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return ModMineColoniesBuildings.CONTAINER_DOCK.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Colony Logistics dock controls are now opened from a MineColonies building
     * tab. The block interaction itself is left to MineColonies so upgrade and
     * repair controls are always available from the core block.
     */
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    public static InteractionResult openContainerDockMenu(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ResolvedLogisticsBuilding building = ResolvedLogisticsBuilding.resolve(serverLevel, pos);
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(serverLevel);
        DockMode mode = data.dockMode(building.key());

        DockSnapshot snapshot = buildSnapshot(serverLevel, serverPlayer, pos, building, mode);
        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> ContainerDockMenu.server(
                                containerId,
                                inventory,
                                pos,
                                snapshot.colonyId(),
                                building.buildingLevel(),
                                building.usable(),
                                mode.name(),
                                snapshot.contractRows(),
                                snapshot.containerRows()
                        ),
                        Component.translatable("menu.colonylogistics.container_dock")
                ),
                buf -> ContainerDockMenu.writeSnapshot(
                        buf,
                        pos,
                        snapshot.colonyId(),
                        building.buildingLevel(),
                        building.usable(),
                        mode.name(),
                        snapshot.contractRows(),
                        snapshot.containerRows()
                )
        );
        return InteractionResult.CONSUME;
    }

    private static DockSnapshot buildSnapshot(ServerLevel level, ServerPlayer player, BlockPos dockPos, ResolvedLogisticsBuilding building, DockMode mode) {
        int colonyId = building.colonyId();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);

        List<DockContractRow> contractRows = mode.canExport() ? data.contracts().stream()
                .filter(contract -> contract.status() == ContractStatus.ACCEPTED || contract.status() == ContractStatus.PICKED_UP)
                .filter(contract -> contract.assignedPlayer().filter(player.getUUID()::equals).isPresent())
                .filter(LogisticsContract::canSpawnMoreContainers)
                .filter(contract -> contract.originDockPos().filter(pos -> pos.equals(dockPos)).isPresent())
                .filter(contract -> contract.freightSpec().isPresent())
                .filter(contract -> contract.freightSpec().get().deliveryUnitType() == DeliveryUnitType.CONTAINER_MULTIBLOCK)
                .sorted(Comparator.comparingLong(contract -> deadlineSortKey(contract.freightSpec().get().pickupDeadline())))
                .limit(ContainerDockMenu.MAX_CONTRACT_ROWS)
                .map(contract -> DockContractRow.fromContract(contract, suggestContainerCorePos(level, dockPos, contract.freightSpec().get().containerRequirement().size())))
                .toList() : List.of();

        List<DockContainerRow> containerRows = mode.canImport() ? findNearbyContainerRows(level, player, dockPos).stream()
                .limit(ContainerDockMenu.MAX_CONTAINER_ROWS)
                .toList() : List.of();

        return new DockSnapshot(colonyId, contractRows, containerRows);
    }

    private static long deadlineSortKey(long deadline) {
        return deadline <= 0L ? Long.MAX_VALUE : deadline;
    }

    private static List<DockContainerRow> findNearbyContainerRows(ServerLevel level, ServerPlayer player, BlockPos dockPos) {
        int radius = (int) Math.ceil(ContainerDockService.containerRecognitionRadius());
        ContainerDockService service = new ContainerDockService();
        LogisticsMarketSavedData data = LogisticsMarketSavedData.get(level);
        return BlockPos.betweenClosedStream(
                        dockPos.offset(-radius, -radius, -radius),
                        dockPos.offset(radius, radius, radius)
                )
                .map(BlockPos::immutable)
                .filter(pos -> level.getBlockEntity(pos) instanceof FreightContainerCoreBlockEntity)
                .map(pos -> (FreightContainerCoreBlockEntity) level.getBlockEntity(pos))
                .filter(container -> container.manifest().isPresent())
                .filter(container -> service.isWithinDeliveryRange(level, dockPos, container.getBlockPos(), ContainerDockService.containerRecognitionRadius()))
                .map(container -> debugContainerRow(level, player, dockPos, data, service, container))
                .sorted(Comparator
                        .comparing((DockContainerRow row) -> !row.deliverable())
                        .thenComparingDouble(DockContainerRow::distance)
                        .thenComparing(row -> row.corePos().toShortString()))
                .toList();
    }

    private static DockContainerRow debugContainerRow(
            ServerLevel level,
            ServerPlayer player,
            BlockPos dockPos,
            LogisticsMarketSavedData data,
            ContainerDockService service,
            FreightContainerCoreBlockEntity container
    ) {
        ContainerDockService.ContainerDeliveryAnalysis analysis = service.analyzeDelivery(level, player, dockPos, container);
        return DockContainerRow.debug(
                container,
                analysis.deliverable(),
                analysis.deliveryStatus(),
                analysis.distance(),
                analysis.contractStatus(),
                analysis.cargoName(),
                analysis.expectedContainerText(),
                analysis.issueHint()
        );
    }

    private static BlockPos suggestContainerCorePos(ServerLevel level, BlockPos dockPos, ContainerSize size) {
        ContainerDockService service = new ContainerDockService();
        BlockPos preferred = service.contractSpawnCandidateCorePositions(level, dockPos, size).stream()
                .findFirst()
                .orElse(dockPos.offset(
                        size.halfWidth() + 3,
                        size.halfHeight() + 1,
                        0
                ));
        return service.findAvailableContractCorePos(level, dockPos, size)
                .orElse(preferred);
    }

    private record DockSnapshot(int colonyId, List<DockContractRow> contractRows, List<DockContainerRow> containerRows) {}
}
