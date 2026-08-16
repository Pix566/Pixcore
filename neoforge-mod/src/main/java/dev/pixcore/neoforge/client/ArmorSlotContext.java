package dev.pixcore.neoforge.client;

import net.minecraft.world.entity.EquipmentSlot;

/** Thread-local slot context set while armor layers are being rendered. */
public final class ArmorSlotContext {
    private static final ThreadLocal<EquipmentSlot> CURRENT = new ThreadLocal<>();

    private ArmorSlotContext() {
    }

    public static void set(EquipmentSlot slot) {
        CURRENT.set(slot);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static EquipmentSlot get() {
        return CURRENT.get();
    }
}
