package com.runeroller.runeroller.unlocks;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RunerollerUnlockData extends SavedData {

    private static final String DATA_NAME = Runeroller.MODID + "_unlocks";

    // uuid -> (key -> unlocked)
    private final Map<UUID, Map<String, Boolean>> states = new HashMap<>();

    public RunerollerUnlockData() {}

    public static RunerollerUnlockData get(ServerLevel level) {
        // ✅ 1.21.1: computeIfAbsent needs Factory(supplier, loader, dataFixType)
        // ✅ DataFixTypes.SAVED_DATA doesn't exist in your mappings -> use LEVEL
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        RunerollerUnlockData::new,
                        RunerollerUnlockData::load,
                        DataFixTypes.LEVEL
                ),
                DATA_NAME
        );
    }

    // -------------------------
    // API used by your network
    // -------------------------

    public Map<String, Boolean> getPlayerStates(UUID playerId) {
        return states.computeIfAbsent(playerId, id -> new HashMap<>());
    }

    public boolean isUnlocked(UUID playerId, String key) {
        return getPlayerStates(playerId).getOrDefault(key, false);
    }

    public void setUnlocked(UUID playerId, String key, boolean unlocked) {
        getPlayerStates(playerId).put(key, unlocked);
        this.setDirty();
    }

    // -------------------------
    // Saving / Loading (1.21.1)
    // -------------------------

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag players = new ListTag();

        for (var playerEntry : states.entrySet()) {
            UUID uuid = playerEntry.getKey();
            Map<String, Boolean> unlocksMap = playerEntry.getValue();

            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("id", uuid);

            CompoundTag unlocksTag = new CompoundTag();
            for (var e : unlocksMap.entrySet()) {
                unlocksTag.putBoolean(e.getKey(), e.getValue());
            }

            playerTag.put("unlocks", unlocksTag);
            players.add(playerTag);
        }

        tag.put("players", players);
        return tag;
    }

    public static RunerollerUnlockData load(CompoundTag tag, HolderLookup.Provider registries) {
        RunerollerUnlockData data = new RunerollerUnlockData();

        ListTag players = tag.getList("players", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);

            UUID id = playerTag.getUUID("id");
            CompoundTag unlocksTag = playerTag.getCompound("unlocks");

            Map<String, Boolean> map = new HashMap<>();
            for (String key : unlocksTag.getAllKeys()) {
                map.put(key, unlocksTag.getBoolean(key));
            }

            data.states.put(id, map);
        }

        return data;
    }
}
