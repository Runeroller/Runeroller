package com.runeroller.runeroller.unlocks;

import java.util.HashMap;
import java.util.Map;

public final class RunerollerUnlockDefinitions {

    private RunerollerUnlockDefinitions() {}

    // key -> cost
    private static final Map<String, Integer> COSTS = new HashMap<>();

    // item namespace -> unlock key
    private static final Map<String, String> NAMESPACE_TO_KEY = new HashMap<>();

    static {
        // ----------------------------
        // Costs (your list)
        // ----------------------------
        COSTS.put("botanypots", 5000);
        COSTS.put("botanytrees", 5000);
        COSTS.put("create", 15000);
        COSTS.put("createaddition", 8000);
        COSTS.put("easyvillagers", 9000);
        COSTS.put("irons_spellbooks", 20000);
        COSTS.put("minecolonies", 30000);
        COSTS.put("sophisticatedbackpacks", 12000);

        // ----------------------------
        // Namespaces -> unlock key
        // (IMPORTANT: namespaces must match the item ids you see in-game)
        // ----------------------------
        mapNamespace("botanypots", "botanypots");
        mapNamespace("botanytrees", "botanytrees");

        mapNamespace("create", "create");
        mapNamespace("createaddition", "createaddition");

        // Easy Villagers is usually "easy_villagers" (but your unlock key is "easyvillagers")
        mapNamespace("easy_villagers", "easyvillagers");
        mapNamespace("easyvillagers", "easyvillagers"); // fallback, just in case

        mapNamespace("irons_spellbooks", "irons_spellbooks");

        mapNamespace("minecolonies", "minecolonies");

        mapNamespace("sophisticatedbackpacks", "sophisticatedbackpacks");
    }

    private static void mapNamespace(String namespace, String unlockKey) {
        NAMESPACE_TO_KEY.put(namespace, unlockKey);
    }

    public static int getCost(String key) {
        return COSTS.getOrDefault(key, -1);
    }

    /** Returns unlock key for a given item namespace, or null if not locked by our system. */
    public static String getUnlockKeyForNamespace(String namespace) {
        return NAMESPACE_TO_KEY.get(namespace);
    }

    /** Is this namespace one of the “locked mods” at all? */
    public static boolean isLockedModNamespace(String namespace) {
        return NAMESPACE_TO_KEY.containsKey(namespace);
    }
}
