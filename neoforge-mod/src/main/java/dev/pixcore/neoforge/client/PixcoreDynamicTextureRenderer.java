package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;

/** Renders a simple 3D box for a dynamic Pixcore item model. */
public final class PixcoreDynamicTextureRenderer implements SpecialModelRenderer<PixcoreRenderData> {
    @Override
    public void render(
            PixcoreRenderData data,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            boolean hasFoilType
    ) {
        if (data == null || data.texture() == null) {
            return;
        }
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(data.texture()));
        Matrix4f pose = poseStack.last().pose();
        float scale = (float) data.scale();
        if (data.handheld() && (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)) {
            scale *= 1.25F;
        }
        float s = 0.5F * scale * (float) data.xScale();
        float sy = 0.5F * scale * (float) data.yScale();
        float d = 0.125F * scale * (float) data.depth() * (float) data.zScale();

        quad(consumer, pose, -s, -sy, d, s, -sy, d, s, sy, d, -s, sy, d,
                0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
        quad(consumer, pose, s, -sy, -d, -s, -sy, -d, -s, sy, -d, s, sy, -d,
                0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
        quad(consumer, pose, s, -sy, -d, s, -sy, d, s, sy, d, s, sy, -d,
                0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        quad(consumer, pose, -s, -sy, d, -s, -sy, -d, -s, sy, -d, -s, sy, d,
                0.0F, 1.0F, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        quad(consumer, pose, -s, sy, -d, s, sy, -d, s, sy, d, -s, sy, d,
                0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay);
        quad(consumer, pose, -s, -sy, d, s, -sy, d, s, -sy, -d, -s, -sy, -d,
                0.0F, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, 0.0F, packedLight, packedOverlay);
    }

    private static void quad(
            VertexConsumer consumer, Matrix4f pose,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float u0, float v0, float u1, float v1,
            float nx, float ny, float nz,
            int packedLight, int packedOverlay
    ) {
        consumer.addVertex(pose, x0, y0, z0).setColor(255, 255, 255, 255).setUv(u0, v0)
                .setLight(packedLight).setOverlay(packedOverlay).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x1, y1, z1).setColor(255, 255, 255, 255).setUv(u1, v0)
                .setLight(packedLight).setOverlay(packedOverlay).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(255, 255, 255, 255).setUv(u1, v1)
                .setLight(packedLight).setOverlay(packedOverlay).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(255, 255, 255, 255).setUv(u0, v1)
                .setLight(packedLight).setOverlay(packedOverlay).setNormal(nx, ny, nz);
    }

    @Override
    public void getExtents(Set<Vector3f> output) {
        float s = 0.5F;
        float d = 0.125F;
        output.add(new Vector3f(-s, -s, -d));
        output.add(new Vector3f(s, -s, -d));
        output.add(new Vector3f(s, s, -d));
        output.add(new Vector3f(-s, s, -d));
        output.add(new Vector3f(-s, -s, d));
        output.add(new Vector3f(s, -s, d));
        output.add(new Vector3f(s, s, d));
        output.add(new Vector3f(-s, s, d));
    }

    @Override
    public PixcoreRenderData extractArgument(ItemStack stack) {
        IconRule rule = PixcoreClientState.INSTANCE.findIconRule(stack);
        if (rule == null) {
            return null;
        }
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        ResourceLocation texture = ImageCache.INSTANCE.getOrLoad(state.textureFor(rule, ItemDisplayContext.NONE));
        if (texture == null) {
            return null;
        }
        return new PixcoreRenderData(texture, state.scaleFor(rule), state.depthFor(rule),
                state.xScaleFor(rule), state.yScaleFor(rule), state.zScaleFor(rule), state.handheldFor(rule));
    }
}
