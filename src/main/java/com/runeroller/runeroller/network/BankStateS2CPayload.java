package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BankStateS2CPayload(int slots, int stackLimit) implements CustomPacketPayload {

    public static final Type<BankStateS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "bank_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BankStateS2CPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.slots());
                        buf.writeVarInt(p.stackLimit());
                    },
                    buf -> new BankStateS2CPayload(buf.readVarInt(), buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
