package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.Json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Server-pushed HUD text and texture entries. */
public final class HudEffectManager {
    private final List<HudText> texts = new ArrayList<>();
    private final List<HudTexture> textures = new ArrayList<>();
    private final Map<String, HudText> textById = new LinkedHashMap<>();
    private final Map<String, HudTexture> textureById = new LinkedHashMap<>();

    public void onPacket(String id, String json) {
        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map<?, ?> map)) {
            return;
        }
        if ("pickup".equals(id) && !PixcoreClientState.INSTANCE.settings.pickupHudEnabled) {
            return;
        }
        if ("pickup".equals(id)) {
            Map<String, Object> adjusted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                adjusted.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            adjusted.put("x", -PixcoreClientState.INSTANCE.settings.pickupHudRightMargin);
            adjusted.put("y", -PixcoreClientState.INSTANCE.settings.pickupHudBottomMargin);
            map = adjusted;
        }
        String type = str(map.get("type"), "text");
        removeById(id);
        if ("texture".equals(type)) {
            HudTexture texture = HudTexture.fromMap(id, map);
            textures.add(texture);
            textureById.put(id, texture);
        } else {
            HudText text = HudText.fromMap(id, map);
            texts.add(text);
            textById.put(id, text);
        }
    }

    public void clear(String id) {
        removeById(id);
    }

    public void tick() {
        texts.removeIf(text -> {
            boolean expired = text.isExpired();
            if (expired) {
                textById.remove(text.id);
            }
            return expired;
        });
        textures.removeIf(texture -> {
            boolean expired = texture.isExpired();
            if (expired) {
                textureById.remove(texture.id);
            }
            return expired;
        });
    }

    private void removeById(String id) {
        HudText text = textById.remove(id);
        if (text != null) {
            texts.remove(text);
        }
        HudTexture texture = textureById.remove(id);
        if (texture != null) {
            textures.remove(texture);
        }
    }

    public void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        for (HudText text : texts) {
            text.render(guiGraphics, mc);
        }
        for (HudTexture texture : textures) {
            texture.render(guiGraphics);
        }
    }

    public void clearAll() {
        texts.clear();
        textures.clear();
        textById.clear();
        textureById.clear();
    }

    private static String str(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }

    private static int num(Object o, int def) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static float num(Object o, float def) {
        if (o instanceof Number n) {
            return n.floatValue();
        }
        if (o instanceof String s) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static double num(Object o, double def) {
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        if (o instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static final class HudText {
        final String id;
        final List<String> lines;
        final String anchor;
        final int x;
        final int y;
        final int color;
        final float scale;
        final boolean shadow;
        final int durationTicks;
        int age;

        HudText(String id, List<String> lines, String anchor, int x, int y, int color,
                float scale, boolean shadow, int durationTicks) {
            this.id = id;
            this.lines = lines;
            this.anchor = anchor;
            this.x = x;
            this.y = y;
            this.color = color;
            this.scale = scale;
            this.shadow = shadow;
            this.durationTicks = durationTicks;
        }

        static HudText fromMap(String id, Map<?, ?> map) {
            List<String> lines = new ArrayList<>();
            Object textObj = map.get("text");
            if (textObj instanceof List<?> list) {
                for (Object o : list) {
                    lines.add(String.valueOf(o));
                }
            } else if (textObj != null) {
                lines.add(String.valueOf(textObj));
            }
            return new HudText(id, lines,
                    str(map.get("anchor"), "top-center"),
                    num(map.get("x"), 0),
                    num(map.get("y"), 10),
                    num(map.get("argb"), -256),
                    num(map.get("scale"), 1.0F),
                    Boolean.TRUE.equals(map.get("shadow")),
                    num(map.get("duration-ticks"), 100));
        }

        boolean isExpired() {
            if (durationTicks <= 0) {
                return false;
            }
            return ++age >= durationTicks;
        }

        void render(GuiGraphics guiGraphics, Minecraft mc) {
            if (lines.isEmpty()) {
                return;
            }
            int lineHeight = (int) (9 * scale);
            int width = 0;
            for (String line : lines) {
                width = Math.max(width, (int) (mc.font.width(line) * scale));
            }
            int totalHeight = lineHeight * lines.size();
            int screenW = guiGraphics.guiWidth();
            int screenH = guiGraphics.guiHeight();
            int px = anchorX(anchor, screenW, width, x);
            int py = anchorY(anchor, screenH, totalHeight, y);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int lineX = px + (width - (int) (mc.font.width(line) * scale)) / 2;
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(lineX, py + i * lineHeight);
                guiGraphics.pose().scale(scale, scale);
                guiGraphics.drawString(mc.font, line, 0, 0, color, shadow);
                guiGraphics.pose().popMatrix();
            }
        }
    }

    private static final class HudTexture {
        final String id;
        final ResourceLocation texture;
        final String anchor;
        final int x;
        final int y;
        final int width;
        final int height;
        final float alpha;
        final int durationTicks;
        int age;

        HudTexture(String id, ResourceLocation texture, String anchor, int x, int y,
                   int width, int height, float alpha, int durationTicks) {
            this.id = id;
            this.texture = texture;
            this.anchor = anchor;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.alpha = alpha;
            this.durationTicks = durationTicks;
        }

        static HudTexture fromMap(String id, Map<?, ?> map) {
            String path = str(map.get("texture"), "");
            ResourceLocation loc = ImageCache.INSTANCE.getOrLoad(path);
            double scale = num(map.get("scale"), 1.0);
            int width = Math.max(1, (int) Math.round(num(map.get("width"), 64) * scale));
            int height = Math.max(1, (int) Math.round(num(map.get("height"), 32) * scale));
            return new HudTexture(id, loc,
                    str(map.get("anchor"), "top-center"),
                    num(map.get("x"), 0),
                    num(map.get("y"), 0),
                    width,
                    height,
                    num(map.get("alpha"), 1.0F),
                    num(map.get("duration-ticks"), 200));
        }

        boolean isExpired() {
            if (durationTicks <= 0) {
                return false;
            }
            return ++age >= durationTicks;
        }

        void render(GuiGraphics guiGraphics) {
            if (texture == null) {
                return;
            }
            int screenW = guiGraphics.guiWidth();
            int screenH = guiGraphics.guiHeight();
            int px = anchorX(anchor, screenW, width, x);
            int py = anchorY(anchor, screenH, height, y);
            int alpha8 = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
            if (alpha8 == 0) {
                return;
            }
            int color = ARGB.color(alpha8, 255, 255, 255);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, px, py,
                    0.0F, 0.0F, width, height, width, height, color);
        }
    }

    private static int anchorX(String anchor, int screenW, int width, int offset) {
        return switch (anchor) {
            case "top-left", "center-left", "bottom-left" -> offset;
            case "top-center", "center", "bottom-center" -> (screenW - width) / 2 + offset;
            case "top-right", "center-right", "bottom-right" -> screenW - width + offset;
            default -> (screenW - width) / 2 + offset;
        };
    }

    private static int anchorY(String anchor, int screenH, int height, int offset) {
        return switch (anchor) {
            case "top-left", "top-center", "top-right" -> offset;
            case "center-left", "center", "center-right" -> (screenH - height) / 2 + offset;
            case "bottom-left", "bottom-center", "bottom-right" -> screenH - height + offset;
            default -> (screenH - height) / 2 + offset;
        };
    }
}
