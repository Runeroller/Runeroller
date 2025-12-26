package com.runeroller.runeroller.bank;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public final class PlayerBank {

    public static final int MIN_SLOTS = 54;     // double chest
    public static final int MAX_SLOTS = 2048;   // increased cap for wider bank

    private int slots = MIN_SLOTS;
    private int stackLimit = 64;

    private final List<BankStack> items = new ArrayList<>();

    public PlayerBank() {
        resizeTo(slots);
    }

    /** Create default bank */
    public static PlayerBank createDefault() {
        return new PlayerBank();
    }

    public int getSlots() {
        return slots;
    }

    public int getStackLimit() {
        return stackLimit;
    }

    public void setSlots(int newSlots) {
        newSlots = Mth.clamp(newSlots, MIN_SLOTS, MAX_SLOTS);
        this.slots = newSlots;
        resizeTo(newSlots);
    }

    public void setStackLimit(int newLimit) {
        newLimit = Mth.clamp(newLimit, 64, 999999);
        this.stackLimit = newLimit;

        // clamp any existing oversized stacks
        for (int i = 0; i < slots; i++) {
            ItemStack st = getItem(i);
            if (!st.isEmpty() && st.getCount() > stackLimit) {
                st.setCount(stackLimit);
                setItem(i, st);
            }
        }
    }

    public void sort() {
        // Collect all items
        List<ItemStack> allItems = new ArrayList<>();
        for (int i = 0; i < slots; i++) {
            ItemStack s = getItem(i);
            if (!s.isEmpty()) {
                allItems.add(s);
            }
        }

        // Merge identical stacks (respecting stackLimit)
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : allItems) {
            boolean added = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    int canAdd = stackLimit - existing.getCount();
                    if (canAdd > 0) {
                        int toAdd = Math.min(canAdd, stack.getCount());
                        existing.grow(toAdd);
                        stack.shrink(toAdd);
                        if (stack.isEmpty()) {
                            added = true;
                            break;
                        }
                    }
                }
            }
            if (!added && !stack.isEmpty()) {
                merged.add(stack);
            }
        }

        // Sort by name
        merged.sort((a, b) -> a.getHoverName().getString().compareToIgnoreCase(b.getHoverName().getString()));

        // Clear and put back
        clear();
        for (int i = 0; i < merged.size() && i < slots; i++) {
            setItem(i, merged.get(i));
        }
    }

    public void clear() {
        for (int i = 0; i < slots; i++) items.set(i, BankStack.empty());
    }

    private void resizeTo(int newSlots) {
        while (items.size() < newSlots) items.add(BankStack.empty());
        while (items.size() > newSlots) items.remove(items.size() - 1);
    }

    // ---- Item API ----

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= slots) return ItemStack.EMPTY;
        return items.get(slot).toItemStack();
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= slots) return;

        if (stack == null || stack.isEmpty()) {
            items.set(slot, BankStack.empty());
            return;
        }

        ItemStack copy = stack.copy();
        if (copy.getCount() > stackLimit) copy.setCount(stackLimit);
        items.set(slot, BankStack.fromItemStack(copy));
    }

    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= slots) return ItemStack.EMPTY;

        ItemStack cur = getItem(slot);
        if (cur.isEmpty()) return ItemStack.EMPTY;

        int take = Math.min(amount, cur.getCount());
        ItemStack out = cur.copy();
        out.setCount(take);

        cur.shrink(take);
        if (cur.isEmpty()) {
            setItem(slot, ItemStack.EMPTY);
        } else {
            setItem(slot, cur);
        }
        return out;
    }

    // ---- Save/Load ----

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Slots", slots);
        tag.putInt("StackLimit", stackLimit);

        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            BankStack bs = items.get(i);
            if (bs.isEmpty()) continue;

            CompoundTag e = bs.toTag();
            e.putInt("Slot", i);
            list.add(e);
        }
        tag.put("Items", list);
        return tag;
    }

    public static PlayerBank fromTag(CompoundTag tag) {
        PlayerBank bank = new PlayerBank();

        int slots = tag.getInt("Slots");
        int limit = tag.getInt("StackLimit");

        bank.setSlots(slots <= 0 ? MIN_SLOTS : slots);
        bank.setStackLimit(limit <= 0 ? 64 : limit);

        bank.clear();

        ListTag list = tag.getList("Items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            int slot = e.getInt("Slot");
            if (slot < 0 || slot >= bank.slots) continue;
            bank.items.set(slot, BankStack.fromTag(e));
        }

        return bank;
    }

    // ---- Stored stack ----

    public record BankStack(String itemId, int count, CompoundTag nbt) {

        public static BankStack empty() {
            return new BankStack("", 0, null);
        }

        public boolean isEmpty() {
            return itemId == null || itemId.isEmpty() || count <= 0;
        }

        public ItemStack toItemStack() {
            if (isEmpty()) return ItemStack.EMPTY;

            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
            if (item == null) return ItemStack.EMPTY;

            ItemStack st = new ItemStack(item, count);

            // 1.21.1: use CustomData component
            if (nbt != null && !nbt.isEmpty()) {
                st.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt.copy()));
            }

            return st;
        }

        public static BankStack fromItemStack(ItemStack st) {
            if (st == null || st.isEmpty()) return empty();

            String id = BuiltInRegistries.ITEM.getKey(st.getItem()).toString();

            CompoundTag tag = null;
            CustomData cd = st.get(DataComponents.CUSTOM_DATA);
            if (cd != null) {
                CompoundTag copied = cd.copyTag();
                if (!copied.isEmpty()) tag = copied;
            }

            return new BankStack(id, st.getCount(), tag);
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Item", itemId == null ? "" : itemId);
            tag.putInt("Count", count);
            if (nbt != null && !nbt.isEmpty()) tag.put("NBT", nbt.copy());
            return tag;
        }

        public static BankStack fromTag(CompoundTag tag) {
            String id = tag.getString("Item");
            int count = tag.getInt("Count");

            CompoundTag nbt = null;
            if (tag.contains("NBT", 10)) {
                CompoundTag got = tag.getCompound("NBT");
                if (!got.isEmpty()) nbt = got.copy();
            }

            if (id == null || id.isEmpty() || count <= 0) return empty();
            return new BankStack(id, count, nbt);
        }
    }
}
