package dev.pixcore.neoforge.network;

import dev.pixcore.neoforge.PixcoreMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge wrapper for client -> server Pixcore traffic.
 */
public record PixcoreServerPayload(byte[] data) implements CustomPacketPayload {
    public static final Type<PixcoreServerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PixcoreMod.MOD_ID, "main_c2s"));

    public static final StreamCodec<ByteBuf, PixcoreServerPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.data()),
            buf -> {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                return new PixcoreServerPayload(data);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
