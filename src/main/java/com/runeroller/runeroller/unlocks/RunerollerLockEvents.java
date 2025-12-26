package com.runeroller.runeroller.unlocks;

import com.runeroller.runeroller.Runeroller;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Runeroller.MODID)
public final class RunerollerLockEvents {

    private RunerollerLockEvents() {}

    /** Client-side: Add a big red [LOCKED] warning to tooltips for items in blocked mods. */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return;

        String ns = id.getNamespace();
        String key = RunerollerContentLocks.unlockKeyForNamespace(ns);
        if (key == null) return;

        // Use Client-side check
        if (!com.runeroller.runeroller.client.RunerollerClientUnlocks.isUnlocked(key)) {
            event.getToolTip().add(1, Component.literal("[LOCKED]").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            event.getToolTip().add(2, Component.literal("Unlock in Runeroller Menu (K)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    /** Prevent right-clicking with an item. */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return;

        String ns = id.getNamespace();
        if (!RunerollerContentLocks.isBlocked(sp, ns)) return;

        warn(sp, RunerollerContentLocks.displayForNamespace(ns));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    /** Prevent right-clicking a block. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) return;

        String ns = id.getNamespace();
        if (!RunerollerContentLocks.isBlocked(sp, ns)) return;

        warn(sp, RunerollerContentLocks.displayForNamespace(ns));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    /** Prevent right-clicking an entity (e.g. Iron's Spells or Easy Villagers). */
    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        Entity target = event.getTarget();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (id == null) return;

        String ns = id.getNamespace();
        if (!RunerollerContentLocks.isBlocked(sp, ns)) return;

        warn(sp, RunerollerContentLocks.displayForNamespace(ns));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    private static void warn(ServerPlayer player, String display) {
        // Actionbar message
        player.displayClientMessage(
                Component.literal("Locked: ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(display).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" (Unlock in Runeroller > Unlocks)").withStyle(ChatFormatting.GRAY)),
                true
        );
    }
}
