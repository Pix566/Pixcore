package dev.pixcore.plugin;

import dev.pixcore.protocol.PixcoreProtocol;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side record of a client's negotiated Pixcore capabilities. */
public final class PlayerSession {
    private final int capabilities;
    private final String clientName;
    private final Map<String, String> sentHashes = new ConcurrentHashMap<>();
    private final Map<String, String> resourcePackHashes = new ConcurrentHashMap<>();

    public PlayerSession(int capabilities, String clientName) {
        this.capabilities = capabilities;
        this.clientName = clientName;
    }

    /** Returns true if the given module payload should be sent because it changed. */
    public boolean shouldSend(String module, String payload) {
        String hash = sha256(payload.getBytes(StandardCharsets.UTF_8));
        String previous = sentHashes.get(module);
        if (hash.equals(previous)) {
            return false;
        }
        sentHashes.put(module, hash);
        return true;
    }

    /** Returns true if the resource pack file should be sent because its content changed. */
    public boolean shouldSendResourcePack(String path, byte[] content) {
        String hash = sha256(content);
        String previous = resourcePackHashes.get(path);
        if (hash.equals(previous)) {
            return false;
        }
        resourcePackHashes.put(path, hash);
        return true;
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(java.util.Arrays.hashCode(bytes));
        }
    }

    public int capabilities() {
        return capabilities;
    }

    public String clientName() {
        return clientName;
    }

    public boolean has(int cap) {
        return PixcoreProtocol.hasCapability(capabilities, cap);
    }

    @Override
    public String toString() {
        return "PlayerSession[capabilities=" + capabilities + ", client=" + clientName + "]";
    }
}
