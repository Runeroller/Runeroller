package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server → Client
 * Tells the client to open the Unlocks screen.
 */
public record OpenUnlocksS2CPayload() implements CustomPacketPayload {

    public static final Type<OpenUnlocksS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Runeroller.MODID, "open_unlocks"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenUnlocksS2CPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenUnlocksS2CPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
