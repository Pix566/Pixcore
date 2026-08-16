package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. Equipment appearance rules as JSON. */
public record ArmorRulesPacket(String rulesJson) implements Packet {
    public ArmorRulesPacket {
        rulesJson = rulesJson == null ? "{}" : rulesJson;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_ARMOR_RULES;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, rulesJson);
    }

    static ArmorRulesPacket read(DataInput in) throws IOException {
        return new ArmorRulesPacket(PacketCodec.readString(in));
    }
}
