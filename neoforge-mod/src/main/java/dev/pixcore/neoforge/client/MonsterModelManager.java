package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.pixcore.protocol.Json;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Client-side monster appearance overlay driven by {@code MonsterRulesPacket}. */
public final class MonsterModelManager {
    private List<MonsterRule> rules = new ArrayList<>();

    public void onPacket(String json) {
        List<MonsterRule> parsed = new ArrayList<>();
        Object root = Json.parse(json);
        if (root instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map<?, ?> ruleMap) {
                    parsed.add(MonsterRule.fromMap(String.valueOf(entry.getKey()), ruleMap));
                }
            }
        }
        rules = parsed;
    }

    public void render(RenderLivingEvent.Post<?, ?, ?> event) {
        if (rules.isEmpty()) {
            return;
        }
        EntityRenderState renderState = event.getRenderState();
        MonsterRule rule = null;
        for (MonsterRule candidate : rules) {
            if (candidate.matches(renderState.entityType)) {
                rule = candidate;
                break;
            }
        }
        if (rule == null) {
            return;
        }
        ResourceLocation texture = ImageCache.INSTANCE.getOrLoad(rule.texture());
        if (texture == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f pose = poseStack.last().pose();
        float scale = (float) rule.scale();
        float w = Math.max(0.1F, renderState.boundingBoxWidth) * scale;
        float h = Math.max(0.1F, renderState.boundingBoxHeight) * scale;
        float d = w;
        int alpha = 120;
        if (rule.color() != null) {
            alpha = (rule.color() >>> 24) & 0xFF;
        }

        drawFace(consumer, pose, -w / 2, 0, -d / 2, w / 2, 0, -d / 2, w / 2, h, -d / 2, -w / 2, h, -d / 2,
                0.0F, 1.0F, 1.0F, 0.0F, alpha, event.getPackedLight());
        drawFace(consumer, pose, w / 2, 0, d / 2, -w / 2, 0, d / 2, -w / 2, h, d / 2, w / 2, h, d / 2,
                0.0F, 1.0F, 1.0F, 0.0F, alpha, event.getPackedLight());
        drawFace(consumer, pose, w / 2, 0, -d / 2, w / 2, 0, d / 2, w / 2, h, d / 2, w / 2, h, -d / 2,
                0.0F, 1.0F, 1.0F, 0.0F, alpha, event.getPackedLight());
        drawFace(consumer, pose, -w / 2, 0, d / 2, -w / 2, 0, -d / 2, -w / 2, h, -d / 2, -w / 2, h, d / 2,
                0.0F, 1.0F, 1.0F, 0.0F, alpha, event.getPackedLight());
        drawFace(consumer, pose, -w / 2, h, -d / 2, w / 2, h, -d / 2, w / 2, h, d / 2, -w / 2, h, d / 2,
                0.0F, 1.0F, 1.0F, 0.0F, alpha, event.getPackedLight());
    }

    public void clear() {
        rules = new ArrayList<>();
    }

    private static void drawFace(
            VertexConsumer consumer, Matrix4f pose,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float u0, float v0, float u1, float v1,
            int alpha, int packedLight
    ) {
        consumer.addVertex(pose, x0, y0, z0).setColor(255, 255, 255, alpha).setUv(u0, v0)
                .setLight(packedLight).setOverlay(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y1, z1).setColor(255, 255, 255, alpha).setUv(u1, v0)
                .setLight(packedLight).setOverlay(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x2, y2, z2).setColor(255, 255, 255, alpha).setUv(u1, v1)
                .setLight(packedLight).setOverlay(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x3, y3, z3).setColor(255, 255, 255, alpha).setUv(u0, v1)
                .setLight(packedLight).setOverlay(packedLight).setNormal(0.0F, 0.0F, 1.0F);
    }
}
