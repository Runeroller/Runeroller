package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BankScrollC2SPayload(int rowOffset) implements CustomPacketPayload {

    public static final Type<BankScrollC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "bank_scroll"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BankScrollC2SPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeVarInt(p.rowOffset()),
                    buf -> new BankScrollC2SPayload(buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
