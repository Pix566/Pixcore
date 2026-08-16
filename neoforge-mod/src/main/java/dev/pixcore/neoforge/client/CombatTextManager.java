package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Client-side combat feedback: samples health changes each tick and spawns
 * floating damage/healing numbers near the entity's neck.
 */
public final class CombatTextManager {
    private static final int MAX_TEXTS = 256;
    private static final int MAX_NEW_PER_TICK = 64;
    private static final int LIFE_TICKS = 24;

    private final Map<Integer, Float> lastHealth = new HashMap<>();
    private final List<FloatingText> active = new ArrayList<>();

    public void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            clear();
            return;
        }
        ClientLevel level = mc.level;
        int created = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == mc.player || !living.isAlive() || living.isInvisible()) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > 48.0 * 48.0) {
                continue;
            }
            float health = living.getHealth();
            Float previous = lastHealth.get(entity.getId());
            if (previous != null) {
                float delta = health - previous;
                ClientSettings settings = PixcoreClientState.INSTANCE.settings;
                if (delta < -0.01F && settings.damageEnabled) {
                    addText(entity, String.valueOf(Math.max(1, Math.round(-delta))), 0xFFFF5555, true);
                    created++;
                } else if (delta > 0.01F && settings.healingEnabled) {
                    addText(entity, String.valueOf(Math.max(1, Math.round(delta))), 0xFF55FF55, false);
                    created++;
                }
            }
            lastHealth.put(entity.getId(), health);
            if (created >= MAX_NEW_PER_TICK) {
                break;
            }
        }
        lastHealth.keySet().removeIf(id -> level.getEntity(id) == null);
        ClientSettings settings = PixcoreClientState.INSTANCE.settings;
        if (!settings.damageEnabled) {
            active.removeIf(t -> t.left);
        }
        if (!settings.healingEnabled) {
            active.removeIf(t -> !t.left);
        }
        active.forEach(FloatingText::tickAge);
        active.removeIf(FloatingText::isDead);
        if (active.size() > MAX_TEXTS) {
            active.subList(0, active.size() - MAX_TEXTS).clear();
        }
    }

    private void addText(Entity entity, String text, int color, boolean left) {
        active.add(new FloatingText(entity.getId(), text, color, left,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.9F, entity.getZ()));
    }

    public void render(Minecraft mc, PoseStack poseStack, MultiBufferSource bufferSource, Camera camera) {
        if (mc.level == null) {
            return;
        }
        Iterator<FloatingText> it = active.iterator();
        while (it.hasNext()) {
            FloatingText text = it.next();
            Entity entity = mc.level.getEntity(text.entityId);
            if (entity == null || text.isDead()) {
                it.remove();
                continue;
            }
            float age = text.age;
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() * 0.9F + age * 0.025F;
            double z = entity.getZ();
            float alpha = age < 16 ? 1.0F : 1.0F - (age - 16) / 8.0F;
            int color = (text.color & 0x00FFFFFF) | ((int) (alpha * 255) << 24);
            drawBillboardText(mc.font, poseStack, bufferSource, camera, text.text, x, y, z, color, 0.025F);
        }
    }

    private void drawBillboardText(Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                   Camera camera, String text, double x, double y, double z,
                                   int color, float scale) {
        poseStack.pushPose();
        try {
            poseStack.translate(x - camera.getPosition().x(), y - camera.getPosition().y(), z - camera.getPosition().z());
            poseStack.mulPose(camera.rotation());
            poseStack.scale(scale, -scale, scale);
            Matrix4f pose = poseStack.last().pose();
            font.drawInBatch(text, -font.width(text) / 2.0F, 0, color, true, pose,
                    bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        } finally {
            poseStack.popPose();
        }
    }

    public void clear() {
        active.clear();
        lastHealth.clear();
    }

    private static final class FloatingText {
        final int entityId;
        final String text;
        final int color;
        final boolean left;
        int age;

        FloatingText(int entityId, String text, int color, boolean left, double x, double y, double z) {
            this.entityId = entityId;
            this.text = text;
            this.color = color;
            this.left = left;
        }

        void tickAge() {
            age++;
        }

        boolean isDead() {
            return age >= LIFE_TICKS;
        }
    }
}
