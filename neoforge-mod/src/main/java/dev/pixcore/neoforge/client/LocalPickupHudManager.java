package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.Json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side pickup HUD. Detects item entities that disappear near the local
 * player and shows a HUD notification without requiring the server to send it.
 */
public final class LocalPickupHudManager {
    private static final double TRACK_RANGE = 8.0D;
    private static final double PICKUP_RANGE = 4.0D;

    private final Map<Integer, TrackedItem> tracked = new HashMap<>();

    public void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            tracked.clear();
            return;
        }

        Map<Integer, TrackedItem> current = new HashMap<>();
        for (net.minecraft.world.entity.Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item)) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > TRACK_RANGE * TRACK_RANGE) {
                continue;
            }
            TrackedItem previous = tracked.get(entity.getId());
            if (previous != null) {
                current.put(entity.getId(), previous);
            } else {
                current.put(entity.getId(), new TrackedItem(entity.getId(),
                        item.getItem().copy(), entity.getX(), entity.getY(), entity.getZ()));
            }
        }

        for (Map.Entry<Integer, TrackedItem> entry : tracked.entrySet()) {
            if (current.containsKey(entry.getKey())) {
                continue;
            }
            TrackedItem item = entry.getValue();
            if (mc.player.distanceToSqr(item.x, item.y, item.z) <= PICKUP_RANGE * PICKUP_RANGE) {
                showPickup(item.stack);
            }
        }

        tracked.clear();
        tracked.putAll(current);
    }

    private void showPickup(ItemStack stack) {
        if (stack.isEmpty() || !PixcoreClientState.INSTANCE.settings.pickupHudEnabled) {
            return;
        }
        String name = stack.getHoverName().getString();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "text");
        data.put("text", List.of(name + " x" + stack.getCount()));
        data.put("anchor", "bottom-right");
        data.put("x", -PixcoreClientState.INSTANCE.settings.pickupHudRightMargin);
        data.put("y", -PixcoreClientState.INSTANCE.settings.pickupHudBottomMargin);
        data.put("argb", 0xFFFFFFFF);
        data.put("scale", 1.0);
        data.put("shadow", true);
        data.put("duration-ticks", 60);
        PixcoreClientState.INSTANCE.hud.onPacket("pickup", Json.write(data));
    }

    public void clear() {
        tracked.clear();
    }

    private static final class TrackedItem {
        final int id;
        final ItemStack stack;
        final double x;
        final double y;
        final double z;

        TrackedItem(int id, ItemStack stack, double x, double y, double z) {
            this.id = id;
            this.stack = stack;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
