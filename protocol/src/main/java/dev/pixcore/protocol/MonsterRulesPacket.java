package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. Monster appearance rules as JSON. */
public record MonsterRulesPacket(String rulesJson) implements Packet {
    public MonsterRulesPacket {
        rulesJson = rulesJson == null ? "{}" : rulesJson;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_MONSTER_RULES;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, rulesJson);
    }

    static MonsterRulesPacket read(DataInput in) throws IOException {
        return new MonsterRulesPacket(PacketCodec.readString(in));
    }
}
