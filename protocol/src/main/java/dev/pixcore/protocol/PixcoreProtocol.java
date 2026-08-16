package dev.pixcore.protocol;

import java.util.LinkedHashMap;
import java.util.Map;

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
    public static final int VERSION = 4;

    // Module versions
    public static final int MODULE_ITEM_IMAGES_VERSION = 4;
    public static final int MODULE_ARMOR_VERSION = 2;
    public static final int MODULE_TOOLTIP_VERSION = 2;
    public static final int MODULE_HUD_VERSION = 2;
    public static final int MODULE_PARTICLES_VERSION = 2;
    public static final int MODULE_KEYBINDS_VERSION = 2;
    public static final int MODULE_RESOURCE_PACK_VERSION = 2;

    public static String defaultModuleVersionsJson() {
        Map<String, Object> versions = new LinkedHashMap<>();
        versions.put("item-images", MODULE_ITEM_IMAGES_VERSION);
        versions.put("armor", MODULE_ARMOR_VERSION);
        versions.put("tooltip", MODULE_TOOLTIP_VERSION);
        versions.put("hud", MODULE_HUD_VERSION);
        versions.put("particles", MODULE_PARTICLES_VERSION);
        versions.put("keybinds", MODULE_KEYBINDS_VERSION);
        versions.put("resource-pack", MODULE_RESOURCE_PACK_VERSION);
        return Json.write(versions);
    }

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
