package dev.pixcore.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pixcore.neoforge.client.ArmorSlotContext;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Exposes the currently rendered armor slot to client item extensions so armor
 * textures can be selected per slot.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
    @Inject(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void pixcore$armorSlotHead(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                                       EquipmentSlot slot, int packedLight, HumanoidModel<?> model, CallbackInfo ci) {
        ArmorSlotContext.set(slot);
    }

    @Inject(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void pixcore$armorSlotReturn(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                                         EquipmentSlot slot, int packedLight, HumanoidModel<?> model, CallbackInfo ci) {
        ArmorSlotContext.clear();
    }
}