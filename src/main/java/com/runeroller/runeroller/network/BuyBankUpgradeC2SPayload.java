package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyBankUpgradeC2SPayload(Upgrade upgrade) implements CustomPacketPayload {

    public enum Upgrade { SLOTS, STACK }

    public static final Type<BuyBankUpgradeC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "buy_bank_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyBankUpgradeC2SPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeVarInt(p.upgrade().ordinal()),
                    buf -> new BuyBankUpgradeC2SPayload(Upgrade.values()[buf.readVarInt()])
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
