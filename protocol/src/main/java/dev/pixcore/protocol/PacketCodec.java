package dev.pixcore.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encodes/decodes {@link Packet} objects to and from a byte array.
 *
 * <p>Layout: {@code compressionFlag:byte | packetId:byte | packet body}. Strings
 * are written as UTF-8 with a 4-byte length prefix so payloads are not limited
 * to {@link DataOutput#writeUTF}'s 64 KiB ceiling. Large packets are
 * transparently gzip-compressed.
 */
public final class PacketCodec {
    private static final int COMPRESS_THRESHOLD = 512;
    private static final int MAX_STRING_BYTES = 16 * 1024 * 1024;

    private PacketCodec() {
    }

    public static byte[] encode(Packet packet) throws IOException {
        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        DataOutputStream bodyOut = new DataOutputStream(bodyBuffer);
        bodyOut.writeByte(packet.getId());
        packet.write(bodyOut);
        bodyOut.flush();
        byte[] body = bodyBuffer.toByteArray();

        if (body.length > COMPRESS_THRESHOLD) {
            byte[] compressed = gzip(body);
            if (compressed.length < body.length) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                result.write(1);
                result.write(compressed);
                return result.toByteArray();
            }
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(0);
        result.write(body);
        return result.toByteArray();
    }

    public static Packet decode(byte[] data) throws IOException {
        DataInputStream headerIn = new DataInputStream(new ByteArrayInputStream(data));
        int compressed = headerIn.readUnsignedByte();
        byte[] payload;
        if (compressed == 1) {
            byte[] compressedBytes = readRemaining(headerIn);
            payload = gunzip(compressedBytes);
        } else {
            payload = readRemaining(headerIn);
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
        int id = in.readUnsignedByte();
        return switch (id) {
            case PixcoreProtocol.ID_HANDSHAKE -> HandshakePacket.read(in);
            case PixcoreProtocol.ID_HANDSHAKE_ACK -> HandshakeAckPacket.read(in);
            case PixcoreProtocol.ID_ICON_RULES -> IconRulesPacket.read(in);
            case PixcoreProtocol.ID_HUD -> HudPacket.read(in);
            case PixcoreProtocol.ID_TOOLTIP_RULES -> TooltipRulesPacket.read(in);
            case PixcoreProtocol.ID_PARTICLE -> ParticlePacket.read(in);
            case PixcoreProtocol.ID_KEYBIND_DEFINITIONS -> KeybindDefinitionsPacket.read(in);
            case PixcoreProtocol.ID_ARMOR_RULES -> ArmorRulesPacket.read(in);
            case PixcoreProtocol.ID_KEY_EVENT -> KeyEventPacket.read(in);
            case PixcoreProtocol.ID_EFFECT_CLEAR -> EffectClearPacket.read(in);
            case PixcoreProtocol.ID_RESOURCE_PACK_CHUNK -> ResourcePackChunkPacket.read(in);
            case PixcoreProtocol.ID_RESOURCE_PACK_STATUS -> ResourcePackStatusPacket.read(in);
            case PixcoreProtocol.ID_MONSTER_RULES -> MonsterRulesPacket.read(in);
            default -> throw new IOException("Unknown Pixcore packet id: " + id);
        };
    }

    public static void writeString(DataOutput out, String value) throws IOException {
        byte[] bytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_STRING_BYTES) {
            throw new IOException("Pixcore string too large: " + bytes.length + " bytes");
        }
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static String readString(DataInput in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > MAX_STRING_BYTES) {
            throw new IOException("Invalid Pixcore string length: " + length);
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] readRemaining(DataInputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }

    private static byte[] gunzip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = gzip.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
        }
        return bos.toByteArray();
    }
}
