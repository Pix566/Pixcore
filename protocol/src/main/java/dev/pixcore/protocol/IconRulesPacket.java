package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. JSON object mapping icon rule id to rule definition. */
public record IconRulesPacket(String rulesJson) implements Packet {
    public IconRulesPacket {
        rulesJson = rulesJson == null ? "{}" : rulesJson;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_ICON_RULES;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, rulesJson);
    }

    static IconRulesPacket read(DataInput in) throws IOException {
        return new IconRulesPacket(PacketCodec.readString(in));
    }
}
