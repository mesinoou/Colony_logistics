package jp.colonylogistics.network;

import jp.colonylogistics.ColonyLogistics;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the small C2S networking surface used by the first GUI pass.
 *
 * <p>Menus still synchronize read-only snapshots through their extra-data buffers.
 * Button actions are now normal payloads so survival players do not need command
 * permissions to accept jobs, spawn containers, or deliver containers.</p>
 */
public final class ModNetwork {
    private static final String NETWORK_VERSION = "1";

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ColonyLogistics.MOD_ID).versioned(NETWORK_VERSION);
        registrar.playToServer(AcceptFreightPayload.TYPE, AcceptFreightPayload.STREAM_CODEC, AcceptFreightPayload::handle);
        registrar.playToServer(CancelFreightPayload.TYPE, CancelFreightPayload.STREAM_CODEC, CancelFreightPayload::handle);
        registrar.playToServer(SpawnContainerPayload.TYPE, SpawnContainerPayload.STREAM_CODEC, SpawnContainerPayload::handle);
        registrar.playToServer(DeliverContainerPayload.TYPE, DeliverContainerPayload.STREAM_CODEC, DeliverContainerPayload::handle);
        registrar.playToServer(CreatePlayerTradePayload.TYPE, CreatePlayerTradePayload.STREAM_CODEC, CreatePlayerTradePayload::handle);
        registrar.playToServer(DeliverPlayerTradePayload.TYPE, DeliverPlayerTradePayload.STREAM_CODEC, DeliverPlayerTradePayload::handle);
        registrar.playToServer(CancelPlayerTradePayload.TYPE, CancelPlayerTradePayload.STREAM_CODEC, CancelPlayerTradePayload::handle);
        registrar.playToServer(OpenColonyLogisticsMenuPayload.TYPE, OpenColonyLogisticsMenuPayload.STREAM_CODEC, OpenColonyLogisticsMenuPayload::handle);
    }

    private ModNetwork() {}
}
