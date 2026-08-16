package dev.pixcore.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Client item extensions used to replace armor textures with Pixcore armor
 * rule textures while keeping the vanilla armor model.
 */
public final class PixcoreClientItemExtensions implements IClientItemExtensions {
    @Override
    public ResourceLocation getArmorTexture(
            ItemStack stack,
            EquipmentClientInfo.LayerType type,
            EquipmentClientInfo.Layer layer,
            ResourceLocation defaultTexture
    ) {
        IconRule rule = PixcoreClientState.INSTANCE.findArmorRule(stack);
        if (rule == null) {
            return null;
        }
        EquipmentSlot slot = ArmorSlotContext.get();
        String path = slot != null ? rule.textureForSlot(slot) : null;
        if (path == null) {
            path = type == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                    ? rule.innerTexture()
                    : rule.outerTexture();
        }
        return ImageCache.INSTANCE.getOrLoad(path);
    }

    @Override
    public int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
        IconRule rule = PixcoreClientState.INSTANCE.findArmorRule(stack);
        if (rule == null) {
            return fallbackColor;
        }
        if (rule.pulseColor() != null) {
            long time = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
            double phase = (time * rule.pulseSpeed()) % 100.0D / 100.0D;
            float factor = 0.5F + 0.5F * (float) Math.sin(phase * Math.PI * 2.0D);
            int base = rule.pulseColor();
            int a = (base >>> 24) & 0xFF;
            int r = (int) (((base >> 16) & 0xFF) * factor);
            int g = (int) (((base >> 8) & 0xFF) * factor);
            int b = (int) ((base & 0xFF) * factor);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
        if (rule.color() != null) {
            return rule.color();
        }
        return fallbackColor;
    }
}
