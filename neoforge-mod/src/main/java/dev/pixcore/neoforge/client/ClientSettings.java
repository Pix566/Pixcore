package dev.pixcore.neoforge.client;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Client-side toggle settings, persisted under config/pixcore-client.properties. */
public final class ClientSettings {
    public boolean damageEnabled = true;
    public boolean healingEnabled = true;
    public boolean monsterHealthEnabled = true;
    public boolean pickupHudEnabled = true;
    public int pickupHudRightMargin = 36;
    public int pickupHudBottomMargin = 48;
    public final Set<String> disabledRules = new HashSet<>();
    public final Map<String, Double> ruleScaleOverrides = new HashMap<>();
    public final Map<String, String> ruleTextureOverrides = new HashMap<>();
    public final Map<String, Double> ruleDepthOverrides = new HashMap<>();
    public final Map<String, Double> ruleXScaleOverrides = new HashMap<>();
    public final Map<String, Double> ruleYScaleOverrides = new HashMap<>();
    public final Map<String, Double> ruleZScaleOverrides = new HashMap<>();
    public final Map<String, Boolean> ruleHandheldOverrides = new HashMap<>();
    public final Map<String, Boolean> ruleFoilOverrides = new HashMap<>();

    public ClientSettings() {
    }

    private Path file() {
        return FMLPaths.CONFIGDIR.get()
                .resolve("pixcore-client.properties");
    }

    public void load() {
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
            damageEnabled = Boolean.parseBoolean(props.getProperty("damageEnabled", "true"));
            healingEnabled = Boolean.parseBoolean(props.getProperty("healingEnabled", "true"));
            monsterHealthEnabled = Boolean.parseBoolean(props.getProperty("monsterHealthEnabled", "true"));
            pickupHudEnabled = Boolean.parseBoolean(props.getProperty("pickupHudEnabled", "true"));
            pickupHudRightMargin = Integer.parseInt(props.getProperty("pickupHudRightMargin", "36"));
            pickupHudBottomMargin = Integer.parseInt(props.getProperty("pickupHudBottomMargin", "48"));
            disabledRules.clear();
            String disabled = props.getProperty("disabledRules", "");
            if (!disabled.isEmpty()) {
                for (String id : disabled.split(",")) {
                    if (!id.isBlank()) {
                        disabledRules.add(id.trim());
                    }
                }
            }
            ruleScaleOverrides.clear();
            String scales = props.getProperty("ruleScaleOverrides", "");
            if (!scales.isEmpty()) {
                for (String pair : scales.split(",")) {
                    String[] parts = pair.split("=");
                    if (parts.length == 2) {
                        try {
                            ruleScaleOverrides.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            ruleTextureOverrides.clear();
            String textures = props.getProperty("ruleTextureOverrides", "");
            if (!textures.isEmpty()) {
                for (String pair : textures.split(",")) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2) {
                        ruleTextureOverrides.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
            ruleDepthOverrides.clear();
            ruleXScaleOverrides.clear();
            ruleYScaleOverrides.clear();
            ruleZScaleOverrides.clear();
            loadDoubleMap(props, "ruleDepthOverrides", ruleDepthOverrides);
            loadDoubleMap(props, "ruleXScaleOverrides", ruleXScaleOverrides);
            loadDoubleMap(props, "ruleYScaleOverrides", ruleYScaleOverrides);
            loadDoubleMap(props, "ruleZScaleOverrides", ruleZScaleOverrides);
            ruleHandheldOverrides.clear();
            ruleFoilOverrides.clear();
            loadBooleanMap(props, "ruleHandheldOverrides", ruleHandheldOverrides);
            loadBooleanMap(props, "ruleFoilOverrides", ruleFoilOverrides);
        } catch (IOException | NumberFormatException ignored) {
            // keep defaults on any parse failure
        }
    }

    public void save() {
        try {
            Path file = file();
            Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("damageEnabled", Boolean.toString(damageEnabled));
            props.setProperty("healingEnabled", Boolean.toString(healingEnabled));
            props.setProperty("monsterHealthEnabled", Boolean.toString(monsterHealthEnabled));
            props.setProperty("pickupHudEnabled", Boolean.toString(pickupHudEnabled));
            props.setProperty("pickupHudRightMargin", Integer.toString(pickupHudRightMargin));
            props.setProperty("pickupHudBottomMargin", Integer.toString(pickupHudBottomMargin));
            props.setProperty("disabledRules", String.join(",", disabledRules));
            StringBuilder scaleBuilder = new StringBuilder();
            for (Map.Entry<String, Double> entry : ruleScaleOverrides.entrySet()) {
                if (scaleBuilder.length() > 0) {
                    scaleBuilder.append(',');
                }
                scaleBuilder.append(entry.getKey()).append('=').append(entry.getValue());
            }
            props.setProperty("ruleScaleOverrides", scaleBuilder.toString());
            putStringMap(props, "ruleTextureOverrides", ruleTextureOverrides);
            putDoubleMap(props, "ruleDepthOverrides", ruleDepthOverrides);
            putDoubleMap(props, "ruleXScaleOverrides", ruleXScaleOverrides);
            putDoubleMap(props, "ruleYScaleOverrides", ruleYScaleOverrides);
            putDoubleMap(props, "ruleZScaleOverrides", ruleZScaleOverrides);
            putBooleanMap(props, "ruleHandheldOverrides", ruleHandheldOverrides);
            putBooleanMap(props, "ruleFoilOverrides", ruleFoilOverrides);
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Pixcore client settings");
            }
        } catch (IOException ignored) {
        }
    }

    private static void loadDoubleMap(Properties props, String key, Map<String, Double> target) {
        String value = props.getProperty(key, "");
        if (value.isEmpty()) {
            return;
        }
        for (String pair : value.split(",")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                try {
                    target.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private static void loadBooleanMap(Properties props, String key, Map<String, Boolean> target) {
        String value = props.getProperty(key, "");
        if (value.isEmpty()) {
            return;
        }
        for (String pair : value.split(",")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                target.put(parts[0].trim(), Boolean.parseBoolean(parts[1].trim()));
            }
        }
    }

    private static void putStringMap(Properties props, String key, Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        props.setProperty(key, builder.toString());
    }

    private static void putDoubleMap(Properties props, String key, Map<String, Double> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        props.setProperty(key, builder.toString());
    }

    private static void putBooleanMap(Properties props, String key, Map<String, Boolean> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        props.setProperty(key, builder.toString());
    }
}
