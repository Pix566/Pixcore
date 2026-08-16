package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.joml.Matrix4f;

/**
 * Client-side monster health bar: draws a billboard name and a compact
 * pixel-style health bar above hostile mobs.
 */
public final class MonsterHealthManager {
    private static final int MAX_ENTITIES = 128;
    private static final double RANGE = 32.0;

    public void render(Minecraft mc, PoseStack poseStack, MultiBufferSource bufferSource, Camera camera) {
        if (mc.level == null || mc.player == null || !PixcoreClientState.INSTANCE.settings.monsterHealthEnabled) {
            return;
        }
        ClientLevel level = mc.level;
        int count = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !(entity instanceof Enemy)) {
                continue;
            }
            if (!living.isAlive() || living.getMaxHealth() <= 0) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > RANGE * RANGE) {
                continue;
            }
            if (count++ >= MAX_ENTITIES) {
                break;
            }
            float ratio = Math.max(0.0F, Math.min(1.0F, living.getHealth() / living.getMaxHealth()));
            int barLength = Math.max(1, Math.round(ratio * 10));
            String bar = "█".repeat(barLength) + "░".repeat(10 - barLength);
            String name = living.getDisplayName().getString();
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() + 0.35D;
            double z = entity.getZ();

            drawBillboardText(mc.font, poseStack, bufferSource, camera, name, x, y + 0.12D, z, 0xFFA80000, 0.022F);
            drawBillboardText(mc.font, poseStack, bufferSource, camera, bar, x, y, z, 0xFFFF8C00, 0.022F);
            drawBillboardText(mc.font, poseStack, bufferSource, camera,
                    Math.round(living.getHealth()) + "/" + Math.round(living.getMaxHealth()),
                    x, y - 0.12D, z, 0xFFFFFFFF, 0.018F);
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
}
