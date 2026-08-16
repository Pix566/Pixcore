package dev.pixcore.neoforge.client;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
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
        String path = type == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                ? rule.innerTexture()
                : rule.outerTexture();
        return ImageCache.INSTANCE.getOrLoad(path);
    }
}
