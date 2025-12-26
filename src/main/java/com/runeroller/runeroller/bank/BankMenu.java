package com.runeroller.runeroller.bank;

import com.runeroller.runeroller.registry.RunerollerMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BankMenu extends AbstractContainerMenu {

    // visible grid = wider now
    public static final int COLS = 12;
    public static final int ROWS_VISIBLE = 6;
    public static final int VISIBLE_SLOTS = COLS * ROWS_VISIBLE; // 72

    // backing container ONLY for client-side rendering
    private final Container window = new SimpleContainer(VISIBLE_SLOTS);

    // server-only
    private final ServerPlayer serverPlayer;
    private final BankSavedData saved;
    private final PlayerBank bank;

    // synced row offset (rows)
    private int rowOffset = 0;

    // Search filtering
    private String searchQuery = "";
    private final List<Integer> filteredIndices = new ArrayList<>();

    // we sync offset to client via DataSlot (small int, good)
    private final DataSlot rowOffsetSlot = DataSlot.standalone();
    private final DataSlot visibleCountSlot = DataSlot.standalone();

    public BankMenu(int containerId, Inventory inv) {
        this(containerId, inv, inv.player);
    }

    // This is the constructor your SimpleMenuProvider MUST call
    public BankMenu(int containerId, Inventory inv, Player player) {
        super(RunerollerMenus.BANK.get(), containerId);

        if (player instanceof ServerPlayer sp) {
            this.serverPlayer = sp;
            this.saved = BankSavedData.get(sp.serverLevel());
            this.bank = saved.getBank(sp.getUUID());
        } else {
            this.serverPlayer = null;
            this.saved = null;
            this.bank = null;
        }

        // init sync slot
        this.rowOffsetSlot.set(0);
        this.addDataSlot(rowOffsetSlot);
        this.visibleCountSlot.set(isServer() ? bank.getSlots() : PlayerBank.MIN_SLOTS);
        this.addDataSlot(visibleCountSlot);

        // build slots
        addBankSlots();
        addPlayerSlots(inv);

        // first populate visible window from bank
        refreshWindowFromBank(true);
    }

    private boolean isServer() {
        return serverPlayer != null && saved != null && bank != null;
    }

    public int getBankSlotsTotal() {
        return visibleCountSlot.get();
    }

    public int getStackLimit() {
        return isServer() ? bank.getStackLimit() : 64;
    }

    public int getRowOffset() {
        // always use the synced value when client-side
        if (!isServer()) return rowOffsetSlot.get();
        return rowOffset;
    }

    public int getMaxRowOffset() {
        int total = getBankSlotsTotal();
        int totalRows = (int) Math.ceil(total / (double) COLS);
        return Math.max(0, totalRows - ROWS_VISIBLE);
    }

    public void setSearchQuery(String query) {
        if (!isServer()) return;
        this.searchQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        this.rowOffset = 0;
        this.rowOffsetSlot.set(0);
        refreshFilteredIndices();
        this.visibleCountSlot.set(searchQuery.isEmpty() ? bank.getSlots() : filteredIndices.size());
        refreshWindowFromBank(true);
    }

    private void refreshFilteredIndices() {
        filteredIndices.clear();
        if (searchQuery.isEmpty()) return;

        for (int i = 0; i < bank.getSlots(); i++) {
            ItemStack stack = bank.getItem(i);
            if (!stack.isEmpty()) {
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (name.contains(searchQuery)) {
                    filteredIndices.add(i);
                }
            }
        }
    }

    /** Called by BankScreen (client prediction) */
    public void setRowOffset(int newOffset) {
        newOffset = Mth.clamp(newOffset, 0, getMaxRowOffset());

        if (isServer()) {
            if (rowOffset == newOffset) return;
            rowOffset = newOffset;
            rowOffsetSlot.set(newOffset);
            refreshWindowFromBank(false);
            saved.markDirtyNow();
        } else {
            // client prediction uses DataSlot value
            if (rowOffsetSlot.get() == newOffset) return;
            rowOffsetSlot.set(newOffset);
        }
    }

    /** Called by server packet handler (your code used setRowOffsetServer earlier) */
    public void setRowOffsetServer(int newOffset) {
        setRowOffset(newOffset);
    }

    public void sortServer() {
        if (!isServer()) return;
        bank.sort();
        saved.markDirtyNow();
        refreshWindowFromBank(true);
    }

    private void addBankSlots() {
        // bank window slots: 9x6
        int startX = 8;
        int startY = 30; // After search and header

        for (int row = 0; row < ROWS_VISIBLE; row++) {
            for (int col = 0; col < COLS; col++) {
                int windowIndex = row * COLS + col;
                int x = startX + col * 18;
                int y = startY + row * 18;

                this.addSlot(new BankWindowSlot(window, windowIndex, x, y));
            }
        }
    }

    private void addPlayerSlots(Inventory inv) {
        // player inventory
        int invStartX = 35; // centered: 8 + (230-176)/2
        int invStartY = 142; // After inventory header

        // main inv 3 rows
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int idx = col + row * 9 + 9;
                this.addSlot(new Slot(inv, idx, invStartX + col * 18, invStartY + row * 18));
            }
        }

        // hotbar
        int hotbarY = invStartY + 58; // 142 + 58 = 200
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, invStartX + col * 18, hotbarY));
        }
    }

    private int toRealIndex(int windowIndex) {
        int offsetIndex = windowIndex + (getRowOffset() * COLS);
        if (searchQuery.isEmpty()) return offsetIndex;

        if (offsetIndex >= 0 && offsetIndex < filteredIndices.size()) {
            return filteredIndices.get(offsetIndex);
        }
        return -1;
    }

    private void refreshWindowFromBank(boolean force) {
        if (!isServer()) return;

        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int real = toRealIndex(i);
            ItemStack st = (real >= 0 && real < bank.getSlots()) ? bank.getItem(real) : ItemStack.EMPTY;
            window.setItem(i, st);
        }

        if (force) broadcastChanges();
    }

    private void commitWindowSlotToBank(int windowIndex) {
        if (!isServer()) return;

        int real = toRealIndex(windowIndex);
        if (real < 0 || real >= bank.getSlots()) return;

        ItemStack st = window.getItem(windowIndex);
        bank.setItem(real, st);
        saved.markDirtyNow();
    }

    private ItemStack removeFromBank(int windowIndex, int amount) {
        if (!isServer()) return ItemStack.EMPTY;

        int real = toRealIndex(windowIndex);
        if (real < 0 || real >= bank.getSlots()) return ItemStack.EMPTY;

        ItemStack taken = bank.removeItem(real, amount);
        saved.markDirtyNow();

        // keep window synced
        window.setItem(windowIndex, bank.getItem(real));
        broadcastChanges();

        return taken;
    }

    @Override
    public void slotsChanged(Container changed) {
        super.slotsChanged(changed);

        // if it was a bank window change, commit to PlayerBank
        if (isServer() && changed == window) {
            for (int i = 0; i < VISIBLE_SLOTS; i++) commitWindowSlotToBank(i);
            // Re-filter in case item name changed or items were added/removed
            refreshFilteredIndices();
            this.visibleCountSlot.set(searchQuery.isEmpty() ? bank.getSlots() : filteredIndices.size());
            refreshWindowFromBank(false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // Shift-click move
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        int bankStart = 0;
        int bankEnd = VISIBLE_SLOTS; // exclusive

        int invStart = bankEnd;
        int invEnd = this.slots.size();

        if (index < bankEnd) {
            // from bank -> player inv
            if (!this.moveItemStackTo(stack, invStart, invEnd, true)) return ItemStack.EMPTY;
        } else {
            // from player inv -> bank
            if (!this.moveItemStackTo(stack, bankStart, bankEnd, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

    // ---- Slot type that enforces stack limit ----
    private final class BankWindowSlot extends Slot {

        public BankWindowSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return BankMenu.this.getStackLimit();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            int real = toRealIndex(this.getContainerSlot());
            return real >= 0 && real < getBankSlotsTotal();
        }

        @Override
        public boolean mayPickup(Player player) {
            int real = toRealIndex(this.getContainerSlot());
            return real >= 0 && real < getBankSlotsTotal();
        }

        @Override
        public ItemStack remove(int amount) {
            // server-authoritative remove to keep PlayerBank correct
            if (isServer()) {
                return removeFromBank(this.getContainerSlot(), amount);
            }
            return super.remove(amount);
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            if (isServer()) commitWindowSlotToBank(this.getContainerSlot());
        }
    }
}
