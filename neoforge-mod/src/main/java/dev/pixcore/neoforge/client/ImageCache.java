package dev.pixcore.neoforge.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.pixcore.neoforge.PixcoreMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads images from {@code .minecraft/resourcepacks/pixcore/} and registers
 * them as dynamic textures so GUI code can draw them with {@code ResourceLocation}.
 */
public final class ImageCache {
    public static final ImageCache INSTANCE = new ImageCache();
    private static final int MAX_CACHE_SIZE = 128;

    private final Map<String, ResourceLocation> cache = new java.util.LinkedHashMap<>(16, 0.75F, true);

    private ImageCache() {
    }

    public synchronized void clear() {
        for (Map.Entry<String, ResourceLocation> entry : cache.entrySet()) {
            release(entry.getValue());
        }
        cache.clear();
    }

    private static void release(ResourceLocation id) {
        try {
            if (Minecraft.getInstance().getTextureManager() != null) {
                Minecraft.getInstance().getTextureManager().release(id);
            }
        } catch (Exception ignored) {
        }
    }

    public ResourceLocation getOrLoad(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        ResourceLocation cached = cache.get(path);
        if (cached != null) {
            return cached;
        }
        try {
            Path base = FMLPaths.GAMEDIR.get()
                    .resolve("resourcepacks")
                    .resolve("pixcore")
                    .normalize();
            Path file = base.resolve(path).normalize();
            if (!file.startsWith(base) || !Files.isRegularFile(file)) {
                return null;
            }
            try (InputStream in = Files.newInputStream(file)) {
                NativeImage image = NativeImage.read(in);
                String key = "dynamic/" + path.replaceAll("[^A-Za-z0-9._/]", "_");
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(PixcoreMod.MOD_ID, key);
                DynamicTexture texture = new DynamicTexture(() -> "Pixcore " + path, image);
                texture.upload();
                Minecraft.getInstance().getTextureManager().register(id, texture);
                cache.put(path, id);
                evictIfNeeded();
                return id;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void evictIfNeeded() {
        while (cache.size() > MAX_CACHE_SIZE) {
            java.util.Iterator<Map.Entry<String, ResourceLocation>> it = cache.entrySet().iterator();
            Map.Entry<String, ResourceLocation> oldest = it.next();
            release(oldest.getValue());
            it.remove();
        }
    }
}
