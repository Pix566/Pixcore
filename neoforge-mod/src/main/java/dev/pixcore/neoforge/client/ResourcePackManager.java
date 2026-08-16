package dev.pixcore.neoforge.client;

import dev.pixcore.protocol.ResourcePackChunkPacket;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Receives chunked resource pack files from the server and writes them locally. */
public final class ResourcePackManager {
    private final Map<String, RandomAccessFile> writers = new HashMap<>();

    public void handle(ResourcePackChunkPacket packet) {
        try {
            Path base = FMLPaths.GAMEDIR.get().resolve("resourcepacks").resolve("pixcore").normalize();
            Path file = base.resolve(packet.path()).normalize();
            if (!file.startsWith(base)) {
                return;
            }
            Files.createDirectories(file.getParent());
            RandomAccessFile raf = writers.get(packet.path());
            if (raf == null) {
                raf = new RandomAccessFile(file.toFile(), "rw");
                writers.put(packet.path(), raf);
            }
            raf.seek(packet.offset());
            raf.write(packet.data());
            if (packet.last()) {
                raf.close();
                writers.remove(packet.path());
                ImageCache.INSTANCE.clear();
            }
        } catch (IOException ignored) {
        }
    }

    public void clear() {
        for (RandomAccessFile raf : writers.values()) {
            try {
                raf.close();
            } catch (IOException ignored) {
            }
        }
        writers.clear();
    }
}
