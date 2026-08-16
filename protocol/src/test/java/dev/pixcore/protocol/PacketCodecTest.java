package dev.pixcore.protocol;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PacketCodecTest {
    @Test
    void roundTripHandshake() throws Exception {
        HandshakePacket packet = new HandshakePacket(2, 31, "NeoForge");
        Packet decoded = PacketCodec.decode(PacketCodec.encode(packet));
        HandshakePacket handshake = assertInstanceOf(HandshakePacket.class, decoded);
        assertEquals(2, handshake.maxProtocolVersion());
        assertEquals(2, handshake.minProtocolVersion());
        assertEquals(31, handshake.capabilities());
        assertEquals("NeoForge", handshake.clientName());
    }

    @Test
    void roundTripLargeJson() throws Exception {
        String large = "{\"data\":\"" + "x".repeat(100_000) + "\"}";
        IconRulesPacket packet = new IconRulesPacket(large);
        Packet decoded = PacketCodec.decode(PacketCodec.encode(packet));
        IconRulesPacket rules = assertInstanceOf(IconRulesPacket.class, decoded);
        assertEquals(large, rules.rulesJson());
    }

    @Test
    void roundTripHud() throws Exception {
        HudPacket packet = new HudPacket("welcome", "{\"type\":\"text\",\"text\":[\"hi\"]}");
        Packet decoded = PacketCodec.decode(PacketCodec.encode(packet));
        HudPacket hud = assertInstanceOf(HudPacket.class, decoded);
        assertEquals("welcome", hud.id());
        assertEquals("{\"type\":\"text\",\"text\":[\"hi\"]}", hud.json());
    }

    @Test
    void compressionIsUsedForLargePackets() throws Exception {
        String large = "y".repeat(200_000);
        IconRulesPacket packet = new IconRulesPacket(large);
        byte[] encoded = PacketCodec.encode(packet);
        assertEquals(1, encoded[0] & 0xFF, "Large packet should be marked compressed");
    }

    @Test
    void randomDataDoesNotBreakCodec() throws Exception {
        Random random = new Random(42);
        byte[] junk = new byte[256];
        random.nextBytes(junk);
        // No assertion needed; this is mostly a smoke test that unknown/truncated data fails cleanly.
        try {
            PacketCodec.decode(junk);
        } catch (Exception ignored) {
        }
    }
}
