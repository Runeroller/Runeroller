package com.runeroller.runeroller.unlocks;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class RunerollerContentLocks {

    private RunerollerContentLocks() {}

    /**
     * Maps mod namespace -> your unlock key
     * (unlock keys must match your Unlocks screen + RunerollerUnlockDefinitions keys)
     *
     * IMPORTANT:
     * - Easy Villagers modid is usually "easy_villagers" (underscore) while your unlock key is "easyvillagers"
     */
    private static final Map<String, String> NAMESPACE_TO_UNLOCK_KEY = Map.ofEntries(
            Map.entry("botanypots", "botanypots"),
            Map.entry("botanytrees", "botanytrees"),
            Map.entry("create", "create"),
            Map.entry("createaddition", "createaddition"),
            Map.entry("easy_villagers", "easyvillagers"),
            Map.entry("irons_spellbooks", "irons_spellbooks"),
            Map.entry("minecolonies", "minecolonies"),
            Map.entry("sophisticatedbackpacks", "sophisticatedbackpacks")
    );

    /**
     * Namespace -> pretty display name for warnings
     */
    private static final Map<String, String> NAMESPACE_TO_DISPLAY = Map.ofEntries(
            Map.entry("botanypots", "Botany Pots"),
            Map.entry("botanytrees", "Botany Trees"),
            Map.entry("create", "Create"),
            Map.entry("createaddition", "Create Crafts & Additions"),
            Map.entry("easy_villagers", "Easy Villagers"),
            Map.entry("irons_spellbooks", "Iron's Spells 'n Spellbooks"),
            Map.entry("minecolonies", "MineColonies"),
            Map.entry("sophisticatedbackpacks", "Sophisticated Backpacks")
    );

    /** Returns your unlock-key for a namespace, or null if not locked by your system. */
    public static String unlockKeyForNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) return null;
        return NAMESPACE_TO_UNLOCK_KEY.get(namespace);
    }

    /** Returns a nice display name for warnings. */
    public static String displayForNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) return "Unknown";
        return NAMESPACE_TO_DISPLAY.getOrDefault(namespace, namespace);
    }

    /**
     * Server-side: check if player unlocked this key.
     * If anything is missing, we fail open (return true) so we don't delete items by accident.
     */
    public static boolean isUnlocked(ServerPlayer player, String unlockKey) {
        if (player == null || unlockKey == null || unlockKey.isEmpty()) return true;

        try {
            var data = RunerollerUnlockData.get(player.serverLevel());
            return data.isUnlocked(player.getUUID(), unlockKey);
        } catch (Throwable t) {
            // fail-open: if data isn't available for any reason, do NOT block content
            return true;
        }
    }

    /** True if this namespace is one of your locked mods AND player has NOT unlocked it. */
    public static boolean isBlocked(ServerPlayer player, String namespace) {
        String key = unlockKeyForNamespace(namespace);
        if (key == null) return false; // not locked by your system
        return !isUnlocked(player, key);
    }

    /**
     * Utility: get a copy of current locked states for debugging/logging.
     * Returns namespace -> blocked?
     */
    public static Map<String, Boolean> debugBlockedMap(ServerPlayer player) {
        Map<String, Boolean> out = new HashMap<>();
        for (String ns : NAMESPACE_TO_UNLOCK_KEY.keySet()) {
            out.put(ns, isBlocked(player, ns));
        }
        return out;
    }
}
