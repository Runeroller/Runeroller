package com.runeroller.runeroller.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.runeroller.runeroller.Runeroller;
import com.runeroller.runeroller.network.OpenMenuC2SPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Runeroller.MODID, value = Dist.CLIENT)
public final class RunerollerClientEvents {

    private RunerollerClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only open when no other screen is open
        if (RunerollerKeybinds.OPEN_MENU.consumeClick() && mc.screen == null) {
            if (mc.player.connection != null) {
                mc.player.connection.send(new OpenMenuC2SPayload());
            }
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (RunerollerKeybinds.OPEN_MENU.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                // Check if we are in a MerchantScreen or some other relevant container screen.
                // Overhaul mods like "Towns and Towers" often use standard or slightly modified villager screens.
                // Allowing it in any AbstractContainerScreen is generally safe and helpful.
                if (event.getScreen() instanceof MerchantScreen || event.getScreen() instanceof AbstractContainerScreen) {
                    mc.player.connection.send(new OpenMenuC2SPayload());
                    event.setCanceled(true);
                }
            }
        }
    }
}
