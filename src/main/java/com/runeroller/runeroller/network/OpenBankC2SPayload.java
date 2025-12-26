package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBankC2SPayload() implements CustomPacketPayload {

    public static final Type<OpenBankC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "open_bank"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBankC2SPayload> STREAM_CODEC =
            StreamCodec.of((buf, p) -> {}, buf -> new OpenBankC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
