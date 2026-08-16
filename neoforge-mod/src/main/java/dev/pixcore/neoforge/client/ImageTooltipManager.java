package dev.pixcore.neoforge.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Adds image tooltip components to matching rules. */
public final class ImageTooltipManager {
    public void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        for (TooltipRule rule : PixcoreClientState.INSTANCE.tooltips.rules()) {
            if (PixcoreClientState.INSTANCE.isRuleDisabled(rule.id()) || rule.image() == null || !rule.matches(event.getItemStack())) {
                continue;
            }
            ResourceLocation texture = ImageCache.INSTANCE.getOrLoad(rule.image());
            if (texture == null) {
                continue;
            }
            TooltipComponent component = new ImageTooltipComponent(texture, 16, 16);
            event.getTooltipElements().add(Either.right(component));
        }
    }
}
