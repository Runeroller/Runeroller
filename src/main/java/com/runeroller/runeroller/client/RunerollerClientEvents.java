package com.runeroller.runeroller.client;

import com.runeroller.runeroller.Runeroller;
import com.runeroller.runeroller.network.OpenMenuC2SPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

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
}
