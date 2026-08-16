package dev.pixcore.protocol;

/**
 * Pixcore shared protocol constants.
 *
 * <p>The protocol is intentionally dependency-free: packets are serialized to a
 * byte array with DataInput/DataOutput and transported over Minecraft's custom
 * payload channel. NeoForge and Paper both only need to wrap/unwrap the byte[].
 */
public final class PixcoreProtocol {
    private PixcoreProtocol() {
    }

    /** Custom payload channel used by both Paper and NeoForge. */
    public static final String CHANNEL = "pixcore:main";

    /** Bump when the wire format or capability set changes. */
    public static final int VERSION = 3;

    // Packet ids
    public static final int ID_HANDSHAKE = 1;
    public static final int ID_HANDSHAKE_ACK = 2;
    public static final int ID_ICON_RULES = 3;
    public static final int ID_HUD = 4;
    public static final int ID_TOOLTIP_RULES = 5;
    public static final int ID_PARTICLE = 6;
    public static final int ID_KEYBIND_DEFINITIONS = 7;
    public static final int ID_ARMOR_RULES = 8;
    public static final int ID_KEY_EVENT = 9;
    public static final int ID_EFFECT_CLEAR = 10;
    public static final int ID_RESOURCE_PACK_CHUNK = 11;

    // Client capability bits
    public static final int CAP_COMBAT_TEXT = 1;
    public static final int CAP_MONSTER_HEALTH = 1 << 1;
    public static final int CAP_ITEM_IMAGES = 1 << 2;
    public static final int CAP_EFFECTS = 1 << 3;
    public static final int CAP_KEYBINDS = 1 << 4;

    public static final int CAP_ALL = CAP_COMBAT_TEXT | CAP_MONSTER_HEALTH
            | CAP_ITEM_IMAGES | CAP_EFFECTS | CAP_KEYBINDS;

    public static boolean hasCapability(int capabilities, int cap) {
        return (capabilities & cap) != 0;
    }
}
