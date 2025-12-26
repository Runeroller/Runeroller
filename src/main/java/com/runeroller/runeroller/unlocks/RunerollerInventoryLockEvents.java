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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Runeroller.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class RunerollerInventoryLockEvents {

    private RunerollerInventoryLockEvents() {}

    // simple cooldown so chat/actionbar doesn't spam
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        // run only every 10 ticks (0.5s) for performance
        if ((sp.tickCount % 10) != 0) return;

        UUID id = sp.getUUID();
        int cd = COOLDOWN.getOrDefault(id, 0);
        if (cd > 0) COOLDOWN.put(id, cd - 1);

        boolean removedAny = false;
        String lastBlockedDisplay = null;

        // 1) remove from carried stack (cursor)
        ItemStack carried = sp.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            String ns = namespaceOf(carried);
            if (ns != null && RunerollerContentLocks.isBlocked(sp, ns)) {
                sp.containerMenu.setCarried(ItemStack.EMPTY);
                removedAny = true;
                lastBlockedDisplay = RunerollerContentLocks.displayForNamespace(ns);
            }
        }

        // 2) remove from inventory (hotbar + main)
        var inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            String ns = namespaceOf(stack);
            if (ns == null) continue;

            if (RunerollerContentLocks.isBlocked(sp, ns)) {
                inv.setItem(i, ItemStack.EMPTY);
                removedAny = true;
                lastBlockedDisplay = RunerollerContentLocks.displayForNamespace(ns);
            }
        }

        if (removedAny) {
            sp.inventoryMenu.broadcastChanges();

            // warn sometimes, not every tick
            if (COOLDOWN.getOrDefault(id, 0) <= 0) {
                warn(sp, lastBlockedDisplay != null ? lastBlockedDisplay : "Locked mod");
                COOLDOWN.put(id, 6); // ~3 seconds (since we tick every 0.5s)
            }
        }
    }

    private static String namespaceOf(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) return null;
        return key.getNamespace();
    }

    private static void warn(ServerPlayer player, String display) {
        player.displayClientMessage(
                Component.literal("Locked content removed: ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(display).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" (Unlock in Runeroller > Unlocks)").withStyle(ChatFormatting.GRAY)),
                true
        );
    }
}
