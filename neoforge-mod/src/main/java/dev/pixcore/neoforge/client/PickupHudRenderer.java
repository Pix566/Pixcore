package dev.pixcore.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Renders pickup notifications with item icons, merging and fade-out. */
public final class PickupHudRenderer {
    private static final int MAX_AGE = 100;
    private static final int FADE_START = 60;
    private static final int ROW_HEIGHT = 20;

    private final List<PickupEntry> entries = new ArrayList<>();

    public void add(ItemStack stack) {
        if (stack.isEmpty() || !PixcoreClientState.INSTANCE.settings.pickupHudEnabled) {
            return;
        }
        String key = stack.getItem() + "|" + stack.getHoverName().getString();
        for (PickupEntry entry : entries) {
            if (entry.key.equals(key)) {
                entry.count += stack.getCount();
                entry.age = 0;
                return;
            }
        }
        entries.add(new PickupEntry(key, stack.copy(), stack.getCount()));
    }

    public void tick() {
        Iterator<PickupEntry> it = entries.iterator();
        while (it.hasNext()) {
            PickupEntry entry = it.next();
            entry.age++;
            if (entry.age >= MAX_AGE) {
                it.remove();
            }
        }
    }

    public void render(GuiGraphics guiGraphics) {
        if (entries.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientSettings settings = PixcoreClientState.INSTANCE.settings;
        int screenW = guiGraphics.guiWidth();
        int screenH = guiGraphics.guiHeight();
        int index = 0;
        for (int i = entries.size() - 1; i >= 0; i--) {
            PickupEntry entry = entries.get(i);
            float alpha = entry.alpha();
            if (alpha <= 0.0F) {
                continue;
            }
            String text = entry.stack.getHoverName().getString() + " x" + entry.count;
            int textWidth = mc.font.width(text);
            int rowWidth = 18 + 4 + textWidth;
            int x = screenW - settings.pickupHudRightMargin - rowWidth;
            int y = screenH - settings.pickupHudBottomMargin - (index + 1) * ROW_HEIGHT;
            guiGraphics.renderItem(entry.stack, x, y);
            guiGraphics.renderItemDecorations(mc.font, entry.stack, x, y);
            guiGraphics.drawString(mc.font, text, x + 18, y + 5, 0xFFFFFFFF, true);
            int fadeAlpha = (int) ((1.0F - alpha) * 255.0F);
            if (fadeAlpha > 0) {
                guiGraphics.fill(x, y, x + rowWidth, y + 16, fadeAlpha << 24);
            }
            index++;
        }
    }

    public void clear() {
        entries.clear();
    }

    private static final class PickupEntry {
        final String key;
        final ItemStack stack;
        int count;
        int age;

        PickupEntry(String key, ItemStack stack, int count) {
            this.key = key;
            this.stack = stack;
            this.count = count;
        }

        float alpha() {
            if (age < FADE_START) {
                return 1.0F;
            }
            return Math.max(0.0F, 1.0F - (float) (age - FADE_START) / (MAX_AGE - FADE_START));
        }
    }
}
