package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → Server
 * Requests unlock data + permission to open the Unlocks screen.
 */
public record RequestUnlocksC2SPayload() implements CustomPacketPayload {

    public static final Type<RequestUnlocksC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Runeroller.MODID, "request_unlocks"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestUnlocksC2SPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestUnlocksC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
