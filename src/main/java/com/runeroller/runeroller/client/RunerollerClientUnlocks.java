package com.runeroller.runeroller.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class RunerollerClientUnlocks {

    private static final Map<String, Boolean> STATES = new HashMap<>();

    private RunerollerClientUnlocks() {}

    public static void setAll(Map<String, Boolean> states) {
        STATES.clear();
        STATES.putAll(states);
    }

    public static boolean isUnlocked(String key) {
        return STATES.getOrDefault(key, false);
    }

    public static Map<String, Boolean> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(STATES));
    }
}
