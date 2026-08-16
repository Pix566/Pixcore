package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Client -> server. Sent when the Pixcore NeoForge client joins a server. */
public record HandshakePacket(int maxProtocolVersion, int minProtocolVersion,
                              int capabilities, String clientName) implements Packet {
    public HandshakePacket {
        clientName = clientName == null ? "" : clientName;
    }

    /** Convenience constructor used when the client supports exactly one version. */
    public HandshakePacket(int protocolVersion, int capabilities, String clientName) {
        this(protocolVersion, protocolVersion, capabilities, clientName);
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_HANDSHAKE;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(maxProtocolVersion);
        out.writeInt(minProtocolVersion);
        out.writeInt(capabilities);
        PacketCodec.writeString(out, clientName);
    }

    static HandshakePacket read(DataInput in) throws IOException {
        return new HandshakePacket(in.readInt(), in.readInt(), in.readInt(), PacketCodec.readString(in));
    }
}
