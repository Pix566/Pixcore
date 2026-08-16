package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Client -> server. Reports the client's local resource pack file hashes. */
public record ResourcePackStatusPacket(String manifestJson) implements Packet {
    public ResourcePackStatusPacket {
        manifestJson = manifestJson == null ? "{}" : manifestJson;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_RESOURCE_PACK_STATUS;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, manifestJson);
    }

    static ResourcePackStatusPacket read(DataInput in) throws IOException {
        return new ResourcePackStatusPacket(PacketCodec.readString(in));
    }
}
