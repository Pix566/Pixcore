package dev.pixcore.neoforge.network;

import dev.pixcore.neoforge.PixcoreMod;
import dev.pixcore.protocol.PixcoreProtocol;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the Pixcore payload type on both logical sides. The client handler
 * itself is registered separately in {@code PixcoreClient} because it is
 * client-only.
 */
@EventBusSubscriber(modid = PixcoreMod.MOD_ID)
public final class NetworkRegistration {
    private NetworkRegistration() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(String.valueOf(PixcoreProtocol.VERSION));
        // Pixcore talks to a Paper server, so the NeoForge side only needs the
        // clientbound registration to receive packets. Outgoing packets are sent
        // directly through ClientPacketDistributor and Paper listens on the same channel.
        registrar.playToClient(PixcorePayload.TYPE, PixcorePayload.STREAM_CODEC);
    }
}
