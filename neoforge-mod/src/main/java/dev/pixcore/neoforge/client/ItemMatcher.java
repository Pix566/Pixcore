package dev.pixcore.neoforge.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Shared item matching used by icon, armor and tooltip rules.
 *
 * <p>Supports material, exact name, name regex, lore lines, lore regexes and
 * NBT sub-tag matching. A rule without any match condition matches every item.
 */
public final class ItemMatcher {
    private final String material;
    private final String name;
    private final Pattern nameRegex;
    private final List<String> lore;
    private final List<Pattern> loreRegex;
    private final CompoundTag nbt;

    private ItemMatcher(String material, String name, Pattern nameRegex,
                        List<String> lore, List<Pattern> loreRegex, CompoundTag nbt) {
        this.material = material;
        this.name = name;
        this.nameRegex = nameRegex;
        this.lore = lore;
        this.loreRegex = loreRegex;
        this.nbt = nbt;
    }

    public static ItemMatcher fromMap(Map<?, ?> matchMap) {
        if (matchMap == null) {
            return new ItemMatcher(null, null, null, List.of(), List.of(), null);
        }
        String material = str(matchMap.get("material"), null);
        String name = str(matchMap.get("name"), null);
        Pattern nameRegex = null;
        String regex = str(matchMap.get("name-regex"), null);
        if (regex != null) {
            try {
                nameRegex = Pattern.compile(regex);
            } catch (PatternSyntaxException ignored) {
                nameRegex = null;
            }
        }
        List<String> lore = parseStringList(matchMap.get("lore"));
        List<Pattern> loreRegex = parseRegexList(matchMap.get("lore-regex"));
        CompoundTag nbt = null;
        Object nbtValue = matchMap.get("nbt");
        if (nbtValue instanceof Map<?, ?> nbtMap) {
            nbt = toNbt(nbtMap);
        }
        return new ItemMatcher(material, name, nameRegex, lore, loreRegex, nbt);
    }

    public boolean matches(ItemStack stack) {
        if (material != null) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String wanted = material.contains(":") ? material : "minecraft:" + material;
            if (!itemId.equals(wanted)) {
                return false;
            }
        }
        if (name != null && !stack.getHoverName().getString().equals(name)) {
            return false;
        }
        if (nameRegex != null && !nameRegex.matcher(stack.getHoverName().getString()).matches()) {
            return false;
        }
        if (!lore.isEmpty()) {
            List<String> lines = loreLines(stack);
            if (!lines.containsAll(lore)) {
                return false;
            }
        }
        if (!loreRegex.isEmpty()) {
            List<String> lines = loreLines(stack);
            for (Pattern pattern : loreRegex) {
                boolean matched = lines.stream().anyMatch(line -> pattern.matcher(line).matches());
                if (!matched) {
                    return false;
                }
            }
        }
        if (nbt != null && !nbt.isEmpty()) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (!customData.matchedBy(nbt)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasConditions() {
        return material != null || name != null || nameRegex != null
                || !lore.isEmpty() || !loreRegex.isEmpty()
                || (nbt != null && !nbt.isEmpty());
    }

    private static List<String> loreLines(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (net.minecraft.network.chat.Component line : lore.lines()) {
            lines.add(line.getString());
        }
        return lines;
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

    private static List<Pattern> parseRegexList(Object value) {
        if (value == null) {
            return List.of();
        }
        List<String> regexes = parseStringList(value);
        List<Pattern> result = new ArrayList<>();
        for (String regex : regexes) {
            try {
                result.add(Pattern.compile(regex));
            } catch (PatternSyntaxException ignored) {
                // Skip invalid regexes so one bad rule cannot break the whole set.
            }
        }
        return result;
    }

    private static CompoundTag toNbt(Map<?, ?> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            putNbtValue(tag, String.valueOf(entry.getKey()), entry.getValue());
        }
        return tag;
    }

    private static void putNbtValue(CompoundTag tag, String key, Object value) {
        if (value instanceof Map<?, ?> map) {
            tag.put(key, toNbt(map));
        } else if (value instanceof List<?> list) {
            ListTag listTag = new ListTag();
            for (Object element : list) {
                listTag.add(toNbtElement(element));
            }
            tag.put(key, listTag);
        } else if (value instanceof Boolean b) {
            tag.putBoolean(key, b);
        } else if (value instanceof Byte b) {
            tag.putByte(key, b);
        } else if (value instanceof Short s) {
            tag.putShort(key, s);
        } else if (value instanceof Integer i) {
            tag.putInt(key, i);
        } else if (value instanceof Long l) {
            tag.putLong(key, l);
        } else if (value instanceof Float f) {
            tag.putFloat(key, f);
        } else if (value instanceof Number n) {
            tag.putDouble(key, n.doubleValue());
        } else {
            tag.putString(key, String.valueOf(value));
        }
    }

    private static Tag toNbtElement(Object value) {
        if (value instanceof Map<?, ?> map) {
            return toNbt(map);
        }
        if (value instanceof List<?> list) {
            ListTag listTag = new ListTag();
            for (Object element : list) {
                listTag.add(toNbtElement(element));
            }
            return listTag;
        }
        CompoundTag holder = new CompoundTag();
        putNbtValue(holder, "value", value);
        return holder.get("value");
    }

    private static String str(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }
}
