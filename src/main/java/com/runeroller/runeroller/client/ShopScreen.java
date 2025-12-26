package com.runeroller.runeroller.client;

import com.runeroller.runeroller.network.BuyBankUpgradeC2SPayload;
import com.runeroller.runeroller.network.RequestBankStateC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ShopScreen extends Screen {

    private final Screen back;
    
    // Layout constants
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 180;
    
    private static final ResourceLocation COIN_ITEM_ID = 
            ResourceLocation.fromNamespaceAndPath("runeroller_coins", "coin");

    private static final ResourceLocation ICON_BANK = ResourceLocation.fromNamespaceAndPath("runeroller", "textures/gui/menu/bank.png");
    private static final ResourceLocation ICON_UPGRADE = ResourceLocation.fromNamespaceAndPath("runeroller", "textures/gui/menu/skills.png");

    public ShopScreen(Screen back) {
        super(Component.literal("Shop"));
        this.back = back;
    }

    @Override
    protected void init() {
        // request latest bank state so we can show slots/stack
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new RequestBankStateC2SPayload());
        }

        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        // Upgrade Slots Button
        addRenderableWidget(new IconTextButton(
                x + 20, y + 60, PANEL_W - 40, 20,
                Component.literal("Upgrade Slots (+9)"),
                ICON_BANK,
                () -> sendBuy(BuyBankUpgradeC2SPayload.Upgrade.SLOTS)
        ));

        // Upgrade Stack Limit Button
        addRenderableWidget(new IconTextButton(
                x + 20, y + 100, PANEL_W - 40, 20,
                Component.literal("Upgrade Stack Limit (+64)"),
                ICON_UPGRADE,
                () -> sendBuy(BuyBankUpgradeC2SPayload.Upgrade.STACK)
        ));

        // Back button (sleek top-right)
        addRenderableWidget(Button.builder(Component.literal("<"), b ->
                Minecraft.getInstance().setScreen(back)
        ).bounds(x + PANEL_W - 20, y + 4, 16, 12).build());
    }

    private void sendBuy(BuyBankUpgradeC2SPayload.Upgrade up) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new BuyBankUpgradeC2SPayload(up));
        }
        // refresh request after buying
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new RequestBankStateC2SPayload());
        }
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Overlay to make the GUI stand out more
        gfx.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);

        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        // Main Panel
        drawCard(gfx, x, y, PANEL_W, PANEL_H);
        
        // Header bar
        gfx.fill(x + 4, y + 4, x + PANEL_W - 22, y + 18, 0xFF1A1F27);
        gfx.drawString(this.font, "Bank Upgrades Shop", x + 10, y + 6, 0xFFFFFFFF, false);

        // Coin display
        int coins = countCoinsClient();
        String coinsTxt = "Coins: " + coins;
        gfx.drawString(this.font, coinsTxt, x + PANEL_W - 26 - this.font.width(coinsTxt), y + 6, 0xFF4C89FF, false);

        // Content Area Background
        drawSubCard(gfx, x + 10, y + 25, PANEL_W - 20, PANEL_H - 35);

        // Info labels
        int curSlots = RunerollerClientBankState.slots();
        int curStack = RunerollerClientBankState.stackLimit();
        
        gfx.drawString(this.font, "Current Bank Capacity:", x + 20, y + 35, 0xFFE0E0E0, false);
        gfx.drawString(this.font, curSlots + " Slots | Max Stack: " + curStack, x + 20, y + 46, 0xFF5CABFF, false);

        // Calculate costs based on number of upgrades bought
        int slotUpgrades = (curSlots - 54) / 9;
        int stackUpgrades = (curStack - 64) / 64;

        int slotsCost = 200 + (slotUpgrades * 150) + (slotUpgrades * slotUpgrades * 25);
        int stackCost = 200 + (stackUpgrades * 150) + (stackUpgrades * stackUpgrades * 25);

        gfx.drawString(this.font, "Cost: " + slotsCost + " coins", x + 25, y + 82, 0xFFFFD27A, false);
        gfx.drawString(this.font, "Cost: " + stackCost + " coins", x + 25, y + 122, 0xFFFFD27A, false);

        super.render(gfx, mouseX, mouseY, partialTick);
        
        renderTooltips(gfx, mouseX, mouseY, slotsCost, stackCost);
    }

    private void renderTooltips(GuiGraphics gfx, int mouseX, int mouseY, int slotsCost, int stackCost) {
        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        // Slot upgrade tooltip
        if (mouseX >= x + 20 && mouseX <= x + PANEL_W - 20 && mouseY >= y + 60 && mouseY <= y + 80) {
            gfx.renderTooltip(this.font, Component.literal("Adds 9 more storage slots to your bank."), mouseX, mouseY);
        }
        // Stack upgrade tooltip
        if (mouseX >= x + 20 && mouseX <= x + PANEL_W - 20 && mouseY >= y + 100 && mouseY <= y + 120) {
            gfx.renderTooltip(this.font, Component.literal("Increases max items per slot by 64."), mouseX, mouseY);
        }
    }

    private void drawCard(GuiGraphics gfx, int x, int y, int w, int h) {
        // Outer glow/shadow
        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF373737);
        gfx.fill(x, y, x + w, y + h, 0xFF0B0F16); // Background
        
        // Subtle 3D edges
        gfx.fill(x, y, x + w, y + 1, 0xFF4A4A4A); // Top highlight
        gfx.fill(x, y + h - 1, x + w, y + h, 0xFF1A1F27); // Bottom shadow
    }

    private void drawSubCard(GuiGraphics gfx, int x, int y, int w, int h) {
        // Recessed background
        gfx.fill(x, y, x + w, y + h, 0xFF111318);
        
        // Inner shadow effect
        int shadow = 0xFF080A0E;
        int highlight = 0xFF1A1F27;
        gfx.fill(x, y, x + w, y + 1, shadow); // Top
        gfx.fill(x, y + h - 1, x + w, y + h, highlight); // Bottom
        gfx.fill(x, y, x + 1, y + h, shadow); // Left
        gfx.fill(x + w - 1, y, x + w, y + h, highlight); // Right
    }

    private int countCoinsClient() {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        Item coin = BuiltInRegistries.ITEM.get(COIN_ITEM_ID);
        if (coin == null) return 0;
        int total = 0;
        for (ItemStack st : mc.player.getInventory().items) {
            if (!st.isEmpty() && st.getItem() == coin) total += st.getCount();
        }
        ItemStack off = mc.player.getOffhandItem();
        if (!off.isEmpty() && off.getItem() == coin) total += off.getCount();
        return Mth.clamp(total, 0, Integer.MAX_VALUE);
    }

    public void refreshText() {
        // Costs and labels are updated in render()
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
