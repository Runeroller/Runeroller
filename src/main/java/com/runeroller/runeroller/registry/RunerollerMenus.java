package com.runeroller.runeroller.registry;

import com.runeroller.runeroller.Runeroller;
import com.runeroller.runeroller.bank.BankMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RunerollerMenus {

    private RunerollerMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Runeroller.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BankMenu>> BANK =
            MENUS.register("bank", () -> new MenuType<>(BankMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
