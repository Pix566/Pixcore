package dev.pixcore.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Server -> client. Removes a previously sent HUD/effect by id. */
public record EffectClearPacket(String effectId) implements Packet {
    public EffectClearPacket {
        effectId = effectId == null ? "" : effectId;
    }

    @Override
    public int getId() {
        return PixcoreProtocol.ID_EFFECT_CLEAR;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        PacketCodec.writeString(out, effectId);
    }

    static EffectClearPacket read(DataInput in) throws IOException {
        return new EffectClearPacket(PacketCodec.readString(in));
    }
}
