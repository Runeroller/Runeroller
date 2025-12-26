package com.runeroller.runeroller.unlocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side helper for counting/removing Runeroller Coins from a player's inventory.
 * Coin item id: runeroller_coins:coin
 */
public final class RunerollerCoinHelper {

    private RunerollerCoinHelper() {}

    // ✅ 1.21.1: use fromNamespaceAndPath (constructor is private)
    public static final ResourceLocation COIN_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("runeroller_coins", "coin");

    private static Item cachedCoin;

    private static Item getCoinItem() {
        if (cachedCoin == null) {
            cachedCoin = BuiltInRegistries.ITEM.get(COIN_ITEM_ID);
        }
        return cachedCoin;
    }

    /** Count how many coins the player has in their inventory. */
    public static int countCoins(ServerPlayer player) {
        Item coin = getCoinItem();
        if (coin == null) return 0;

        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == coin) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /**
     * Remove N coins from the player's inventory.
     * @return true if removed fully; false if not enough coins (inventory unchanged as best-effort)
     */
    public static boolean removeCoins(ServerPlayer player, int amount) {
        if (amount <= 0) return true;

        int have = countCoins(player);
        if (have < amount) return false;

        Item coin = getCoinItem();
        int remaining = amount;

        // Remove from stacks until remaining == 0
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty() || stack.getItem() != coin) continue;

            int take = Math.min(stack.getCount(), remaining);
            stack.shrink(take);
            remaining -= take;
        }

        // Sync inventory
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    /** Add coins to the player's inventory. */
    public static void addCoins(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        Item coin = getCoinItem();
        if (coin == null) return;

        ItemStack stack = new ItemStack(coin, amount);
        if (!player.getInventory().add(stack)) {
            // If inventory is full, drop it on the ground
            player.drop(stack, false);
        }
        player.inventoryMenu.broadcastChanges();
    }
}
