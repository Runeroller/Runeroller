package com.runeroller.runeroller.client;

public final class RunerollerClientBankState {
    private RunerollerClientBankState() {}

    private static int slots = 54;
    private static int stackLimit = 64;

    public static void set(int newSlots, int newStackLimit) {
        slots = newSlots;
        stackLimit = newStackLimit;
    }

    public static int slots() {
        return slots;
    }

    public static int stackLimit() {
        return stackLimit;
    }
}
