package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → Server
 * Requests opening the Runeroller menu.
 */
public record OpenMenuC2SPayload() implements CustomPacketPayload {

    public static final Type<OpenMenuC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Runeroller.MODID, "open_menu_c2s"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuC2SPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenMenuC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
