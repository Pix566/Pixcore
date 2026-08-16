package dev.pixcore.neoforge.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Simple client-side viewer for the currently loaded Pixcore rules. */
public final class PixcoreRulesScreen extends Screen {
    private static final int LINE_HEIGHT = 10;
    private final List<String> lines = new ArrayList<>();

    public PixcoreRulesScreen() {
        super(Component.literal("Pixcore Rules"));
        buildLines();
    }

    private void buildLines() {
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        lines.add("图标规则 (" + state.iconRules().size() + "):");
        for (IconRule rule : state.iconRules()) {
            lines.add("  " + rule.id() + " p=" + rule.priority() + " tex=" + rule.texture());
        }
        lines.add("盔甲规则 (" + state.armorRules().size() + "):");
        for (IconRule rule : state.armorRules()) {
            lines.add("  " + rule.id() + " p=" + rule.priority() + " tex=" + rule.texture());
        }
        lines.add("Tooltip 规则 (" + state.tooltips.rules().size() + "):");
        for (TooltipRule rule : state.tooltips.rules()) {
            lines.add("  " + rule.id() + " p=" + rule.priority() + " op=" + rule.operation());
        }
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> this.minecraft.setScreen(null))
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, 0xFFFFFF, true);
        int y = 30;
        for (String line : lines) {
            if (y > this.height - 40) {
                break;
            }
            guiGraphics.drawString(this.font, line, 20, y, 0xE0E0E0, false);
            y += LINE_HEIGHT;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
