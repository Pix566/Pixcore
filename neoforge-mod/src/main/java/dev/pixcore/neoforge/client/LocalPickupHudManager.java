package dev.pixcore.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

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
        PixcoreClientState.INSTANCE.pickupHudRenderer.add(stack);
    }

    public void tick(Minecraft mc) {
        // Kept for compatibility; actual detection is packet-driven.
    }

    public void clear() {
    }
}
