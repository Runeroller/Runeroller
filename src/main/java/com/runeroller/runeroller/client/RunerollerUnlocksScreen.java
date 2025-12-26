package com.runeroller.runeroller.client;

import com.runeroller.runeroller.network.RequestUnlocksC2SPayload;
import com.runeroller.runeroller.network.ToggleUnlockPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class RunerollerUnlocksScreen extends Screen {

    private final Screen backScreen;

    private UnlockListWidget list;
    private UnlockDef selected;

    private Button unlockBtn;
    private Button backBtn;

    // Layout
    private static final int HEADER_H = 22;
    private static final int FOOTER_H = 26;
    private static final int PAD = 10;
    private static final int GAP = 10;

    private static final int PANEL_MAX_W = 520;
    private static final int LIST_W = 235;

    // Compact list sizing
    private static final int ROW_H = 22;
    private static final int LIST_VISIBLE_MIN = 96;
    private static final int LIST_VISIBLE_MAX = 140;

    private static final List<UnlockDef> DEFAULTS = List.of(
            new UnlockDef("botanypots", "Botany Pots", 5000),
            new UnlockDef("botanytrees", "Botany Trees", 5000),
            new UnlockDef("create", "Create", 15000),
            new UnlockDef("createaddition", "Create Crafts & Additions", 8000),
            new UnlockDef("easyvillagers", "Easy Villagers", 9000),
            new UnlockDef("irons_spellbooks", "Iron's Spells 'n Spellbooks", 20000),
            new UnlockDef("minecolonies", "MineColonies", 30000),
            new UnlockDef("sophisticatedbackpacks", "Sophisticated Backpacks", 12000)
    );

    private static final ResourceLocation COIN_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("runeroller_coins", "coin");

    public RunerollerUnlocksScreen(Screen backScreen) {
        super(Component.literal("Unlocks"));
        this.backScreen = backScreen;
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        gfx.fill(0, 0, this.width, this.height, 0x88000000);
    }

    private Layout layout() {
        int rows = DEFAULTS.size();

        int naturalListH = rows * ROW_H + 8;
        int listH = Mth.clamp(naturalListH, LIST_VISIBLE_MIN, LIST_VISIBLE_MAX);

        // Right side can be a bit taller, but left stays compact
        int contentH = Math.max(listH + 20, 150);

        int panelW = Math.min(PANEL_MAX_W, this.width - 24);
        int panelH = HEADER_H + PAD + contentH + PAD + FOOTER_H;

        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int contentX = panelX + PAD;
        int contentY = panelY + HEADER_H + PAD;

        int leftW = Math.min(LIST_W, (panelW - PAD * 2 - GAP) / 2);
        int leftX = contentX;

        int rightX = leftX + leftW + GAP;
        int rightW = contentX + (panelW - PAD * 2) - rightX;

        int listX = leftX + 10;
        int listY = contentY + 12;
        int listW = leftW - 20;
        int listHInner = listH;

        // Left card is only slightly larger than the list → no wasted space
        int listCardX = leftX;
        int listCardY = contentY;
        int listCardW = leftW;
        int listCardH = (listY + listHInner - contentY) + 10;

        return new Layout(
                panelX, panelY, panelW, panelH,
                contentY, contentH,
                leftX, leftW,
                rightX, rightW,
                listX, listY, listW, listHInner,
                listCardX, listCardY, listCardW, listCardH
        );
    }

    @Override
    protected void init() {
        var mc = Minecraft.getInstance();

        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new RequestUnlocksC2SPayload());
        }

        Layout L = layout();

        this.list = new UnlockListWidget(L.listX, L.listY, L.listW, L.listH, DEFAULTS);
        this.addRenderableWidget(this.list);

        if (!DEFAULTS.isEmpty()) {
            setSelected(DEFAULTS.get(0));
            this.list.setSelectedIndex(0);
        }

        int btnY = L.panelY + L.panelH - FOOTER_H + 4;

        this.unlockBtn = Button.builder(Component.literal("Unlock"), b -> {
                    if (this.selected == null) return;
                    var p = Minecraft.getInstance().player;
                    if (p != null && p.connection != null) {
                        p.connection.send(new ToggleUnlockPayload(this.selected.key()));
                    }
                })
                .bounds(L.rightX, btnY, Math.max(120, L.rightW - 90), 18)
                .build();
        this.addRenderableWidget(this.unlockBtn);

        this.backBtn = Button.builder(Component.literal("<"), b -> this.minecraft.setScreen(backScreen))
                .bounds(L.panelX + L.panelW - 20, L.panelY + 4, 16, 12)
                .build();
        this.addRenderableWidget(this.backBtn);

        updateUnlockButton();
    }

    private void setSelected(UnlockDef def) {
        this.selected = def;
        updateUnlockButton();
    }

    public void refreshFromClientCache() {
        updateUnlockButton();
    }

    @Override
    public void tick() {
        updateUnlockButton();
    }

    private void updateUnlockButton() {
        if (this.unlockBtn == null) return;

        if (this.selected == null) {
            this.unlockBtn.active = false;
            this.unlockBtn.setMessage(Component.literal("Unlock"));
            return;
        }

        boolean unlocked = RunerollerClientUnlocks.isUnlocked(this.selected.key());
        if (unlocked) {
            this.unlockBtn.active = false;
            this.unlockBtn.setMessage(Component.literal("Unlocked"));
        } else {
            this.unlockBtn.active = true;
            this.unlockBtn.setMessage(Component.literal("Unlock"));
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        Layout L = layout();

        drawCard(gfx, L.panelX, L.panelY, L.panelW, L.panelH);

        // Header bar
        gfx.fill(L.panelX + 4, L.panelY + 4, L.panelX + L.panelW - 22, L.panelY + 18, 0xFF1A1F27);
        gfx.drawString(this.font, "Mod Unlocks", L.panelX + PAD, L.panelY + 6, 0xFFEFEFEF, false);

        int coins = countCoinsClient();
        String coinsTxt = "Coins: " + coins;
        gfx.drawString(this.font, coinsTxt,
                L.panelX + L.panelW - 26 - this.font.width(coinsTxt),
                L.panelY + 6, 0xFF4C89FF, false);

        drawSubCard(gfx, L.listCardX, L.listCardY, L.listCardW, L.listCardH);
        drawSubCard(gfx, L.rightX, L.contentY, L.rightW, L.contentH);

        int tx = L.rightX + 10;
        int ty = L.contentY + 10;

        if (this.selected == null) {
            gfx.drawString(this.font, "Select a mod to view details", tx, ty, 0xFF555555, false);
        } else {
            boolean unlocked = RunerollerClientUnlocks.isUnlocked(this.selected.key());
            int cost = this.selected.cost();

            // Mod Name Header
            gfx.fill(L.rightX + 4, L.contentY + 4, L.rightX + L.rightW - 4, L.contentY + 20, 0xFF1A1F27);
            gfx.drawString(this.font, this.selected.displayName(), tx, ty, 0xFFFFFFFF, false);
            ty += 24;

            int statusColor = unlocked ? 0xFF2ECC71 : 0xFFE74C3C;
            gfx.drawString(this.font, "Status: ", tx, ty, 0xFFBDBDBD, false);
            gfx.drawString(this.font, unlocked ? "UNLOCKED" : "LOCKED", tx + 40, ty, statusColor, false);
            ty += 16;

            if (!unlocked) {
                gfx.drawString(this.font, "Unlock Cost: ", tx, ty, 0xFFBDBDBD, false);
                gfx.drawString(this.font, String.valueOf(cost), tx + 70, ty, 0xFFFFD27A, false);
                ty += 20;

                gfx.fill(L.rightX + 8, ty, L.rightX + L.rightW - 8, ty + 1, 0xFF2A2F3A);
                ty += 10;

                String desc = "All items and recipes from this mod are currently restricted. Unlock it to gain full access.";
                gfx.drawWordWrap(this.font, Component.literal(desc), tx, ty, L.rightW - 20, 0xFF9A9A9A);
            } else {
                gfx.drawString(this.font, "Access: ", tx, ty, 0xFFBDBDBD, false);
                gfx.drawString(this.font, "Full Access Granted", tx + 45, ty, 0xFF2ECC71, false);
                ty += 20;
                
                gfx.fill(L.rightX + 8, ty, L.rightX + L.rightW - 8, ty + 1, 0xFF2A2F3A);
                ty += 10;
                
                gfx.drawWordWrap(this.font, Component.literal("You have successfully unlocked this mod. Enjoy the new content!"), tx, ty, L.rightW - 20, 0xFF9A9A9A);
            }
        }

        super.render(gfx, mouseX, mouseY, partialTick);
        renderUnlockTooltip(gfx, mouseX, mouseY);
    }

    private void renderUnlockTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        if (this.unlockBtn == null || this.selected == null) return;
        if (!this.unlockBtn.isMouseOver(mouseX, mouseY)) return;

        boolean unlocked = RunerollerClientUnlocks.isUnlocked(this.selected.key());
        if (unlocked) {
            gfx.renderTooltip(this.font, Component.literal("Already unlocked"), mouseX, mouseY);
            return;
        }

        int coins = countCoinsClient();
        int cost = this.selected.cost();

        if (coins < cost) {
            List<FormattedCharSequence> lines = List.of(
                    Component.literal("Cost: " + cost).getVisualOrderText(),
                    Component.literal("You have: " + coins).getVisualOrderText(),
                    Component.literal("Need: " + (cost - coins)).getVisualOrderText()
            );
            gfx.renderTooltip(this.font, lines, mouseX, mouseY);
        } else {
            gfx.renderTooltip(this.font, Component.literal("Cost: " + cost), mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public record UnlockDef(String key, String displayName, int cost) {}

    private record Layout(
            int panelX, int panelY, int panelW, int panelH,
            int contentY, int contentH,
            int leftX, int leftW,
            int rightX, int rightW,
            int listX, int listY, int listW, int listH,
            int listCardX, int listCardY, int listCardW, int listCardH
    ) {}

    // -----------------------
    // Compact custom list widget (NeoForge 1.21.1 compatible)
    // -----------------------
    private final class UnlockListWidget extends AbstractWidget {

        private final List<UnlockDef> entries;
        private int selectedIndex = -1;
        private float scroll; // pixels

        UnlockListWidget(int x, int y, int w, int h, List<UnlockDef> defs) {
            super(x, y, w, h, Component.empty());
            this.entries = new ArrayList<>(defs);
        }

        void setSelectedIndex(int idx) {
            this.selectedIndex = Mth.clamp(idx, 0, Math.max(0, entries.size() - 1));
            clampScroll();
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int innerX = getX();
            int innerY = getY();
            int innerW = width;
            int innerH = height;

            // clip region
            gfx.enableScissor(innerX, innerY, innerX + innerW, innerY + innerH);

            int totalH = entries.size() * ROW_H;
            int yStart = innerY - (int) scroll;

            for (int i = 0; i < entries.size(); i++) {
                int rowY = yStart + i * ROW_H;
                int rowY2 = rowY + ROW_H;

                if (rowY2 <= innerY || rowY >= innerY + innerH) continue;

                UnlockDef def = entries.get(i);
                boolean hovered = mouseX >= innerX && mouseX < innerX + innerW && mouseY >= rowY && mouseY < rowY2;
                boolean selectedThis = i == selectedIndex;
                boolean unlocked = RunerollerClientUnlocks.isUnlocked(def.key());

                int bg = selectedThis ? 0xFF1A1F27 : (hovered ? 0xFF141A23 : 0xFF0B0F16);
                gfx.fill(innerX, rowY, innerX + innerW, rowY2, bg);

                if (selectedThis) {
                    int selBorder = 0xFF4C89FF;
                    gfx.fill(innerX, rowY, innerX + 1, rowY2, selBorder); // left highlight
                    gfx.fill(innerX + innerW - 1, rowY, innerX + innerW, rowY2, selBorder); // right highlight
                }

                int separator = 0xFF1A1F27;
                gfx.fill(innerX, rowY2 - 1, innerX + innerW, rowY2, separator);

                int dot = unlocked ? 0xFF2ECC71 : 0xFFE74C3C;
                gfx.fill(innerX + 6, rowY + 9, innerX + 8, rowY + 13, dot);

                gfx.drawString(font, def.displayName(), innerX + 14, rowY + 3, 0xFFEFEFEF, false);

                String line2 = unlocked ? "Unlocked" : ("Cost: " + def.cost());
                int c2 = unlocked ? 0xFF85FFB1 : 0xFF707070;
                gfx.drawString(font, line2, innerX + 14, rowY + 12, c2, false);
            }

            gfx.disableScissor();

            // scrollbar
            if (totalH > innerH) {
                int barX = innerX + innerW - 4;
                int trackY0 = innerY;
                int trackY1 = innerY + innerH;

                gfx.fill(barX, trackY0, barX + 2, trackY1, 0x552A2F3A);

                float maxScroll = totalH - innerH;
                float knobH = Math.max(18f, (innerH * (innerH / (float) totalH)));
                float knobY = trackY0 + (scroll / maxScroll) * (innerH - knobH);

                gfx.fill(barX, (int) knobY, barX + 2, (int) (knobY + knobH), 0xAA4C89FF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            if (!this.isMouseOver(mouseX, mouseY)) return false;

            int localY = (int) mouseY - getY() + (int) scroll;
            int idx = localY / ROW_H;

            if (idx >= 0 && idx < entries.size()) {
                this.selectedIndex = idx;
                setSelected(entries.get(idx));
                return true;
            }
            return false;
        }

        // ✅ NeoForge 1.21.1: mouseScrolled has 4 params (scrollX, scrollY)
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (!this.isMouseOver(mouseX, mouseY)) return false;

            // scrollY is usually +/-1
            scroll -= (float) (scrollY * 18.0);
            clampScroll();
            return true;
        }

        private void clampScroll() {
            int totalH = entries.size() * ROW_H;
            float max = Math.max(0, totalH - height);
            scroll = Mth.clamp(scroll, 0, max);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, Component.literal("Unlock list"));
        }
    }

    // --- Drawing helpers ---
    private void drawCard(GuiGraphics gfx, int x, int y, int w, int h) {
        // Shadow
        gfx.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x44000000);
        // Main Background
        gfx.fill(x, y, x + w, y + h, 0xFF0B0F16);
        // Outer Border
        int border = 0xFF373737;
        gfx.fill(x, y, x + w, y + 1, border); // top
        gfx.fill(x, y + h - 1, x + w, y + h, border); // bottom
        gfx.fill(x, y, x + 1, y + h, border); // left
        gfx.fill(x + w - 1, y, x + w, y + h, border); // right
    }

    private void drawSubCard(GuiGraphics gfx, int x, int y, int w, int h) {
        // Recessed background
        gfx.fill(x, y, x + w, y + h, 0xFF111318);
        // Highlight/Shade for depth
        int highlight = 0xFF1A1F27;
        int shadow = 0xFF080A0E;
        gfx.fill(x, y, x + w, y + 1, shadow); // top
        gfx.fill(x, y + h - 1, x + w, y + h, highlight); // bottom
        gfx.fill(x, y, x + 1, y + h, shadow); // left
        gfx.fill(x + w - 1, y, x + w, y + h, highlight); // right
    }

    // --- Coin count (client inventory) ---
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
}
