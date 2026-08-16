package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.Json;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client-side item image rule.
 *
 * <p>The first matching rule with the highest priority wins. The server sends
 * rules as JSON; the client never sends image files back.
 */
public final class IconRule {
    private final String id;
    private final int priority;
    private final ItemMatcher matcher;
    private final String texture;
    private final String innerTexture;
    private final String outerTexture;
    private final String guiTexture;
    private final String handTexture;
    private final String groundTexture;
    private final String headTexture;
    private final String chestTexture;
    private final String legsTexture;
    private final String feetTexture;
    private final double scale;
    private final double depth;
    private final double xScale;
    private final double yScale;
    private final double zScale;
    private final boolean handheld;
    private final boolean foil;
    private final Integer color;
    private final Integer pulseColor;
    private final double pulseSpeed;
    private final boolean modelAnim;
    private final double modelAnimSpeed;

    private IconRule(String id, int priority, ItemMatcher matcher,
                     String texture, String innerTexture, String outerTexture,
                     String guiTexture, String handTexture, String groundTexture,
                     String headTexture, String chestTexture, String legsTexture, String feetTexture,
                     double scale, double depth, double xScale, double yScale, double zScale,
                     boolean handheld, boolean foil, Integer color, Integer pulseColor, double pulseSpeed,
                     boolean modelAnim, double modelAnimSpeed) {
        this.id = id;
        this.priority = priority;
        this.matcher = matcher;
        this.texture = texture;
        this.innerTexture = innerTexture;
        this.outerTexture = outerTexture;
        this.guiTexture = guiTexture;
        this.handTexture = handTexture;
        this.groundTexture = groundTexture;
        this.headTexture = headTexture;
        this.chestTexture = chestTexture;
        this.legsTexture = legsTexture;
        this.feetTexture = feetTexture;
        this.scale = scale;
        this.depth = depth;
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
        this.handheld = handheld;
        this.foil = foil;
        this.color = color;
        this.pulseColor = pulseColor;
        this.pulseSpeed = pulseSpeed;
        this.modelAnim = modelAnim;
        this.modelAnimSpeed = modelAnimSpeed;
    }

    public static IconRule fromMap(String id, Map<?, ?> map) {
        int priority = num(map.get("priority"), 0);
        String texture = str(map.get("texture"), "");
        String innerTexture = str(map.get("inner-texture"), texture);
        String outerTexture = str(map.get("outer-texture"), texture);
        String guiTexture = str(map.get("texture-gui"), texture);
        String handTexture = str(map.get("texture-hand"), texture);
        String groundTexture = str(map.get("texture-ground"), texture);
        String headTexture = str(map.get("head-texture"), outerTexture);
        String chestTexture = str(map.get("chest-texture"), outerTexture);
        String legsTexture = str(map.get("legs-texture"), innerTexture);
        String feetTexture = str(map.get("feet-texture"), outerTexture);
        double scale = num(map.get("scale"), 1.0);
        double depth = num(map.get("depth"), 1.0);
        double xScale = num(map.get("x-scale"), 1.0);
        double yScale = num(map.get("y-scale"), 1.0);
        double zScale = num(map.get("z-scale"), 1.0);
        boolean handheld = Boolean.TRUE.equals(map.get("handheld"));
        boolean foil = Boolean.TRUE.equals(map.get("foil"));
        Integer color = parseColor(map.get("color"));
        Integer pulseColor = parseColor(map.get("pulse-color"));
        double pulseSpeed = num(map.get("pulse-speed"), 1.0);
        boolean modelAnim = Boolean.TRUE.equals(map.get("model-anim"));
        double modelAnimSpeed = num(map.get("model-anim-speed"), 1.0);

        Object match = map.get("match");
        ItemMatcher matcher = ItemMatcher.fromMap(match instanceof Map<?, ?> m ? m : null);
        return new IconRule(id, priority, matcher, texture, innerTexture, outerTexture,
                guiTexture, handTexture, groundTexture,
                headTexture, chestTexture, legsTexture, feetTexture,
                scale, depth, xScale, yScale, zScale, handheld, foil, color, pulseColor, pulseSpeed,
                modelAnim, modelAnimSpeed);
    }

    public boolean matches(ItemStack stack) {
        return matcher.matches(stack);
    }

    public String id() {
        return id;
    }

    public int priority() {
        return priority;
    }

    public String texture() {
        return texture;
    }

    public String innerTexture() {
        return innerTexture;
    }

    public String outerTexture() {
        return outerTexture;
    }

    public String textureFor(ItemDisplayContext context) {
        return switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND,
                    THIRD_PERSON_RIGHT_HAND -> handTexture;
            case GROUND -> groundTexture;
            case GUI, HEAD -> guiTexture;
            default -> texture;
        };
    }

    public String textureForSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> headTexture;
            case CHEST -> chestTexture;
            case LEGS -> legsTexture;
            case FEET -> feetTexture;
            default -> outerTexture;
        };
    }

    public double scale() {
        return scale;
    }

    public double depth() {
        return depth;
    }

    public double xScale() {
        return xScale;
    }

    public double yScale() {
        return yScale;
    }

    public double zScale() {
        return zScale;
    }

    public boolean handheld() {
        return handheld;
    }

    public boolean foil() {
        return foil;
    }

    public Integer color() {
        return color;
    }

    public Integer pulseColor() {
        return pulseColor;
    }

    public double pulseSpeed() {
        return pulseSpeed;
    }

    public boolean modelAnim() {
        return modelAnim;
    }

    public double modelAnimSpeed() {
        return modelAnimSpeed;
    }

    private static Integer parseColor(Object o) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o instanceof String s) {
            try {
                if (s.startsWith("#")) {
                    return Integer.parseUnsignedInt(s.substring(1), 16) | 0xFF000000;
                }
                return Integer.parseUnsignedInt(s, 16) | 0xFF000000;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
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

    /** Parses the JSON object sent by {@code IconRulesPacket}. */
    public static List<IconRule> parseAll(String json) {
        Object root = Json.parse(json);
        List<IconRule> rules = new ArrayList<>();
        if (root instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getValue() instanceof Map<?, ?> ruleMap) {
                    rules.add(fromMap(String.valueOf(e.getKey()), ruleMap));
                }
            }
            rules.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
        }
        return rules;
    }
}
