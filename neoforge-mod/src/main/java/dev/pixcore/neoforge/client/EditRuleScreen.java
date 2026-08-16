package dev.pixcore.neoforge.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client-side field editor for a single rule's scale override. */
public final class EditRuleScreen extends Screen {
    private final String ruleId;
    private final double currentScale;
    private EditBox scaleBox;

    public EditRuleScreen(String ruleId, double currentScale) {
        super(Component.literal("编辑规则: " + ruleId));
        this.ruleId = ruleId;
        this.currentScale = currentScale;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.scaleBox = new EditBox(this.font, centerX - 80, 50, 160, 20, Component.literal("scale"));
        this.scaleBox.setValue(String.valueOf(currentScale));
        addRenderableWidget(this.scaleBox);

        addRenderableWidget(Button.builder(Component.literal("保存"), btn -> save())
                .bounds(centerX - 80, 90, 160, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("清除覆盖"), btn -> clearOverride())
                .bounds(centerX - 80, 120, 160, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> this.minecraft.setScreen(null))
                .bounds(centerX - 80, 150, 160, 20)
                .build());
    }

    private void save() {
        try {
            double scale = Double.parseDouble(this.scaleBox.getValue());
            PixcoreClientState.INSTANCE.settings.ruleScaleOverrides.put(ruleId, scale);
            PixcoreClientState.INSTANCE.settings.save();
        } catch (NumberFormatException ignored) {
        }
        this.minecraft.setScreen(null);
    }

    private void clearOverride() {
        PixcoreClientState.INSTANCE.settings.ruleScaleOverrides.remove(ruleId);
        PixcoreClientState.INSTANCE.settings.save();
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 20, 0xFFFFFF, true);
        guiGraphics.drawString(this.font, "scale", this.width / 2 - 80, 38, 0xE0E0E0, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
