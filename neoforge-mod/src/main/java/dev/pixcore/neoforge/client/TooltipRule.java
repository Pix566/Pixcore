package dev.pixcore.neoforge.client;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Server-defined tooltip enhancement rule.
 *
 * <p>Rules can append, prepend or replace the tooltip of matching items.
 * Legacy {@code &}-codes are supported inside lines. Optional color and
 * formatting fields are applied to lines without an explicit {@code &} code.
 */
public final class TooltipRule {
    private final String id;
    private final int priority;
    private final String operation;
    private final ItemMatcher matcher;
    private final List<String> lines;
    private final Integer color;
    private final boolean bold;
    private final boolean italic;
    private final boolean underlined;
    private final boolean strikethrough;
    private final boolean obfuscated;
    private final boolean firstMatchOnly;
    private final String translate;
    private final List<String> componentJsons;

    private TooltipRule(String id, int priority, String operation, ItemMatcher matcher, List<String> lines,
                        Integer color, boolean bold, boolean italic, boolean underlined,
                        boolean strikethrough, boolean obfuscated, boolean firstMatchOnly,
                        String translate, List<String> componentJsons) {
        this.id = id;
        this.priority = priority;
        this.operation = operation;
        this.matcher = matcher;
        this.lines = lines;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.firstMatchOnly = firstMatchOnly;
        this.translate = translate;
        this.componentJsons = componentJsons;
    }

    public static TooltipRule fromMap(String id, Map<?, ?> map) {
        int priority = num(map.get("priority"), 0);
        String operation = str(map.get("operation"), "append");
        Object match = map.get("match");
        ItemMatcher matcher = ItemMatcher.fromMap(match instanceof Map<?, ?> m ? m : null);
        List<String> lines = parseStringList(map.get("lines"));
        Integer color = parseColor(map.get("color"));
        boolean firstMatchOnly = "first".equalsIgnoreCase(str(map.get("combine"), "all"));
        return new TooltipRule(id, priority, operation, matcher, lines,
                color,
                Boolean.TRUE.equals(map.get("bold")),
                Boolean.TRUE.equals(map.get("italic")),
                Boolean.TRUE.equals(map.get("underlined")),
                Boolean.TRUE.equals(map.get("strikethrough")),
                Boolean.TRUE.equals(map.get("obfuscated")),
                firstMatchOnly,
                str(map.get("translate"), null),
                parseStringList(map.get("component-json")));
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

    public String operation() {
        return operation;
    }

    public boolean firstMatchOnly() {
        return firstMatchOnly;
    }

    public List<Component> components() {
        Style baseStyle = Style.EMPTY;
        if (color != null) {
            baseStyle = baseStyle.withColor(color);
        }
        if (bold) {
            baseStyle = baseStyle.withBold(true);
        }
        if (italic) {
            baseStyle = baseStyle.withItalic(true);
        }
        if (underlined) {
            baseStyle = baseStyle.withUnderlined(true);
        }
        if (strikethrough) {
            baseStyle = baseStyle.withStrikethrough(true);
        }
        if (obfuscated) {
            baseStyle = baseStyle.withObfuscated(true);
        }

        List<Component> result = new ArrayList<>();
        for (String line : lines) {
            result.add(legacy(line, baseStyle));
        }
        if (translate != null && !translate.isEmpty()) {
            result.add(Component.translatable(translate).copy().withStyle(baseStyle));
        }
        for (String json : componentJsons) {
            Component component = parseComponentJson(json);
            if (component != null) {
                result.add(component);
            }
        }
        return result;
    }

    private static Component parseComponentJson(String json) {
        try {
            var parsed = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
            return parsed.result().orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Component legacy(String input, Style baseStyle) {
        MutableComponent root = Component.literal("");
        Style style = baseStyle;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < input.length() && isFormatCode(input.charAt(i + 1))) {
                if (current.length() > 0) {
                    root.append(Component.literal(current.toString()).withStyle(style));
                    current.setLength(0);
                }
                char code = input.charAt(++i);
                ChatFormatting formatting = ChatFormatting.getByCode(code);
                if (formatting == ChatFormatting.RESET) {
                    style = baseStyle;
                } else if (formatting != null) {
                    style = style.applyLegacyFormat(formatting);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            root.append(Component.literal(current.toString()).withStyle(style));
        }
        return root;
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

    private static boolean isFormatCode(char c) {
        return "0123456789abcdefklmnor".indexOf(Character.toLowerCase(c)) >= 0;
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

    private static List<String> parseStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof String s) {
            return List.of(s);
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> result = new ArrayList<>();
            for (Object o : iterable) {
                result.add(String.valueOf(o));
            }
            return result;
        }
        return List.of(String.valueOf(value));
    }
}
