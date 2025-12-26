package com.runeroller.runeroller.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.runeroller.runeroller.Runeroller;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = Runeroller.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public final class RunerollerKeybinds {

    public static final String CATEGORY = "key.categories.runeroller";
    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.runeroller.open_menu",
            InputConstants.KEY_K,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
    }
}
