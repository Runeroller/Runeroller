package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleUnlockPayload(String key) implements CustomPacketPayload {

    public static final Type<ToggleUnlockPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "toggle_unlock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleUnlockPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeUtf(payload.key()),
                    buf -> new ToggleUnlockPayload(buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
