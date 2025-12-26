package com.runeroller.runeroller.unlocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class RunerollerUnlocks {

    private RunerollerUnlocks() {}

    private static final String ROOT = "runeroller";
    private static final String UNLOCKS = "unlocks";

    private static CompoundTag getUnlockTag(ServerPlayer player) {
        CompoundTag pd = player.getPersistentData();
        if (!pd.contains(ROOT, CompoundTag.TAG_COMPOUND)) {
            pd.put(ROOT, new CompoundTag());
        }
        CompoundTag root = pd.getCompound(ROOT);

        if (!root.contains(UNLOCKS, CompoundTag.TAG_COMPOUND)) {
            root.put(UNLOCKS, new CompoundTag());
        }
        return root.getCompound(UNLOCKS);
    }

    public static boolean isUnlocked(ServerPlayer player, String key) {
        return getUnlockTag(player).getBoolean(key);
    }

    public static void setUnlocked(ServerPlayer player, String key, boolean value) {
        getUnlockTag(player).putBoolean(key, value);
    }
}
