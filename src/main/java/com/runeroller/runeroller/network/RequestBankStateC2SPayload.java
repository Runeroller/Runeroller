package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestBankStateC2SPayload() implements CustomPacketPayload {

    public static final Type<RequestBankStateC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "request_bank_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBankStateC2SPayload> STREAM_CODEC =
            StreamCodec.of((buf, p) -> {}, buf -> new RequestBankStateC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
