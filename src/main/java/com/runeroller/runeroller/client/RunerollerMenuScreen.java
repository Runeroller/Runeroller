package com.runeroller.runeroller.client;

import com.runeroller.runeroller.Runeroller;
import com.runeroller.runeroller.network.OpenBankC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class RunerollerMenuScreen extends Screen {

    // Icons (src/main/resources/assets/runeroller/textures/gui/menu/)
    private static final ResourceLocation ICON_SLAY    = ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "textures/gui/menu/slay.png");
    private static final ResourceLocation ICON_QUEST   = ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "textures/gui/menu/quest.png");
    private static final ResourceLocation ICON_UNLOCKS = ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "textures/gui/menu/unlocks.png");
    private static final ResourceLocation ICON_BANK    = ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "textures/gui/menu/bank.png");
    private static final ResourceLocation ICON_SHOP    = ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "textures/gui/menu/shop.png");
    private static final ResourceLocation ICON_SKILL   = ResourceLocation.fromNamespaceAndPath(Runeroller.MODID, "textures/gui/menu/skills.png");

    // Layout
    private static final int BTN_W = 240;
    private static final int BTN_H = 22;
    private static final int GAP   = 8;

    private long openedAtMs;
    private final List<MenuButtonEntry> buttons = new ArrayList<>();

    private record MenuButtonEntry(IconTextButton button, int index) {}

    public RunerollerMenuScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.openedAtMs = System.currentTimeMillis();
        this.buttons.clear();

        int x = (this.width - BTN_W) / 2;

        int titleH = 30;
        int buttonsCount = 7; // 6 (main) + Close
        int totalButtonsH = (buttonsCount * BTN_H) + ((buttonsCount - 1) * GAP);
        int totalH = titleH + 20 + totalButtonsH;

        int top = (this.height - totalH) / 2;
        int y = top + titleH + 20;

        addButton(0, x, y, "Slay", ICON_SLAY, () -> Minecraft.getInstance().setScreen(new RunerollerSlayScreen(this)));
        y += BTN_H + GAP;

        addButton(1, x, y, "Quest", ICON_QUEST, () -> Minecraft.getInstance().setScreen(new RunerollerQuestsScreen(this)));
        y += BTN_H + GAP;

        addButton(2, x, y, "Unlocks", ICON_UNLOCKS, () -> Minecraft.getInstance().setScreen(new RunerollerUnlocksScreen(this)));
        y += BTN_H + GAP;

        addButton(3, x, y, "Bank", ICON_BANK, () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                mc.player.connection.send(new OpenBankC2SPayload());
            }
            this.onClose();
        });
        y += BTN_H + GAP;

        addButton(4, x, y, "Shop", ICON_SHOP, () -> Minecraft.getInstance().setScreen(new ShopScreen(this)));
        y += BTN_H + GAP;

        addButton(5, x, y, "Skill", ICON_SKILL, () -> Minecraft.getInstance().setScreen(new RunerollerSkillScreen(this)));
        y += BTN_H + GAP;

        // Utility buttons in top right
        int utilX = this.width - 95;
        int utilY = 5;
        int utilW = 90;
        int utilH = 18;
        int utilGap = 2;

        IconTextButton readmeBtn = new IconTextButton(utilX, utilY, utilW, utilH, Component.literal("Readme"), null, () -> Minecraft.getInstance().setScreen(new RunerollerReadmeScreen(this)));
        utilY += utilH + utilGap;
        IconTextButton tutorialBtn = new IconTextButton(utilX, utilY, utilW, utilH, Component.literal("Tutorial"), null, () -> {
            Minecraft.getInstance().setScreen(new RunerollerReadmeScreen(this, RunerollerReadmeScreen.View.TUTORIAL));
        });
        utilY += utilH + utilGap;
        IconTextButton changelogBtn = new IconTextButton(utilX, utilY, utilW, utilH, Component.literal("Changelog"), null, () -> {
            Minecraft.getInstance().setScreen(new RunerollerReadmeScreen(this, RunerollerReadmeScreen.View.CHANGELOG));
        });
        utilY += utilH + utilGap;

        // GUI Scale button
        int currentScale = minecraft.options.guiScale().get();
        String scaleText = "Scale: " + (currentScale == 0 ? "Auto" : currentScale);
        IconTextButton scaleBtn = new IconTextButton(utilX, utilY, utilW, utilH, Component.literal(scaleText), null, () -> {
            int max = minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode()) + 1;
            int next = (minecraft.options.guiScale().get() + 1) % max;
            minecraft.options.guiScale().set(next);
            minecraft.resizeDisplay();
            this.init(minecraft, width, height);
        });

        this.addRenderableWidget(readmeBtn);
        this.addRenderableWidget(tutorialBtn);
        this.addRenderableWidget(changelogBtn);
        this.addRenderableWidget(scaleBtn);

        // Close button at the bottom of the main stack
        IconTextButton closeBtn = new IconTextButton(x, y, BTN_W, BTN_H, Component.literal("Close"), null, this::onClose);
        this.buttons.add(new MenuButtonEntry(closeBtn, 6));
        this.addRenderableWidget(closeBtn);
    }

    private void addButton(int index, int x, int y, String text, ResourceLocation icon, Runnable action) {
        IconTextButton btn = new IconTextButton(x, y, BTN_W, BTN_H, Component.literal(text), icon, action, 1.0f + (index * 0.05f));
        this.buttons.add(new MenuButtonEntry(btn, index));
        this.addRenderableWidget(btn);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderModernBackground(gfx, partialTick);
        
        // Update button animations before super.render
        float seconds = (System.currentTimeMillis() - openedAtMs) / 1000.0f;
        for (MenuButtonEntry entry : buttons) {
            float delay = entry.index * 0.1f;
            float anim = Mth.clamp((seconds - delay) * 2.0f, 0.0f, 1.0f);
            // Smooth entry: slide from left and fade
            float eased = 1.0f - (float) Math.pow(1.0f - anim, 3); // easeOutCubic
            
            int targetX = (this.width - BTN_W) / 2;
            int startX = targetX - 50;
            entry.button.setX((int) Mth.lerp(eased, startX, targetX));
            // We can't easily set alpha on the widget itself without modifying it, 
            // but the IconTextButton can use its internal timer.
        }

        super.render(gfx, mouseX, mouseY, partialTick);
        drawAnimatedTitle(gfx);
    }

    private void renderModernBackground(GuiGraphics gfx, float partialTick) {
        // Base dark dim
        gfx.fill(0, 0, this.width, this.height, 0xCC050505);
        
        long ms = System.currentTimeMillis();
        float t = (ms % 100000) / 1000.0f;

        // Animated gradient lines or spots
        for (int i = 0; i < 5; i++) {
            float xPulse = Mth.sin(t * (0.2f + i * 0.1f)) * 0.5f + 0.5f;
            float yPulse = Mth.cos(t * (0.15f + i * 0.05f)) * 0.5f + 0.5f;
            
            int gx = (int) (xPulse * this.width);
            int gy = (int) (yPulse * this.height);
            int size = 150 + i * 50;
            
            // Draw a very soft radial-like glow using fills (crude but effective for performance)
            // Ideally we'd use a texture, but we can do some overlapping fills
            int color = 0x084C89FF; // Very faint blue
            gfx.fill(gx - size/2, gy - size/2, gx + size/2, gy + size/2, color);
        }
        
        // Horizontal scanline effect
        int scanlineY = (int) ((t * 40) % this.height);
        gfx.fill(0, scanlineY, this.width, scanlineY + 1, 0x11FFFFFF);
    }

    private void drawAnimatedTitle(GuiGraphics gfx) {
        String title = "RUNEROLLER";
        String subtitle = "Premium Modded Experience";

        float seconds = (System.currentTimeMillis() - openedAtMs) / 1000.0f;
        
        // Entry animation for title
        float titleAnim = Mth.clamp(seconds * 1.5f, 0.0f, 1.0f);
        float titleYOffset = (1.0f - titleAnim) * -20;

        float hue = (seconds * 0.12f) % 1.0f;
        int rgb = Mth.hsvToRgb(hue, 0.7f, 0.9f);
        int mainColor = 0xFF000000 | rgb;

        int titleW = this.font.width(title);
        int titleX = (this.width - titleW) / 2;

        int titleH = 30;
        int buttonsCount = 7;
        int totalButtonsH = (buttonsCount * BTN_H) + ((buttonsCount - 1) * GAP);
        int totalH = titleH + 20 + totalButtonsH;
        int menuTop = (this.height - totalH) / 2;
        int titleY = (int) (menuTop + titleYOffset);

        // Main Title
        gfx.pose().pushPose();
        gfx.pose().translate(titleX + titleW / 2.0f, titleY + 5, 0);
        float scale = 1.5f + 0.05f * Mth.sin(seconds * 2.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.pose().translate(-(titleW / 2.0f), -5, 0);
        
        // Shadow/Glow
        gfx.drawString(this.font, title, 1, 1, 0xAA000000, false);
        gfx.drawString(this.font, title, 0, 0, mainColor, false);
        gfx.pose().popPose();

        // Subtitle
        int subW = this.font.width(subtitle);
        int subX = (this.width - subW) / 2;
        int subY = titleY + 22;
        float subAnim = Mth.clamp((seconds - 0.5f) * 1.5f, 0.0f, 1.0f);
        int subAlpha = (int) (subAnim * 255);
        gfx.drawString(this.font, subtitle, subX, subY, (subAlpha << 24) | 0xAAAAAA, false);
        
        // Separator line
        int lineW = (int) (titleAnim * (BTN_W + 40));
        int lineX = (this.width - lineW) / 2;
        gfx.fill(lineX, subY + 12, lineX + lineW, subY + 13, (subAlpha / 2 << 24) | 0x4C89FF);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.options.save();
    }

    private void run(String commandWithoutSlash) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendCommand(commandWithoutSlash);
        }
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
