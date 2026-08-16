package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. One HUD text/texture entry as JSON. */
public record HudPacket(String id, String json) implements Packet {
    public HudPacket {
        id = id == null ? "" : id;
        json = json == null ? "{}" : json;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_HUD;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, id);
        PacketCodec.writeString(out, json);
    }

    static HudPacket read(DataInput in) throws IOException {
        return new HudPacket(PacketCodec.readString(in), PacketCodec.readString(in));
    }
}
