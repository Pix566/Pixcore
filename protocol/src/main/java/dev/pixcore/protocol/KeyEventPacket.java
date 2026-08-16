package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Client -> server. A server-defined keybind was pressed/released. */
public record KeyEventPacket(String keyId, int action) implements Packet {
    /** action: 0 = release, 1 = press, 2 = repeat (vanilla GLFW-ish). */
    public KeyEventPacket {
        keyId = keyId == null ? "" : keyId;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_KEY_EVENT;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, keyId);
        out.writeInt(action);
    }

    static KeyEventPacket read(DataInput in) throws IOException {
        return new KeyEventPacket(PacketCodec.readString(in), in.readInt());
    }
}
