package dev.pixcore.neoforge.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

/**
 * Simple client settings screen for Pixcore HUD toggles and pickup HUD margins.
 */
public final class PixcoreSettingsScreen extends Screen {
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 20;

    private final ClientSettings settings;
    private Button damageButton;
    private Button healingButton;
    private Button monsterHealthButton;
    private Button pickupHudButton;
    private Button dragModeButton;
    private ExtendedSlider rightMarginSlider;
    private ExtendedSlider bottomMarginSlider;
    private boolean dragMode;

    public PixcoreSettingsScreen() {
        super(Component.literal("Pixcore Settings"));
        this.settings = PixcoreClientState.INSTANCE.settings;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        damageButton = addToggleButton(centerX, y, "战斗伤害数字", settings.damageEnabled);
        y += 24;
        healingButton = addToggleButton(centerX, y, "治疗数字", settings.healingEnabled);
        y += 24;
        monsterHealthButton = addToggleButton(centerX, y, "怪物血条", settings.monsterHealthEnabled);
        y += 24;
        pickupHudButton = addToggleButton(centerX, y, "拾取 HUD 通知", settings.pickupHudEnabled);
        y += 32;

        rightMarginSlider = new ExtendedSlider(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("右间距"), Component.literal("px"),
                0.0D, 200.0D, settings.pickupHudRightMargin, true);
        addRenderableWidget(rightMarginSlider);
        y += 24;

        bottomMarginSlider = new ExtendedSlider(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("下间距"), Component.literal("px"),
                0.0D, 200.0D, settings.pickupHudBottomMargin, true);
        addRenderableWidget(bottomMarginSlider);
        y += 24;

        dragModeButton = addRenderableWidget(Button.builder(Component.literal("拖动模式: 关"), btn -> {
            dragMode = !dragMode;
            btn.setMessage(Component.literal("拖动模式: " + (dragMode ? "开" : "关")));
        }).bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += 32;

        addRenderableWidget(Button.builder(Component.literal("查看已加载规则"), btn ->
                        this.minecraft.setScreen(new PixcoreRulesScreen()))
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("保存并关闭"), btn -> saveAndClose())
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 20, 0xFFFFFF, true);
        if (dragMode) {
            guiGraphics.drawString(this.font, "拖动模式：按住左键移动鼠标，拾取 HUD 会跟随鼠标位置",
                    this.width / 2 - 160, this.height - 30, 0xFFFFAA00, true);
        }
        if (settings.pickupHudEnabled) {
            int boxW = 120;
            int boxH = 30;
            int boxX = this.width - settings.pickupHudRightMargin - boxW;
            int boxY = this.height - settings.pickupHudBottomMargin - boxH;
            guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x40FFFFFF);
            guiGraphics.drawString(this.font, "拾取 HUD", boxX + 4, boxY + 4, 0xFFFFFFFF, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Button addToggleButton(int centerX, int y, String label, boolean enabled) {
        Button button = Button.builder(Component.literal(statusLabel(label, enabled)), btn -> {
            boolean current = currentValue(label);
            setValue(label, !current);
            btn.setMessage(Component.literal(statusLabel(label, !current)));
        }).bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        return addRenderableWidget(button);
    }

    private boolean currentValue(String label) {
        return switch (label) {
            case "战斗伤害数字" -> settings.damageEnabled;
            case "治疗数字" -> settings.healingEnabled;
            case "怪物血条" -> settings.monsterHealthEnabled;
            case "拾取 HUD 通知" -> settings.pickupHudEnabled;
            default -> false;
        };
    }

    private void setValue(String label, boolean value) {
        switch (label) {
            case "战斗伤害数字" -> settings.damageEnabled = value;
            case "治疗数字" -> settings.healingEnabled = value;
            case "怪物血条" -> settings.monsterHealthEnabled = value;
            case "拾取 HUD 通知" -> settings.pickupHudEnabled = value;
            default -> {
            }
        }
    }

    private static String statusLabel(String label, boolean enabled) {
        return label + ": " + (enabled ? "开" : "关");
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragMode && button == 0) {
            settings.pickupHudRightMargin = clampMargin(this.width - (int) mouseX);
            settings.pickupHudBottomMargin = clampMargin(this.height - (int) mouseY);
            rightMarginSlider.setValue(settings.pickupHudRightMargin);
            bottomMarginSlider.setValue(settings.pickupHudBottomMargin);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragMode && button == 0) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static int clampMargin(int value) {
        return Math.max(0, Math.min(200, value));
    }

    private void saveAndClose() {
        settings.pickupHudRightMargin = rightMarginSlider.getValueInt();
        settings.pickupHudBottomMargin = bottomMarginSlider.getValueInt();
        settings.save();
        this.minecraft.setScreen(null);
    }
}
