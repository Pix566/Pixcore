package dev.pixcore.neoforge.mixin;

import dev.pixcore.neoforge.client.IconRule;
import dev.pixcore.neoforge.client.ImageCache;
import dev.pixcore.neoforge.client.PixcoreClientState;
import dev.pixcore.neoforge.client.PixcoreDynamicTextureRenderer;
import dev.pixcore.neoforge.client.PixcoreRenderData;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Mixin into {@link ItemModelResolver} so matching items render the Pixcore
 * dynamic model during item rendering without modifying the ItemStack.
 */
@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {
    @Inject(method = "appendItemLayers", at = @At("HEAD"), cancellable = true, remap = false)
    private void pixcore$onAppendItemLayers(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext displayContext,
            @Nullable Level level,
            @Nullable LivingEntity entity,
            int seed,
            CallbackInfo ci
    ) {
        IconRule rule = PixcoreClientState.INSTANCE.findIconRule(stack);
        if (rule == null) {
            return;
        }
        ResourceLocation texture = ImageCache.INSTANCE.getOrLoad(rule.textureFor(displayContext));
        if (texture == null) {
            return;
        }
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        layer.setupSpecialModel(new PixcoreDynamicTextureRenderer(), new PixcoreRenderData(texture, PixcoreClientState.INSTANCE.scaleFor(rule), rule.depth(),
                rule.xScale(), rule.yScale(), rule.zScale(), rule.handheld()));
        if (rule.foil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            renderState.setAnimated();
        }
        ci.cancel();
    }
}
