package dev.pixcore.neoforge.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

/** Client-side monster appearance rule. */
public final class MonsterRule {
    private final String id;
    private final String entityId;
    private final String texture;
    private final double scale;
    private final Integer color;

    private MonsterRule(String id, String entityId, String texture, double scale, Integer color) {
        this.id = id;
        this.entityId = entityId;
        this.texture = texture;
        this.scale = scale;
        this.color = color;
    }

    public static MonsterRule fromMap(String id, Map<?, ?> map) {
        String entityId = str(map.get("entity"), "");
        String texture = str(map.get("texture"), "");
        double scale = num(map.get("scale"), 1.0);
        Integer color = parseColor(map.get("color"));
        return new MonsterRule(id, entityId, texture, scale, color);
    }

    public boolean matches(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            return false;
        }
        String wanted = entityId.contains(":") ? entityId : "minecraft:" + entityId;
        return key.toString().equals(wanted);
    }

    public String id() {
        return id;
    }

    public String texture() {
        return texture;
    }

    public double scale() {
        return scale;
    }

    public Integer color() {
        return color;
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
}
