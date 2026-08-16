package dev.pixcore.neoforge.network;

import dev.pixcore.neoforge.PixcoreMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge wrapper around the dependency-free Pixcore byte protocol.
 * The actual encoding is handled by {@code dev.pixcore.protocol.PacketCodec}.
 */
public record PixcorePayload(byte[] data) implements CustomPacketPayload {
    public static final Type<PixcorePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PixcoreMod.MOD_ID, "main"));

    public static final StreamCodec<ByteBuf, PixcorePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.data()),
            buf -> {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                return new PixcorePayload(data);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
