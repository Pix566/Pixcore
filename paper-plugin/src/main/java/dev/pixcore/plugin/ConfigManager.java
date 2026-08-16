package dev.pixcore.plugin;

import dev.pixcore.protocol.Json;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigManager {
    private static final String[] MODULE_FILES = {
            "modules/icons.yml",
            "modules/hud.yml",
            "modules/tooltip-text.yml",
            "modules/particles.yml",
            "modules/keybinds.yml",
            "modules/armor.yml"
    };

    private final PixcorePlugin plugin;
    private final File modulesDir;

    private Map<String, Object> icons = Map.of();
    private Map<String, Object> hudEntries = Map.of();
    private Map<String, Object> tooltipRules = Map.of();
    private Map<String, Object> particleEntries = Map.of();
    private Map<String, Object> keybindDefinitions = Map.of();
    private Map<String, Object> armorRules = Map.of();

    private int maxIconRules = 512;
    private int maxHudEntries = 64;
    private int maxTooltipRules = 64;
    private int maxParticleEntries = 64;
    private int maxKeybindDefinitions = 64;
    private int maxArmorRules = 64;
    private int maxParticleCount = 128;

    public ConfigManager(PixcorePlugin plugin) {
        this.plugin = plugin;
        this.modulesDir = new File(plugin.getDataFolder(), "modules");
    }

    public void saveDefaults() {
        plugin.saveDefaultConfig();
        if (!modulesDir.exists()) {
            modulesDir.mkdirs();
        }
        File resourcePackDir = new File(plugin.getDataFolder(), "resourcepacks/pixcore");
        if (!resourcePackDir.exists()) {
            resourcePackDir.mkdirs();
        }
        for (String path : MODULE_FILES) {
            plugin.saveResource(path, false);
        }
    }

    public void reload() {
        plugin.reloadConfig();
        maxIconRules = plugin.getConfig().getInt("limits.max-icon-rules", 512);
        maxHudEntries = plugin.getConfig().getInt("limits.max-hud-entries", 64);
        maxTooltipRules = plugin.getConfig().getInt("limits.max-tooltip-rules", 64);
        maxParticleEntries = plugin.getConfig().getInt("limits.max-particle-entries", 64);
        maxKeybindDefinitions = plugin.getConfig().getInt("limits.max-keybind-definitions", 64);
        maxArmorRules = plugin.getConfig().getInt("limits.max-armor-rules", 64);
        maxParticleCount = plugin.getConfig().getInt("limits.max-particle-count", 128);

        icons = limitMap(validateEntries(loadRoot("modules/icons.yml", "icons"), "icons", "texture", "modules/icons.yml"), maxIconRules, "icons");
        hudEntries = limitMap(validateEntries(loadRoot("modules/hud.yml", null), "hud", null, "modules/hud.yml"), maxHudEntries, "hud");
        tooltipRules = limitMap(validateEntries(loadRoot("modules/tooltip-text.yml", null), "tooltip-text", "lines", "modules/tooltip-text.yml"), maxTooltipRules, "tooltip-text");
        particleEntries = limitMap(validateEntries(loadRoot("modules/particles.yml", null), "particles", "particle-id", "modules/particles.yml"), maxParticleEntries, "particles");
        keybindDefinitions = limitMap(validateEntries(loadRoot("modules/keybinds.yml", null), "keybinds", "default-key", "modules/keybinds.yml"), maxKeybindDefinitions, "keybinds");
        armorRules = limitMap(validateEntries(loadRoot("modules/armor.yml", null), "armor", "texture", "modules/armor.yml"), maxArmorRules, "armor");
    }

    public boolean moduleEnabled(String key) {
        return plugin.getConfig().getBoolean("modules." + key, true);
    }

    public String getIconsJson() {
        return Json.write(icons);
    }

    public Map<String, Object> getHudEntries() {
        return hudEntries;
    }

    public String getTooltipJson() {
        return Json.write(tooltipRules);
    }

    public Map<String, Object> getParticleEntries() {
        return particleEntries;
    }

    public String getKeybindsJson() {
        return Json.write(keybindDefinitions);
    }

    public String getArmorJson() {
        return Json.write(armorRules);
    }

    public Object findHudEntry(String id) {
        return hudEntries.get(id);
    }

    public int getMaxParticleCount() {
        return maxParticleCount;
    }

    private Map<String, Object> validateEntries(Map<String, Object> source, String moduleName,
                                                String requiredField, String fileName) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Integer> lines = buildEntryLineMap(fileName);
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Map<?, ?> map)) {
                plugin.getLogger().warning("Pixcore " + moduleName + " entry '" + entry.getKey()
                        + "' is not a map; skipped. (" + fileName + ":" + lines.getOrDefault(entry.getKey(), -1) + ")");
                continue;
            }
            if (requiredField != null) {
                Object field = map.get(requiredField);
                if (field == null || (field instanceof String s && s.isEmpty())) {
                    plugin.getLogger().warning("Pixcore " + moduleName + " entry '" + entry.getKey()
                            + "' is missing required field '" + requiredField + "'; skipped. ("
                            + fileName + ":" + lines.getOrDefault(entry.getKey(), -1) + ")");
                    continue;
                }
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }

    private Map<String, Integer> buildEntryLineMap(String fileName) {
        File file = new File(modulesDir, fileName);
        if (!file.exists()) {
            return Map.of();
        }
        Map<String, Integer> result = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            Node root = new Yaml().compose(reader);
            if (root instanceof MappingNode mapping) {
                for (NodeTuple tuple : mapping.getValue()) {
                    Node keyNode = tuple.getKeyNode();
                    if (keyNode instanceof ScalarNode scalar) {
                        result.put(scalar.getValue(), keyNode.getStartMark().getLine() + 1);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private Map<String, Object> limitMap(Map<String, Object> source, int max, String name) {
        if (source.size() <= max) {
            return source;
        }
        plugin.getLogger().warning("Pixcore " + name + " config has " + source.size()
                + " entries, but limit is " + max + "; keeping the first " + max + ".");
        Map<String, Object> limited = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (count++ >= max) {
                break;
            }
            limited.put(entry.getKey(), entry.getValue());
        }
        return limited;
    }

    private Map<String, Object> loadRoot(String fileName, String root) {
        File file = new File(modulesDir, fileName);
        if (!file.exists()) {
            return Map.of();
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = root == null ? yaml : yaml.getConfigurationSection(root);
        if (section == null) {
            return Map.of();
        }
        return sectionToMap(section);
    }

    private Map<String, Object> sectionToMap(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection child) {
                result.put(key, sectionToMap(child));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
}
