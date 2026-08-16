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
        registrar.playToClient(PixcorePayload.TYPE, PixcorePayload.STREAM_CODEC);
        registrar.playToServer(PixcorePayload.TYPE, PixcorePayload.STREAM_CODEC,
                (payload, context) -> {
                    // On NeoForge servers there is nothing to do; Paper handles input via Bukkit.
                });
    }
}
