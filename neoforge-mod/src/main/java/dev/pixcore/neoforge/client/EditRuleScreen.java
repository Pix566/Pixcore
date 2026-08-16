package dev.pixcore.neoforge.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client-side field editor for a single rule. */
public final class EditRuleScreen extends Screen {
    private final String ruleId;
    private final IconRule rule;
    private final PixcoreClientState state;
    private EditBox textureBox;
    private EditBox scaleBox;
    private EditBox depthBox;
    private EditBox xScaleBox;
    private EditBox yScaleBox;
    private EditBox zScaleBox;
    private Button handheldButton;
    private Button foilButton;

    public EditRuleScreen(String ruleId) {
        super(Component.literal("编辑规则: " + ruleId));
        this.ruleId = ruleId;
        this.state = PixcoreClientState.INSTANCE;
        this.rule = findRule();
    }

    private IconRule findRule() {
        for (IconRule r : state.iconRules()) {
            if (r.id().equals(ruleId)) {
                return r;
            }
        }
        for (IconRule r : state.armorRules()) {
            if (r.id().equals(ruleId)) {
                return r;
            }
        }
        return null;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        if (rule == null) {
            addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> this.minecraft.setScreen(null))
                    .bounds(centerX - 50, y, 100, 20).build());
            return;
        }
        textureBox = addField(centerX, y, "texture", state.settings.ruleTextureOverrides.getOrDefault(ruleId, rule.texture()));
        y += 24;
        scaleBox = addField(centerX, y, "scale", String.valueOf(state.scaleFor(rule)));
        y += 24;
        depthBox = addField(centerX, y, "depth", String.valueOf(state.depthFor(rule)));
        y += 24;
        xScaleBox = addField(centerX, y, "x-scale", String.valueOf(state.xScaleFor(rule)));
        y += 24;
        yScaleBox = addField(centerX, y, "y-scale", String.valueOf(state.yScaleFor(rule)));
        y += 24;
        zScaleBox = addField(centerX, y, "z-scale", String.valueOf(state.zScaleFor(rule)));
        y += 28;

        handheldButton = Button.builder(Component.literal("handheld: " + state.handheldFor(rule)), btn -> {
            boolean current = state.handheldFor(rule);
            state.settings.ruleHandheldOverrides.put(ruleId, !current);
            state.settings.save();
            btn.setMessage(Component.literal("handheld: " + !current));
        }).bounds(centerX - 90, y, 180, 20).build();
        addRenderableWidget(handheldButton);
        y += 24;

        foilButton = Button.builder(Component.literal("foil: " + state.foilFor(rule)), btn -> {
            boolean current = state.foilFor(rule);
            state.settings.ruleFoilOverrides.put(ruleId, !current);
            state.settings.save();
            btn.setMessage(Component.literal("foil: " + !current));
        }).bounds(centerX - 90, y, 180, 20).build();
        addRenderableWidget(foilButton);
        y += 28;

        addRenderableWidget(Button.builder(Component.literal("保存"), btn -> save())
                .bounds(centerX - 90, y, 180, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("清除全部覆盖"), btn -> clearAllOverrides())
                .bounds(centerX - 90, y, 180, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> this.minecraft.setScreen(null))
                .bounds(centerX - 90, y, 180, 20).build());
    }

    private EditBox addField(int centerX, int y, String label, String value) {
        EditBox box = new EditBox(this.font, centerX - 80, y, 160, 18, Component.literal(label));
        box.setValue(value);
        addRenderableWidget(box);
        return box;
    }

    private void save() {
        if (rule == null) {
            return;
        }
        state.settings.ruleTextureOverrides.put(ruleId, textureBox.getValue());
        putDouble(scaleBox, state.settings.ruleScaleOverrides);
        putDouble(depthBox, state.settings.ruleDepthOverrides);
        putDouble(xScaleBox, state.settings.ruleXScaleOverrides);
        putDouble(yScaleBox, state.settings.ruleYScaleOverrides);
        putDouble(zScaleBox, state.settings.ruleZScaleOverrides);
        state.settings.save();
        this.minecraft.setScreen(null);
    }

    private void putDouble(EditBox box, java.util.Map<String, Double> target) {
        try {
            target.put(ruleId, Double.parseDouble(box.getValue()));
        } catch (NumberFormatException ignored) {
        }
    }

    private void clearAllOverrides() {
        state.settings.ruleTextureOverrides.remove(ruleId);
        state.settings.ruleScaleOverrides.remove(ruleId);
        state.settings.ruleDepthOverrides.remove(ruleId);
        state.settings.ruleXScaleOverrides.remove(ruleId);
        state.settings.ruleYScaleOverrides.remove(ruleId);
        state.settings.ruleZScaleOverrides.remove(ruleId);
        state.settings.ruleHandheldOverrides.remove(ruleId);
        state.settings.ruleFoilOverrides.remove(ruleId);
        state.settings.save();
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 20, 0xFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
