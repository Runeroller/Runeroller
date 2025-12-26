package com.runeroller.runeroller.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public final class IconTextButton extends AbstractButton {

    private final ResourceLocation icon;
    private final Runnable action;

    // icon stays native size (sharp)
    private static final int ICON_SIZE = 18;
    private static final int ICON_LEFT_PAD = 6;
    private static final int TEXT_LEFT_PAD = ICON_LEFT_PAD + ICON_SIZE + 8;

    // animation state
    private float hoverAnim = 0.0f; // 0..1
    private final float clickPitch; // small variety per button

    public IconTextButton(int x, int y, int width, int height, Component message,
                          ResourceLocation icon, Runnable action) {
        this(x, y, width, height, message, icon, action, 1.0f);
    }

    public IconTextButton(int x, int y, int width, int height, Component message,
                          ResourceLocation icon, Runnable action, float clickPitch) {
        super(x, y, width, height, message);
        this.icon = icon;
        this.action = action;
        this.clickPitch = clickPitch;
    }

    @Override
    public void onPress() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), this.clickPitch));
        this.action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        long ms = Util.getMillis();
        float t = (ms % 100000) / 1000.0f;

        boolean hovered = this.isHoveredOrFocused();

        // Smooth hover animation
        float target = hovered ? 1.0f : 0.0f;
        // higher = snappier
        float speed = 0.20f;
        this.hoverAnim = Mth.lerp(speed, this.hoverAnim, target);

        // Background colors
        int baseBg = 0xFF404040;
        int hoverBg = 0xFF585858;
        int bg = lerpARGB(baseBg, hoverBg, this.hoverAnim);

        // Subtle pulse only when hovered
        float pulse = hovered ? (0.5f + 0.5f * Mth.sin(t * 6.0f)) : 0.0f; // 0..1

        // Border + glow
        int border = 0xFF000000;

        // Outer border
        gfx.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, border);

        // Inner fill
        gfx.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, bg);

        // Hover glow line (top and bottom for more "modern" feel)
        if (hovered) {
            int glowAlpha = (int) (100 + 100 * pulse); // 100..200
            int glowColor = 0x4C89FF; // Blue glow
            int glow = (glowAlpha << 24) | glowColor;
            
            // Outer glow effect
            gfx.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY(), glow);
            gfx.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, glow);
            
            // Inner accent lines
            gfx.fill(this.getX() + 2, this.getY() + 2, this.getX() + this.width - 2, this.getY() + 3, glow);
        }

        // Icon position
        int iconX = this.getX() + ICON_LEFT_PAD;
        int iconY = this.getY() + (this.height - ICON_SIZE) / 2;

        // Icon bounce + tiny scale on hover + subtle rotation
        float bounce = (hovered ? (Mth.sin(t * 10.0f) * 1.5f) : 0.0f) + (this.hoverAnim * -1.5f);
        float scale = 1.0f + (0.12f * this.hoverAnim);
        float rotation = hovered ? (Mth.sin(t * 4.0f) * 5.0f) : 0.0f;

        if (this.icon != null) {
            gfx.pose().pushPose();
            gfx.pose().translate(iconX + ICON_SIZE / 2.0f, iconY + ICON_SIZE / 2.0f + bounce, 0);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
            gfx.pose().translate(-ICON_SIZE / 2.0f, -ICON_SIZE / 2.0f, 0);

            gfx.blit(
                    this.icon,
                    0, 0,
                    0, 0,
                    ICON_SIZE, ICON_SIZE,
                    ICON_SIZE, ICON_SIZE
            );

            gfx.pose().popPose();
        }

        // Text: centered in remaining space
        Font font = Minecraft.getInstance().font;

        int leftPad = (this.icon != null) ? TEXT_LEFT_PAD : 8;
        int usableW = this.width - leftPad - 8;
        int textW = font.width(this.getMessage());

        int textX = this.getX() + leftPad + Math.max(0, (usableW - textW) / 2);
        int textY = this.getY() + (this.height - font.lineHeight) / 2;

        // Text color slightly brightens on hover
        int baseText = 0xFFEFEFEF;
        int hoverText = 0xFFFFFFFF;
        int textColor = lerpARGB(baseText, hoverText, this.hoverAnim);

        // crisp outline for readability
        drawOutlineString(gfx, font, this.getMessage(), textX, textY, textColor);
    }

    // IMPORTANT: must be public (you hit "attempting to assign weaker access privileges; was public")
    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    private static void drawOutlineString(GuiGraphics gfx, Font font, Component text, int x, int y, int color) {
        int outline = 0xFF000000;
        gfx.drawString(font, text, x - 1, y, outline, false);
        gfx.drawString(font, text, x + 1, y, outline, false);
        gfx.drawString(font, text, x, y - 1, outline, false);
        gfx.drawString(font, text, x, y + 1, outline, false);
        gfx.drawString(font, text, x, y, color, false);
    }

    private static int lerpARGB(int a, int b, float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);

        int aA = (a >>> 24) & 0xFF, aR = (a >>> 16) & 0xFF, aG = (a >>> 8) & 0xFF, aB = a & 0xFF;
        int bA = (b >>> 24) & 0xFF, bR = (b >>> 16) & 0xFF, bG = (b >>> 8) & 0xFF, bB = b & 0xFF;

        int oA = (int) Mth.lerp(t, aA, bA);
        int oR = (int) Mth.lerp(t, aR, bR);
        int oG = (int) Mth.lerp(t, aG, bG);
        int oB = (int) Mth.lerp(t, aB, bB);

        return (oA << 24) | (oR << 16) | (oG << 8) | oB;
    }
}
