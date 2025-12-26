package com.runeroller.runeroller.client;

import com.runeroller.runeroller.Runeroller;
import com.runeroller.runeroller.bank.BankMenu;
import com.runeroller.runeroller.registry.RunerollerMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.resources.ResourceLocation;

@EventBusSubscriber(modid = Runeroller.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RunerollerClientScreens {

    private RunerollerClientScreens() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RunerollerMenus.BANK.get(), BankScreen::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(ResourceLocation.withDefaultNamespace("hotbar"), 
                ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "slayer_hud"), 
                SlayerHudOverlay::render);
    }
}
