package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. Response to {@link HandshakePacket}. */
public record HandshakeAckPacket(boolean accepted, String serverName, String serverVersion,
                                 int protocolVersion, String moduleVersionsJson) implements Packet {
    public HandshakeAckPacket {
        serverName = serverName == null ? "" : serverName;
        serverVersion = serverVersion == null ? "" : serverVersion;
        moduleVersionsJson = moduleVersionsJson == null ? "{}" : moduleVersionsJson;
    }

    public HandshakeAckPacket(boolean accepted, String serverName, String serverVersion, int protocolVersion) {
        this(accepted, serverName, serverVersion, protocolVersion, PixcoreProtocol.defaultModuleVersionsJson());
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_HANDSHAKE_ACK;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeBoolean(accepted);
        PacketCodec.writeString(out, serverName);
        PacketCodec.writeString(out, serverVersion);
        out.writeInt(protocolVersion);
        PacketCodec.writeString(out, moduleVersionsJson);
    }

    static HandshakeAckPacket read(DataInput in) throws IOException {
        return new HandshakeAckPacket(in.readBoolean(), PacketCodec.readString(in), PacketCodec.readString(in),
                in.readInt(), PacketCodec.readString(in));
    }
}
