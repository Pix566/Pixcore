package dev.pixcore.neoforge.client;

import net.minecraft.resources.ResourceLocation;

/** Render information for a dynamic Pixcore item model. */
public record PixcoreRenderData(
        ResourceLocation texture,
        double scale,
        double depth,
        double xScale,
        double yScale,
        double zScale,
        boolean handheld
) {
    public PixcoreRenderData {
        if (scale <= 0) {
            scale = 1.0;
        }
        if (depth <= 0) {
            depth = 1.0;
        }
        if (xScale <= 0) {
            xScale = 1.0;
        }
        if (yScale <= 0) {
            yScale = 1.0;
        }
        if (zScale <= 0) {
            zScale = 1.0;
        }
    }

    public PixcoreRenderData(ResourceLocation texture, double scale) {
        this(texture, scale, 1.0, 1.0, 1.0, 1.0, false);
    }
}
