package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. Server-defined keybind list as JSON. */
public record KeybindDefinitionsPacket(String definitionsJson) implements Packet {
    public KeybindDefinitionsPacket {
        definitionsJson = definitionsJson == null ? "{}" : definitionsJson;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_KEYBIND_DEFINITIONS;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, definitionsJson);
    }

    static KeybindDefinitionsPacket read(DataInput in) throws IOException {
        return new KeybindDefinitionsPacket(PacketCodec.readString(in));
    }
}
