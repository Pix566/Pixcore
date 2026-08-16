package dev.pixcore.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(PixcoreMod.MOD_ID)
public final class PixcoreMod {
    public static final String MOD_ID = "pixcore";
    public static final String MOD_NAME = "Pixcore";

    public PixcoreMod(IEventBus modBus) {
        // All client/networking wiring is annotation-driven in PixcoreClient.
    }
}
