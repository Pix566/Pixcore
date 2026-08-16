package dev.pixcore.plugin;

import dev.pixcore.protocol.Packet;
import dev.pixcore.protocol.PacketCodec;
import dev.pixcore.protocol.PixcoreProtocol;
import dev.pixcore.protocol.ResourcePackChunkPacket;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public final class PixcorePlugin extends JavaPlugin {
    private static PixcorePlugin instance;

    private ConfigManager configManager;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, java.util.Queue<ResourcePackChunkPacket>> resourcePackQueues = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        configManager.saveDefaults();
        configManager.reload();

        PlayerListener listener = new PlayerListener(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PixcoreProtocol.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, PixcoreProtocol.CHANNEL, listener);
        getServer().getPluginManager().registerEvents(listener, this);

        var command = getCommand("pixcore");
        if (command != null) {
            PixcoreCommand executor = new PixcoreCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getScheduler().runTaskTimer(this, this::flushResourcePackQueues, 1L, 1L);
        getLogger().info("Pixcore enabled. Channel: " + PixcoreProtocol.CHANNEL);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        sessions.clear();
    }

    public static PixcorePlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerSession session(UUID uuid) {
        return sessions.get(uuid);
    }

    public PlayerSession session(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void putSession(UUID uuid, PlayerSession session) {
        sessions.put(uuid, session);
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
        resourcePackQueues.remove(uuid);
    }

    public void sendPacket(Player player, Packet packet) {
        if (player == null || !player.isOnline()) {
            return;
        }
        try {
            player.sendPluginMessage(this, PixcoreProtocol.CHANNEL, PacketCodec.encode(packet));
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to send Pixcore packet to " + player.getName(), e);
        }
    }

    /**
     * Sends the server's resource pack folder to a compatible client in chunks.
     */
    public void syncResourcePack(Player player) {
        if (!configManager.moduleEnabled("resource-pack-sync")) {
            return;
        }
        PlayerSession session = session(player);
        if (session == null) {
            return;
        }
        resourcePackQueues.remove(player.getUniqueId());
        File base = new File(getDataFolder(), "resourcepacks/pixcore");
        if (!base.isDirectory()) {
            return;
        }
        List<File> files = new ArrayList<>();
        collectFiles(base, base, files);
        int chunkSize = 16 * 1024;
        for (File file : files) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String relative = base.toPath().relativize(file.toPath()).toString().replace('\\', '/');
                if (!session.shouldSendResourcePack(relative, bytes)) {
                    continue;
                }
                Queue<ResourcePackChunkPacket> queue = resourcePackQueues.computeIfAbsent(
                        player.getUniqueId(), k -> new LinkedBlockingQueue<>());
                int offset = 0;
                while (offset < bytes.length) {
                    int length = Math.min(chunkSize, bytes.length - offset);
                    byte[] chunk = new byte[length];
                    System.arraycopy(bytes, offset, chunk, 0, length);
                    boolean last = offset + length >= bytes.length;
                    queue.add(new ResourcePackChunkPacket(relative, offset, chunk, last));
                    offset += length;
                }
                if (bytes.length == 0) {
                    queue.add(new ResourcePackChunkPacket(relative, 0, new byte[0], true));
                }
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Failed to read resource pack file " + file, e);
            }
        }
    }

    private void flushResourcePackQueues() {
        int perTick = Math.max(1, getConfig().getInt("limits.max-resource-pack-chunks-per-tick", 20));
        for (Player player : getServer().getOnlinePlayers()) {
            Queue<ResourcePackChunkPacket> queue = resourcePackQueues.get(player.getUniqueId());
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            for (int i = 0; i < perTick && !queue.isEmpty(); i++) {
                sendPacket(player, queue.poll());
            }
        }
    }

    private void collectFiles(File base, File dir, List<File> output) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collectFiles(base, child, output);
            } else {
                output.add(child);
            }
        }
    }

    /**
     * Sends every enabled rule set to a compatible player after handshake.
     */
    public void syncAll(Player player, PlayerSession session) {
        int caps = session.capabilities();
        ConfigManager cfg = configManager;

        if (cfg.moduleEnabled("item-images") && PixcoreProtocol.hasCapability(caps, PixcoreProtocol.CAP_ITEM_IMAGES)) {
            String json = cfg.getIconsJson();
            if (session.shouldSend("icons", json)) {
                sendPacket(player, new dev.pixcore.protocol.IconRulesPacket(json));
            }
        }
        if (cfg.moduleEnabled("armor") && PixcoreProtocol.hasCapability(caps, PixcoreProtocol.CAP_ITEM_IMAGES)) {
            String json = cfg.getArmorJson();
            if (session.shouldSend("armor", json)) {
                sendPacket(player, new dev.pixcore.protocol.ArmorRulesPacket(json));
            }
        }
        if (cfg.moduleEnabled("tooltip") && PixcoreProtocol.hasCapability(caps, PixcoreProtocol.CAP_EFFECTS)) {
            String json = cfg.getTooltipJson();
            if (session.shouldSend("tooltip", json)) {
                sendPacket(player, new dev.pixcore.protocol.TooltipRulesPacket(json));
            }
        }
        if (cfg.moduleEnabled("keybinds") && PixcoreProtocol.hasCapability(caps, PixcoreProtocol.CAP_KEYBINDS)) {
            String json = cfg.getKeybindsJson();
            if (session.shouldSend("keybinds", json)) {
                sendPacket(player, new dev.pixcore.protocol.KeybindDefinitionsPacket(json));
            }
        }
        if (cfg.moduleEnabled("hud") && PixcoreProtocol.hasCapability(caps, PixcoreProtocol.CAP_EFFECTS)) {
            String hudHash = dev.pixcore.protocol.Json.write(cfg.getHudEntries());
            if (session.shouldSend("hud", hudHash)) {
                for (Map.Entry<String, Object> entry : cfg.getHudEntries().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Map<?, ?> map) {
                        Object enable = map.get("enable");
                        if (enable instanceof Boolean b && b) {
                            sendPacket(player, new dev.pixcore.protocol.HudPacket(entry.getKey(), dev.pixcore.protocol.Json.write(map)));
                        }
                    }
                }
            }
        }
        if (cfg.moduleEnabled("particles") && PixcoreProtocol.hasCapability(caps, PixcoreProtocol.CAP_EFFECTS)) {
            String particleHash = dev.pixcore.protocol.Json.write(cfg.getParticleEntries());
            if (session.shouldSend("particles", particleHash)) {
                int maxParticleCount = Math.max(1, cfg.getMaxParticleCount());
                for (Map.Entry<String, Object> entry : cfg.getParticleEntries().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Map<?, ?> map) {
                        Map<String, Object> safe = new LinkedHashMap<>();
                        for (Map.Entry<?, ?> e : map.entrySet()) {
                            safe.put(String.valueOf(e.getKey()), e.getValue());
                        }
                        Object count = safe.get("count");
                        int countValue = count instanceof Number n ? n.intValue() : 40;
                        safe.put("count", Math.min(countValue, maxParticleCount));
                        sendPacket(player, new dev.pixcore.protocol.ParticlePacket(dev.pixcore.protocol.Json.write(safe)));
                    }
                }
            }
        }
    }
}
