package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestUnlocksPayload() implements CustomPacketPayload {

    public static final Type<RequestUnlocksPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "request_unlocks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestUnlocksPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestUnlocksPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
