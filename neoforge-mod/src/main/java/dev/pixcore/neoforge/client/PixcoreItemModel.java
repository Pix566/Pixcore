package dev.pixcore.neoforge.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Dynamic item model used by {@code pixcore:dynamic}. It looks up the current
 * server icon rule for the rendered stack and draws the configured image as a
 * flat item model.
 */
public final class PixcoreItemModel implements ItemModel {
    private final SpecialModelRenderer<PixcoreRenderData> renderer;

    private PixcoreItemModel(SpecialModelRenderer<PixcoreRenderData> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void update(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext,
            ClientLevel level,
            LivingEntity entity,
            int seed
    ) {
        IconRule rule = PixcoreClientState.INSTANCE.findIconRule(stack);
        if (rule == null) {
            return;
        }
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        ResourceLocation texture = ImageCache.INSTANCE.getOrLoad(state.textureFor(rule, displayContext));
        if (texture == null) {
            return;
        }
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        layer.setupSpecialModel(renderer, new PixcoreRenderData(texture, state.scaleFor(rule), state.depthFor(rule),
                state.xScaleFor(rule), state.yScaleFor(rule), state.zScaleFor(rule), state.handheldFor(rule)));
        if (state.foilFor(rule)) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            renderState.setAnimated();
        }
    }

    public static final class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            return new PixcoreItemModel(new PixcoreDynamicTextureRenderer());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }
    }
}
