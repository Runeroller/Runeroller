package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenMenuS2CPayload() implements CustomPacketPayload {

    public static final Type<OpenMenuS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "open_menu_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuS2CPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenMenuS2CPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
