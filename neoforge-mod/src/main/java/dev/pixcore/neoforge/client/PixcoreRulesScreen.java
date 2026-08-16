package dev.pixcore.neoforge.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Client-side rule viewer/editor: view and toggle individual Pixcore rules. */
public final class PixcoreRulesScreen extends Screen {
    private static final int MAX_VISIBLE = 24;
    private final List<String> lines = new ArrayList<>();
    private final List<String> ruleIds = new ArrayList<>();

    public PixcoreRulesScreen() {
        super(Component.literal("Pixcore Rules"));
        buildLines();
    }

    private void buildLines() {
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        lines.add("图标规则 (" + state.iconRules().size() + "):");
        for (IconRule rule : state.iconRules()) {
            lines.add("  " + rule.id() + " p=" + rule.priority() + " tex=" + rule.texture());
            ruleIds.add("icon:" + rule.id());
        }
        lines.add("盔甲规则 (" + state.armorRules().size() + "):");
        for (IconRule rule : state.armorRules()) {
            lines.add("  " + rule.id() + " p=" + rule.priority() + " tex=" + rule.texture());
            ruleIds.add("armor:" + rule.id());
        }
        lines.add("Tooltip 规则 (" + state.tooltips.rules().size() + "):");
        for (TooltipRule rule : state.tooltips.rules()) {
            lines.add("  " + rule.id() + " p=" + rule.priority() + " op=" + rule.operation());
            ruleIds.add("tooltip:" + rule.id());
        }
    }

    @Override
    protected void init() {
        PixcoreClientState state = PixcoreClientState.INSTANCE;
        int y = 30;
        int shown = 0;
        for (String id : ruleIds) {
            if (shown >= MAX_VISIBLE) {
                break;
            }
            String plainId = id.substring(id.indexOf(':') + 1);
            boolean disabled = state.settings.disabledRules.contains(plainId);
            Button button = Button.builder(Component.literal((disabled ? "[启用] " : "[禁用] ") + plainId), btn -> {
                if (state.settings.disabledRules.contains(plainId)) {
                    state.settings.disabledRules.remove(plainId);
                } else {
                    state.settings.disabledRules.add(plainId);
                }
                state.settings.save();
                btn.setMessage(Component.literal((state.settings.disabledRules.contains(plainId) ? "[启用] " : "[禁用] ") + plainId));
            }).bounds(20, y, 220, 18).build();
            addRenderableWidget(button);
            y += 20;
            shown++;
        }
        if (ruleIds.size() > MAX_VISIBLE) {
            lines.add("... 仅显示前 " + MAX_VISIBLE + " 条");
        }

        addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> this.minecraft.setScreen(null))
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, 0xFFFFFF, true);
        int y = 30 + Math.min(ruleIds.size(), MAX_VISIBLE) * 20;
        for (String line : lines) {
            if (line.startsWith("  ")) {
                continue;
            }
            if (y > this.height - 40) {
                break;
            }
            guiGraphics.drawString(this.font, line, 260, y, 0xE0E0E0, false);
            y += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
