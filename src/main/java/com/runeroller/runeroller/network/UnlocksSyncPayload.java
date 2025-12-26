package com.runeroller.runeroller.network;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Server -> Client
 * Syncs ALL unlock states for the current player.
 *
 * Map format:
 *  key   = content id (example: "create")
 *  value = unlocked (true/false)
 */
public record UnlocksSyncPayload(Map<String, Boolean> states)
        implements CustomPacketPayload {

    public static final Type<UnlocksSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Runeroller.MODID,
                    "unlocks_sync"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnlocksSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    // WRITE
                    (buf, payload) -> {
                        buf.writeVarInt(payload.states.size());
                        for (var entry : payload.states.entrySet()) {
                            buf.writeUtf(entry.getKey());
                            buf.writeBoolean(entry.getValue());
                        }
                    },
                    // READ
                    buf -> {
                        int size = buf.readVarInt();
                        Map<String, Boolean> map = new HashMap<>();

                        for (int i = 0; i < size; i++) {
                            String key = buf.readUtf();
                            boolean value = buf.readBoolean();
                            map.put(key, value);
                        }

                        return new UnlocksSyncPayload(map);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
