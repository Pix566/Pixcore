package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. One chunk of a file inside the Pixcore resource pack folder. */
public record ResourcePackChunkPacket(String path, int offset, byte[] data, boolean last) implements Packet {
    public ResourcePackChunkPacket {
        path = path == null ? "" : path;
        data = data == null ? new byte[0] : data;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_RESOURCE_PACK_CHUNK;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, path);
        out.writeInt(offset);
        out.writeInt(data.length);
        out.write(data);
        out.writeBoolean(last);
    }

    static ResourcePackChunkPacket read(DataInput in) throws IOException {
        String path = PacketCodec.readString(in);
        int offset = in.readInt();
        int length = in.readInt();
        if (length < 0 || length > 16 * 1024 * 1024) {
            throw new IOException("Invalid resource pack chunk length: " + length);
        }
        byte[] data = new byte[length];
        in.readFully(data);
        boolean last = in.readBoolean();
        return new ResourcePackChunkPacket(path, offset, data, last);
    }
}
