package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. Tooltip enhancement rules as JSON. */
public record TooltipRulesPacket(String rulesJson) implements Packet {
    public TooltipRulesPacket {
        rulesJson = rulesJson == null ? "{}" : rulesJson;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_TOOLTIP_RULES;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, rulesJson);
    }

    static TooltipRulesPacket read(DataInput in) throws IOException {
        return new TooltipRulesPacket(PacketCodec.readString(in));
    }
}
