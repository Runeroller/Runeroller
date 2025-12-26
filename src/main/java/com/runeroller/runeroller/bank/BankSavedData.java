package com.runeroller.runeroller.bank;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BankSavedData extends SavedData {

    public static final String DATA_NAME = "runeroller_bank";
    private final Map<UUID, PlayerBank> banks = new HashMap<>();

    public static BankSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(BankSavedData::new, BankSavedData::load),
                DATA_NAME
        );
    }

    private static BankSavedData load(CompoundTag tag, HolderLookup.Provider lookup) {
        BankSavedData data = new BankSavedData();

        ListTag list = tag.getList("Banks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            UUID id = e.getUUID("Id");
            PlayerBank bank = PlayerBank.fromTag(e.getCompound("Bank"));
            data.banks.put(id, bank);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (var entry : banks.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Id", entry.getKey());
            e.put("Bank", entry.getValue().toTag());
            list.add(e);
        }
        tag.put("Banks", list);
        return tag;
    }

    public PlayerBank getBank(UUID playerId) {
        return banks.computeIfAbsent(playerId, id -> PlayerBank.createDefault());
    }

    public void markDirtyNow() {
        setDirty();
    }
}
