package jp.colonylogistics.contract;

import jp.colonylogistics.colony.ColonyLogisticsState;
import jp.colonylogistics.buildingstate.DockRuntimeState;
import jp.colonylogistics.buildingstate.LogisticsBuildingKey;
import jp.colonylogistics.buildingstate.TradeTerminalRuntimeState;
import jp.colonylogistics.currency.CurrencyService;
import jp.colonylogistics.dock.DockMode;
import jp.colonylogistics.profile.CarrierProfile;
import jp.colonylogistics.trade.PlayerTradeContract;
import jp.colonylogistics.trade.PlayerTradeNbt;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public final class LogisticsMarketSavedData extends SavedData {
    private static final String DATA_NAME = "colonylogistics_market";

    private final Map<Integer, ColonyLogisticsState> colonies = new HashMap<>();
    private final Map<UUID, LogisticsContract> contracts = new HashMap<>();
    private final Map<UUID, CarrierProfile> carrierProfiles = new HashMap<>();
    private final Map<UUID, PlayerTradeContract> playerTrades = new HashMap<>();
    private final Map<LogisticsBuildingKey, DockRuntimeState> dockStates = new HashMap<>();
    private final Map<LogisticsBuildingKey, TradeTerminalRuntimeState> terminalStates = new HashMap<>();

    private long nextMarketGenerationGameTime = 0L;
    private long nextMarketPurgeGameTime = 0L;

    public static LogisticsMarketSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(LogisticsMarketSavedData::new, LogisticsMarketSavedData::load),
                DATA_NAME
        );
    }

    public ColonyLogisticsState colonyState(int colonyId) {
        return colonies.computeIfAbsent(colonyId, ColonyLogisticsState::new);
    }

    public Collection<ColonyLogisticsState> allColonies() {
        return colonies.values();
    }

    public Stream<ColonyLogisticsState> activeColonies() {
        return colonies.values().stream().filter(state -> state.logisticsOfficeLevel() > 0);
    }

    public Optional<LogisticsContract> contract(UUID id) {
        return Optional.ofNullable(contracts.get(id));
    }

    public Collection<LogisticsContract> contracts() {
        return contracts.values();
    }

    public Optional<PlayerTradeContract> playerTrade(UUID id) {
        return Optional.ofNullable(playerTrades.get(id));
    }

    public Collection<PlayerTradeContract> playerTrades() {
        return playerTrades.values();
    }

    public DockRuntimeState dockState(LogisticsBuildingKey key) {
        return dockStates.computeIfAbsent(key, ignored -> DockRuntimeState.DEFAULT);
    }

    public DockMode dockMode(LogisticsBuildingKey key) {
        return DockMode.BOTH;
    }

    /** Deprecated: Container Docks are always bidirectional as of Phase 17.9.8. */
    public void setDockMode(LogisticsBuildingKey key, DockMode mode) {
        // Keep method for old packet/source compatibility, but do not persist modes.
    }

    public TradeTerminalRuntimeState terminalState(LogisticsBuildingKey key) {
        return terminalStates.computeIfAbsent(key, ignored -> TradeTerminalRuntimeState.empty());
    }

    public void setTerminalState(LogisticsBuildingKey key, TradeTerminalRuntimeState state) {
        terminalStates.put(key, state);
        setDirty();
    }

    public void clearTerminalEscrowInput(LogisticsBuildingKey key) {
        TradeTerminalRuntimeState state = terminalState(key);
        terminalStates.put(key, state.withEscrowInput(net.minecraft.world.item.ItemStack.EMPTY));
        setDirty();
    }

    public Stream<PlayerTradeContract> openPlayerTradesForTerminal(net.minecraft.core.BlockPos terminalPos) {
        return playerTrades.values().stream()
                .filter(contract -> contract.status() == ContractStatus.OPEN)
                .filter(contract -> contract.terminalPos().equals(terminalPos));
    }

    public Stream<PlayerTradeContract> finishedPlayerTradesForTerminal(net.minecraft.core.BlockPos terminalPos) {
        return playerTrades.values().stream()
                .filter(contract -> contract.status() != ContractStatus.OPEN)
                .filter(contract -> contract.terminalPos().equals(terminalPos));
    }

    public int purgeFinishedPlayerTrades() {
        int before = playerTrades.size();
        playerTrades.values().removeIf(contract -> contract.status() == ContractStatus.COMPLETED
                || contract.status() == ContractStatus.CANCELLED
                || contract.status() == ContractStatus.EXPIRED
                || contract.status() == ContractStatus.FAILED);
        int removed = before - playerTrades.size();
        if (removed > 0) setDirty();
        return removed;
    }

    public Stream<LogisticsContract> contractsForStatus(ContractStatus status) {
        return contracts.values().stream().filter(contract -> contract.status() == status);
    }

    public long openGeneratedJobsForColony(int colonyId) {
        return contracts.values().stream()
                .filter(contract -> contract.type() == ContractType.GENERATED_FREIGHT)
                .filter(contract -> contract.status() == ContractStatus.OPEN)
                .filter(contract -> contract.originColonyId() == colonyId)
                .count();
    }

    public long openContainerJobsForColony(int colonyId) {
        return contracts.values().stream()
                .filter(contract -> contract.type() == ContractType.GENERATED_FREIGHT)
                .filter(contract -> contract.status() == ContractStatus.OPEN)
                .filter(contract -> contract.originColonyId() == colonyId)
                .filter(contract -> contract.freightSpec().isPresent())
                .filter(contract -> contract.freightSpec().get().deliveryUnitType() == DeliveryUnitType.CONTAINER_MULTIBLOCK)
                .count();
    }

    public long activeContainerJobsForColony(int colonyId) {
        return contracts.values().stream()
                .filter(contract -> contract.type() == ContractType.GENERATED_FREIGHT)
                .filter(contract -> contract.originColonyId() == colonyId)
                .filter(contract -> contract.freightSpec().isPresent())
                .filter(contract -> contract.freightSpec().get().deliveryUnitType() == DeliveryUnitType.CONTAINER_MULTIBLOCK)
                .filter(contract -> contract.status() == ContractStatus.ACCEPTED || contract.status() == ContractStatus.PICKED_UP)
                .count();
    }

    public long acceptedJobsForPlayer(UUID playerId) {
        return contracts.values().stream()
                .filter(contract -> contract.assignedPlayer().filter(playerId::equals).isPresent())
                .filter(contract -> contract.status() == ContractStatus.ACCEPTED || contract.status() == ContractStatus.PICKED_UP)
                .count();
    }

    public CarrierProfile carrierProfile(UUID playerId) {
        return carrierProfiles.computeIfAbsent(playerId, CarrierProfile::new);
    }

    public Collection<CarrierProfile> carrierProfiles() {
        return carrierProfiles.values();
    }

    public long nextMarketGenerationGameTime() {
        return nextMarketGenerationGameTime;
    }

    public void setNextMarketGenerationGameTime(long gameTime) {
        this.nextMarketGenerationGameTime = gameTime;
        setDirty();
    }

    public long nextMarketPurgeGameTime() {
        return nextMarketPurgeGameTime;
    }

    public void setNextMarketPurgeGameTime(long gameTime) {
        this.nextMarketPurgeGameTime = gameTime;
        setDirty();
    }

    public void putContract(LogisticsContract contract) {
        contracts.put(contract.id(), normalizeGeneratedContractReward(contract));
        setDirty();
    }

    public void putPlayerTrade(PlayerTradeContract contract) {
        playerTrades.put(contract.id(), contract);
        setDirty();
    }

    public void replacePlayerTrade(PlayerTradeContract contract) {
        playerTrades.put(contract.id(), contract);
        setDirty();
    }

    public void replaceContract(LogisticsContract contract) {
        contracts.put(contract.id(), normalizeGeneratedContractReward(contract));
        setDirty();
    }

    public int purgeFinishedContracts() {
        int before = contracts.size();
        contracts.values().removeIf(contract -> contract.status() == ContractStatus.COMPLETED
                || contract.status() == ContractStatus.CANCELLED
                || contract.status() == ContractStatus.EXPIRED
                || contract.status() == ContractStatus.FAILED);
        int removed = before - contracts.size();
        if (removed > 0) setDirty();
        return removed;
    }

    private static LogisticsContract normalizeGeneratedContractReward(LogisticsContract contract) {
        if (contract.type() != ContractType.GENERATED_FREIGHT) {
            return contract;
        }
        RewardSpec normalized = new CurrencyService().normalizeGeneratedReward(contract.reward());
        return normalized.equals(contract.reward()) ? contract : contract.withReward(normalized);
    }

    public static LogisticsMarketSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        LogisticsMarketSavedData data = new LogisticsMarketSavedData();

        ListTag colonyTags = tag.getList("Colonies", Tag.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.size(); i++) {
            ColonyLogisticsState state = ColonyLogisticsState.load(colonyTags.getCompound(i));
            data.colonies.put(state.colonyId(), state);
        }

        ListTag contractTags = tag.getList("Contracts", Tag.TAG_COMPOUND);
        for (int i = 0; i < contractTags.size(); i++) {
            LogisticsContract contract = normalizeGeneratedContractReward(ContractNbt.load(contractTags.getCompound(i)));
            data.contracts.put(contract.id(), contract);
        }

        ListTag profileTags = tag.getList("CarrierProfiles", Tag.TAG_COMPOUND);
        for (int i = 0; i < profileTags.size(); i++) {
            CarrierProfile profile = CarrierProfile.load(profileTags.getCompound(i));
            data.carrierProfiles.put(profile.playerId(), profile);
        }

        ListTag playerTradeTags = tag.getList("PlayerTrades", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerTradeTags.size(); i++) {
            PlayerTradeContract trade = PlayerTradeNbt.load(playerTradeTags.getCompound(i), registries);
            data.playerTrades.put(trade.id(), trade);
        }

        ListTag dockStateTags = tag.getList("DockStates", Tag.TAG_COMPOUND);
        for (int i = 0; i < dockStateTags.size(); i++) {
            CompoundTag entry = dockStateTags.getCompound(i);
            data.dockStates.put(LogisticsBuildingKey.load(entry.getCompound("Key")), DockRuntimeState.load(entry.getCompound("State")));
        }

        ListTag terminalStateTags = tag.getList("TerminalStates", Tag.TAG_COMPOUND);
        for (int i = 0; i < terminalStateTags.size(); i++) {
            CompoundTag entry = terminalStateTags.getCompound(i);
            data.terminalStates.put(LogisticsBuildingKey.load(entry.getCompound("Key")), TradeTerminalRuntimeState.load(entry.getCompound("State"), registries));
        }

        data.nextMarketGenerationGameTime = tag.getLong("NextMarketGenerationGameTime");
        data.nextMarketPurgeGameTime = tag.getLong("NextMarketPurgeGameTime");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag colonyTags = new ListTag();
        for (ColonyLogisticsState state : colonies.values()) {
            colonyTags.add(state.save());
        }
        tag.put("Colonies", colonyTags);

        ListTag contractTags = new ListTag();
        for (LogisticsContract contract : contracts.values()) {
            contractTags.add(ContractNbt.save(contract));
        }
        tag.put("Contracts", contractTags);

        ListTag profileTags = new ListTag();
        for (CarrierProfile profile : carrierProfiles.values()) {
            profileTags.add(profile.save());
        }
        tag.put("CarrierProfiles", profileTags);

        ListTag playerTradeTags = new ListTag();
        for (PlayerTradeContract trade : playerTrades.values()) {
            playerTradeTags.add(PlayerTradeNbt.save(trade, registries));
        }
        tag.put("PlayerTrades", playerTradeTags);

        ListTag dockStateTags = new ListTag();
        for (Map.Entry<LogisticsBuildingKey, DockRuntimeState> entry : dockStates.entrySet()) {
            CompoundTag stateTag = new CompoundTag();
            stateTag.put("Key", entry.getKey().save());
            stateTag.put("State", entry.getValue().save());
            dockStateTags.add(stateTag);
        }
        tag.put("DockStates", dockStateTags);

        ListTag terminalStateTags = new ListTag();
        for (Map.Entry<LogisticsBuildingKey, TradeTerminalRuntimeState> entry : terminalStates.entrySet()) {
            CompoundTag stateTag = new CompoundTag();
            stateTag.put("Key", entry.getKey().save());
            stateTag.put("State", entry.getValue().save(registries));
            terminalStateTags.add(stateTag);
        }
        tag.put("TerminalStates", terminalStateTags);

        tag.putLong("NextMarketGenerationGameTime", nextMarketGenerationGameTime);
        tag.putLong("NextMarketPurgeGameTime", nextMarketPurgeGameTime);
        return tag;
    }
}
