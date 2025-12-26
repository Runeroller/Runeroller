package com.runeroller.runeroller.unlocks;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Runeroller.MODID)
public final class RunerollerRecipeLockEvents {

    private RunerollerRecipeLockEvents() {}

    /**
     * More proactive blocking: Prevent even taking the result out of the crafting slot.
     */
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        
        // We can check if they are currently holding a locked item in their cursor (carried)
        // and if so, delete it. This covers many ways of getting the item.
        ItemStack carried = sp.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(carried.getItem());
            if (id != null && RunerollerContentLocks.isBlocked(sp, id.getNamespace())) {
                sp.containerMenu.setCarried(ItemStack.EMPTY);
                warn(sp, RunerollerContentLocks.displayForNamespace(id.getNamespace()));
            }
        }
    }

    /**
     * Fires when player crafts via crafting grid (inventory crafting or crafting table).
     * This is the cleanest way (no tick snapshot needed).
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        ItemStack crafted = event.getCrafting();
        if (crafted == null || crafted.isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(crafted.getItem());
        if (id == null) return;

        String ns = id.getNamespace();

        // only lock mod namespaces you mapped in RunerollerContentLocks
        if (!RunerollerContentLocks.isBlocked(sp, ns)) return;

        // DELETE the crafted output (one-time craft deletes what you just made)
        // In practice, crafted stack is already being inserted to inv/cursor.
        // We remove up to crafted.getCount() from player inventory & cursor.
        int toRemove = crafted.getCount();

        // 1) Remove from cursor (carried stack)
        ItemStack carried = sp.containerMenu.getCarried();
        if (!carried.isEmpty() && ItemStack.isSameItemSameComponents(carried, crafted)) {
            int take = Math.min(toRemove, carried.getCount());
            carried.shrink(take);
            toRemove -= take;
            sp.containerMenu.setCarried(carried);
        }

        // 2) Remove from inventory
        if (toRemove > 0) {
            toRemove -= removeFromInventory(sp, crafted, toRemove);
        }

        warn(sp, RunerollerContentLocks.displayForNamespace(ns));
    }

    /**
     * OPTIONAL BONUS:
     * If you also want to delete smelt results (furnace/blast/smoker) when locked,
     * keep this event. If you only care about crafting table, you can remove it.
     */
    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        ItemStack smelted = event.getSmelting();
        if (smelted == null || smelted.isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(smelted.getItem());
        if (id == null) return;

        String ns = id.getNamespace();
        if (!RunerollerContentLocks.isBlocked(sp, ns)) return;

        int toRemove = smelted.getCount();

        // delete from cursor first (sometimes result is taken into cursor)
        ItemStack carried = sp.containerMenu.getCarried();
        if (!carried.isEmpty() && ItemStack.isSameItemSameComponents(carried, smelted)) {
            int take = Math.min(toRemove, carried.getCount());
            carried.shrink(take);
            toRemove -= take;
            sp.containerMenu.setCarried(carried);
        }

        if (toRemove > 0) {
            removeFromInventory(sp, smelted, toRemove);
        }

        warn(sp, RunerollerContentLocks.displayForNamespace(ns));
    }

    private static int removeFromInventory(ServerPlayer sp, ItemStack match, int amount) {
        int removed = 0;

        // ServerPlayer inventory = sp.getInventory()
        // items list contains main inventory + hotbar.
        var inv = sp.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (removed >= amount) break;

            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;

            if (ItemStack.isSameItemSameComponents(s, match)) {
                int take = Math.min(amount - removed, s.getCount());
                s.shrink(take);
                removed += take;

                if (s.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
                else inv.setItem(i, s);
            }
        }

        // Make sure client sees the update
        sp.inventoryMenu.broadcastChanges();
        return removed;
    }

    private static void warn(ServerPlayer player, String display) {
        player.displayClientMessage(
                Component.literal("Craft blocked: ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(display).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" (Unlock in Runeroller > Unlocks)").withStyle(ChatFormatting.GRAY)),
                true
        );
    }
}
