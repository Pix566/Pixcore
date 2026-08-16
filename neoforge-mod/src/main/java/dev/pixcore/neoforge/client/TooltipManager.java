package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.Json;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Client-side tooltip enhancements driven by {@code TooltipRulesPacket}. */
public final class TooltipManager {
    private List<TooltipRule> rules = new ArrayList<>();

    public void onPacket(String json) {
        List<TooltipRule> parsed = new ArrayList<>();
        Object root = Json.parse(json);
        if (root instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map<?, ?> ruleMap) {
                    parsed.add(TooltipRule.fromMap(String.valueOf(entry.getKey()), ruleMap));
                }
            }
            parsed.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
        }
        rules = parsed;
    }

    public void clearAll() {
        rules = new ArrayList<>();
    }

    public List<TooltipRule> rules() {
        return rules;
    }

    public void onTooltip(ItemTooltipEvent event) {
        if (rules.isEmpty() || event.getEntity() == null) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        for (TooltipRule rule : rules) {
            if (PixcoreClientState.INSTANCE.isRuleDisabled(rule.id()) || !rule.matches(stack)) {
                continue;
            }
            List<Component> lines = rule.components();
            if ("replace".equalsIgnoreCase(rule.operation())) {
                tooltip.clear();
                tooltip.addAll(lines);
            } else if ("prepend".equalsIgnoreCase(rule.operation())) {
                tooltip.addAll(0, lines);
            } else {
                tooltip.addAll(lines);
            }
            if (rule.firstMatchOnly()) {
                break;
            }
        }
    }
}
