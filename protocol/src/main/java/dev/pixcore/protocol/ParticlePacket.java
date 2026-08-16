package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. One particle effect entry as JSON. */
public record ParticlePacket(String json) implements Packet {
    public ParticlePacket {
        json = json == null ? "{}" : json;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_PARTICLE;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, json);
    }

    static ParticlePacket read(DataInput in) throws IOException {
        return new ParticlePacket(PacketCodec.readString(in));
    }
}
