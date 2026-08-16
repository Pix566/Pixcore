package dev.pixcore.neoforge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** Client renderer for {@link ImageTooltipComponent}. */
public final class ClientImageTooltipComponent implements ClientTooltipComponent {
    private final ImageTooltipComponent component;

    public ClientImageTooltipComponent(ImageTooltipComponent component) {
        this.component = component;
    }

    @Override
    public int getHeight(Font font) {
        return component.height();
    }

    @Override
    public int getWidth(Font font) {
        return component.width();
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, int width, int height, GuiGraphics guiGraphics) {
        if (component.texture() != null) {
            guiGraphics.blit(component.texture(), mouseX, mouseY, mouseX + component.width(),
                    mouseY + component.height(), 0.0F, 0.0F, 1.0F, 1.0F);
        }
    }
}
