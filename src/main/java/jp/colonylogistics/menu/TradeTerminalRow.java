package jp.colonylogistics.menu;

import jp.colonylogistics.contract.ContractStatus;
import jp.colonylogistics.trade.ItemMatchMode;
import jp.colonylogistics.trade.PlayerTradeContract;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record TradeTerminalRow(
        UUID contractId,
        UUID creatorPlayer,
        ResourceLocation requestedItemId,
        int requestedCount,
        ResourceLocation rewardItemId,
        int rewardCount,
        ItemMatchMode matchMode,
        ContractStatus status
) {
    public static TradeTerminalRow fromContract(PlayerTradeContract contract) {
        return new TradeTerminalRow(
                contract.id(),
                contract.creatorPlayer(),
                contract.requestedItemId(),
                contract.requestedCount(),
                contract.rewardItemId(),
                contract.rewardCount(),
                contract.matchMode(),
                contract.status()
        );
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(contractId);
        buf.writeUUID(creatorPlayer);
        buf.writeUtf(requestedItemId.toString());
        buf.writeVarInt(requestedCount);
        buf.writeUtf(rewardItemId.toString());
        buf.writeVarInt(rewardCount);
        buf.writeVarInt(matchMode.ordinal());
        buf.writeUtf(status.name());
    }

    private static ResourceLocation readId(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = ResourceLocation.tryParse(buf.readUtf());
        return id != null ? id : ResourceLocation.withDefaultNamespace("air");
    }

    public static TradeTerminalRow read(RegistryFriendlyByteBuf buf) {
        return new TradeTerminalRow(
                buf.readUUID(),
                buf.readUUID(),
                readId(buf),
                buf.readVarInt(),
                readId(buf),
                buf.readVarInt(),
                ItemMatchMode.byOrdinal(buf.readVarInt()),
                readStatus(buf.readUtf())
        );
    }

    private static ContractStatus readStatus(String name) {
        try {
            return ContractStatus.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return ContractStatus.OPEN;
        }
    }
}
