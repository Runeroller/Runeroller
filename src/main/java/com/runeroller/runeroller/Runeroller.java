package com.runeroller.runeroller;

import com.mojang.logging.LogUtils;
import com.runeroller.runeroller.client.RunerollerClientScreens;
import com.runeroller.runeroller.network.RunerollerNetwork;
import com.runeroller.runeroller.registry.RunerollerMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Runeroller.MODID)
public final class Runeroller {

    public static final String MODID = "runeroller";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Runeroller(IEventBus modEventBus, ModContainer modContainer) {

        // ✅ Register MenuTypes (Bank menu, future menus)
        RunerollerMenus.register(modEventBus);

        // ✅ Register network payloads (menus, unlocks, bank, shop, etc.)
        modEventBus.addListener(RunerollerNetwork::registerPayloads);

        // ✅ Lifecycle setup
        modEventBus.addListener(this::onCommonSetup);

        LOGGER.info("Runeroller core mod constructed");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Runeroller common setup complete");
    }
}
