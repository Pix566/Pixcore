package dev.pixcore.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Armor model wrapper that applies a subtle time-based animation to the head
 * and body when the matching armor rule enables {@code model-anim}.
 */
public class DynamicArmorModel<T extends HumanoidRenderState> extends HumanoidModel<T> {
    private final double speed;

    public DynamicArmorModel(ModelPart root, double speed) {
        super(root);
        this.speed = speed;
    }

    public static <T extends HumanoidRenderState> DynamicArmorModel<T> create(double speed) {
        ModelPart root = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F).getRoot().bake(64, 32);
        return new DynamicArmorModel<>(root, speed);
    }

    @Override
    public void setupAnim(T state) {
        super.setupAnim(state);
        if (Minecraft.getInstance().level == null) {
            return;
        }
        long time = Minecraft.getInstance().level.getGameTime();
        double phase = (time * speed) % 100.0D / 100.0D;
        float angle = (float) (Math.sin(phase * Math.PI * 2.0D) * 0.08D);
        this.head.xRot += angle * 0.5F;
        this.body.yRot += angle * 0.3F;
    }
}
