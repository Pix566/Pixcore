package dev.pixcore.neoforge.client;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
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
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Pixcore client settings");
            }
        } catch (IOException ignored) {
        }
    }
}
