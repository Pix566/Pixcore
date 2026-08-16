package dev.pixcore.neoforge.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Tooltip component that renders a Pixcore image inside item tooltips. */
public record ImageTooltipComponent(ResourceLocation texture, int width, int height) implements TooltipComponent {
}
