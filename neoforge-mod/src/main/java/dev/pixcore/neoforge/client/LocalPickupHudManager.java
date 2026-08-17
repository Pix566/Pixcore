package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.Json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side pickup HUD. Uses the vanilla {@code ClientboundTakeItemEntityPacket}
 * via a Mixin to detect when the local player picks up an item, without needing
 * a Pixcore server packet.
 */
public final class LocalPickupHudManager {
    public void onTakeItem(ClientLevel level, int itemId, int playerId, int amount) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getId() != playerId) {
            return;
        }
        if (!(level.getEntity(itemId) instanceof ItemEntity item)) {
            return;
        }
        ItemStack stack = item.getItem().copy();
        if (stack.isEmpty()) {
            return;
        }
        stack.setCount(Math.max(1, amount));
        showPickup(stack);
    }

    private void showPickup(ItemStack stack) {
        if (!PixcoreClientState.INSTANCE.settings.pickupHudEnabled) {
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

    public void tick(Minecraft mc) {
        // Kept for compatibility; actual detection is packet-driven.
    }

    public void clear() {
    }
}
