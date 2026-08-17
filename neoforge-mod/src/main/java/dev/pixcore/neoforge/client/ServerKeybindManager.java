package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import dev.pixcore.neoforge.network.PixcoreServerPayload;
import dev.pixcore.protocol.Json;
import dev.pixcore.protocol.KeyEventPacket;
import dev.pixcore.protocol.PacketCodec;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dynamically registers server-defined keybinds while connected to a Pixcore
 * server and forwards press/release events back to the server.
 */
public final class ServerKeybindManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MAPPING_PREFIX = "key.pixcore.server.";

    private final Map<String, KeyMapping> mappings = new HashMap<>();
    private final Set<String> inOptions = new HashSet<>();
    private final List<ServerKeybind> keybinds = new ArrayList<>();

    public void onPacket(String json) {
        keybinds.clear();
        List<ServerKeybind> parsed = new ArrayList<>();
        Set<InputConstants.Key> usedKeys = new HashSet<>();

        Object root = Json.parse(json);
        if (root instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String id = String.valueOf(entry.getKey());
                if (!(entry.getValue() instanceof Map<?, ?> definition)) {
                    continue;
                }
                KeyMapping mapping = mappings.get(id);
                if (mapping == null) {
                    mapping = createMapping(id, definition);
                    mappings.put(id, mapping);
                } else {
                    updateMapping(mapping, definition);
                }

                InputConstants.Key key = mapping.getKey();
                if (!usedKeys.add(key)) {
                    LOGGER.warn("Pixcore keybind '{}' uses the same default key as another server keybind", id);
                }
                parsed.add(new ServerKeybind(id, mapping, false));
            }
        }

        keybinds.addAll(parsed);
        refreshOptions();
    }

    public void tick(Minecraft mc) {
        if (mc.player == null || keybinds.isEmpty()) {
            return;
        }
        for (ServerKeybind keybind : keybinds) {
            boolean down = keybind.mapping.isDown();
            if (down && !keybind.wasDown) {
                send(new KeyEventPacket(keybind.id, 1));
            } else if (!down && keybind.wasDown) {
                send(new KeyEventPacket(keybind.id, 0));
            }
            keybind.wasDown = down;
        }
    }

    public void clearAll() {
        keybinds.clear();
        removeFromOptions();
    }

    private void updateMapping(KeyMapping mapping, Map<?, ?> definition) {
        String defaultKey = str(definition.get("default-key"), null);
        if (defaultKey == null) {
            return;
        }
        try {
            mapping.setKey(InputConstants.getKey(defaultKey));
        } catch (Exception e) {
            LOGGER.warn("Pixcore failed to update keybind '{}': {}", mapping.getName(), e.getMessage());
        }
    }

    private KeyMapping createMapping(String id, Map<?, ?> definition) {
        String name = MAPPING_PREFIX + id;
        String category = str(definition.get("category"), "key.categories.pixcore");
        String defaultKey = str(definition.get("default-key"), "key.keyboard.g");
        InputConstants.Key key;
        try {
            key = InputConstants.getKey(defaultKey);
        } catch (Exception e) {
            key = InputConstants.getKey("key.keyboard.g");
        }
        return new KeyMapping(name, key.getType(), key.getValue(), category);
    }

    private void refreshOptions() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        Set<String> active = new HashSet<>();
        for (ServerKeybind keybind : keybinds) {
            active.add(keybind.mapping.getName());
        }

        List<KeyMapping> list = new ArrayList<>();
        for (KeyMapping mapping : mc.options.keyMappings) {
            if (mapping.getName().startsWith(MAPPING_PREFIX) && !active.contains(mapping.getName())) {
                continue;
            }
            list.add(mapping);
        }
        for (ServerKeybind keybind : keybinds) {
            if (!inOptions.contains(keybind.mapping.getName())) {
                list.add(keybind.mapping);
                inOptions.add(keybind.mapping.getName());
            }
        }
        mc.options.keyMappings = list.toArray(new KeyMapping[0]);
        KeyMapping.resetMapping();
    }

    private void removeFromOptions() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        List<KeyMapping> list = new ArrayList<>();
        for (KeyMapping mapping : mc.options.keyMappings) {
            if (mapping.getName().startsWith(MAPPING_PREFIX)) {
                continue;
            }
            list.add(mapping);
        }
        mc.options.keyMappings = list.toArray(new KeyMapping[0]);
        inOptions.clear();
        KeyMapping.resetMapping();
    }

    private static void send(KeyEventPacket packet) {
        try {
            ClientPacketDistributor.sendToServer(new PixcoreServerPayload(PacketCodec.encode(packet)));
        } catch (Exception ignored) {
        }
    }

    private static String str(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }

    private static final class ServerKeybind {
        final String id;
        final KeyMapping mapping;
        boolean wasDown;

        ServerKeybind(String id, KeyMapping mapping, boolean wasDown) {
            this.id = id;
            this.mapping = mapping;
            this.wasDown = wasDown;
        }
    }
}
