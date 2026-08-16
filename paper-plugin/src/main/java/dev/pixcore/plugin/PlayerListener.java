package dev.pixcore.plugin;

import dev.pixcore.protocol.HandshakeAckPacket;
import dev.pixcore.protocol.HandshakePacket;
import dev.pixcore.protocol.HudPacket;
import dev.pixcore.protocol.Json;
import dev.pixcore.protocol.KeyEventPacket;
import dev.pixcore.protocol.Packet;
import dev.pixcore.protocol.PacketCodec;
import dev.pixcore.protocol.PixcoreProtocol;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerListener implements Listener, PluginMessageListener {
    private final PixcorePlugin plugin;
    private final Map<UUID, long[]> keyEventWindows = new ConcurrentHashMap<>();
    private final Map<UUID, long[]> packetWindows = new ConcurrentHashMap<>();

    public PlayerListener(PixcorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!PixcoreProtocol.CHANNEL.equals(channel)) {
            return;
        }
        if (!allowPacket(player)) {
            return;
        }
        try {
            Packet packet = PacketCodec.decode(message);
            if (packet instanceof HandshakePacket handshake) {
                handleHandshake(player, handshake);
            } else if (packet instanceof KeyEventPacket keyEvent) {
                if (allowKeyEvent(player)) {
                    plugin.getLogger().info(player.getName() + " pressed Pixcore key " + keyEvent.keyId()
                            + " action=" + keyEvent.action());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to decode Pixcore packet from " + player.getName() + ": " + e.getMessage());
        }
    }

    private boolean allowPacket(Player player) {
        int maxPerSecond = Math.max(1, plugin.getConfig().getInt("limits.max-events-per-player-per-second", 40));
        long now = System.currentTimeMillis() / 1000L;
        long[] window = packetWindows.computeIfAbsent(player.getUniqueId(), k -> new long[]{now, 0L});
        if (window[0] != now) {
            window[0] = now;
            window[1] = 0L;
        }
        return ++window[1] <= maxPerSecond;
    }

    private boolean allowKeyEvent(Player player) {
        int maxPerSecond = Math.max(1, plugin.getConfig().getInt("limits.max-key-events-per-second", 20));
        long now = System.currentTimeMillis() / 1000L;
        long[] window = keyEventWindows.computeIfAbsent(player.getUniqueId(), k -> new long[]{now, 0L});
        if (window[0] != now) {
            window[0] = now;
            window[1] = 0L;
        }
        return ++window[1] <= maxPerSecond;
    }

    private void handleHandshake(Player player, HandshakePacket handshake) {
        int serverVersion = PixcoreProtocol.VERSION;
        boolean accepted = handshake.minProtocolVersion() <= serverVersion
                && serverVersion <= handshake.maxProtocolVersion();
        PlayerSession session = new PlayerSession(handshake.capabilities(), handshake.clientName());
        plugin.putSession(player.getUniqueId(), session);

        plugin.sendPacket(player, new HandshakeAckPacket(
                accepted,
                plugin.getServer().getName(),
                plugin.getServer().getMinecraftVersion(),
                serverVersion
        ));

        if (accepted) {
            plugin.syncAll(player, session);
            plugin.syncResourcePack(player);
        } else {
            plugin.getLogger().warning("Pixcore client " + player.getName()
                    + " supports protocol " + handshake.minProtocolVersion()
                    + "-" + handshake.maxProtocolVersion()
                    + " but server expects " + serverVersion);
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!plugin.getConfigManager().moduleEnabled("pickup-hud")) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = event.getItem().getItemStack();
        String name = stack.hasItemMeta() && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName()
                ? stack.getItemMeta().getDisplayName()
                : stack.getI18NDisplayName();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "text");
        data.put("text", List.of(name + " x" + stack.getAmount()));
        data.put("anchor", "bottom-right");
        data.put("x", -8);
        data.put("y", -8);
        data.put("argb", 0xFFFFFFFF);
        data.put("scale", 1.0);
        data.put("shadow", true);
        data.put("duration-ticks", 60);
        plugin.sendPacket(player, new HudPacket("pickup", Json.write(data)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer().getUniqueId());
        keyEventWindows.remove(event.getPlayer().getUniqueId());
        packetWindows.remove(event.getPlayer().getUniqueId());
    }
}
