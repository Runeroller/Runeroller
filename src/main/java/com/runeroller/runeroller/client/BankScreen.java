package com.runeroller.runeroller.client;

import com.runeroller.runeroller.bank.BankMenu;
import com.runeroller.runeroller.network.BankScrollC2SPayload;
import com.runeroller.runeroller.network.BankSearchC2SPayload;
import com.runeroller.runeroller.network.RequestBankStateC2SPayload;
import com.runeroller.runeroller.network.SortBankC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class BankScreen extends AbstractContainerScreen<BankMenu> {

    private EditBox search;
    private boolean draggingScroll = false;

    public BankScreen(BankMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        // Wider size: 12 columns * 18 + edges
        this.imageWidth = 230;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;

        // request bank state for shop labels (slots/stack)
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new RequestBankStateC2SPayload());
        }

        // search box
        int sx = leftPos + 10;
        int sy = topPos + 6;
        this.search = new EditBox(this.font, sx, sy, 140, 10, Component.literal("Search"));
        this.search.setBordered(false);
        this.search.setMaxLength(32);
        this.search.setTextColor(0xFFFFFFFF);
        this.search.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.search);

        // ✅ Sort button
        this.addRenderableWidget(Button.builder(Component.literal("S"), b -> {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null) {
                Minecraft.getInstance().player.connection.send(new SortBankC2SPayload());
            }
        }).bounds(leftPos + 155, topPos + 4, 16, 12).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Sort Bank"))).build());

        // ✅ Back button to menu
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            Minecraft.getInstance().setScreen(new RunerollerMenuScreen());
        }).bounds(leftPos + 210, topPos + 4, 16, 12).build());
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        // Main outer border
        gfx.fill(leftPos - 1, topPos - 1, leftPos + imageWidth + 1, topPos + imageHeight + 1, 0xFF373737);
        // clean dark background panel
        gfx.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF0B0F16);

        // Header bars for visual separation
        // Bank storage header
        gfx.fill(leftPos + 6, topPos + 18, leftPos + imageWidth - 6, topPos + 30, 0xFF1C222D);
        // Bank slots area background
        gfx.fill(leftPos + 6, topPos + 30, leftPos + imageWidth - 6, topPos + 138, 0xFF0B0F16);

        // Draw Bank Slots (12x6)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 12; col++) {
                drawSlot(gfx, leftPos + 8 - 1 + col * 18, topPos + 30 - 1 + row * 18);
            }
        }

        // Inventory header
        gfx.fill(leftPos + 6, topPos + 130, leftPos + imageWidth - 6, topPos + 142, 0xFF1C222D);
        // Inventory panel background
        gfx.fill(leftPos + 6, topPos + 142, leftPos + imageWidth - 6, topPos + 218, 0xFF0B0F16);

        // Draw Player Inventory (9x3) - Centered in wide GUI
        int invStartX = 8 + (imageWidth - 176) / 2;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(gfx, leftPos + invStartX - 1 + col * 18, topPos + 142 - 1 + row * 18);
            }
        }
        // Draw Hotbar (9x1)
        for (int col = 0; col < 9; col++) {
            drawSlot(gfx, leftPos + invStartX - 1 + col * 18, topPos + 200 - 1);
        }

        // Highlight the search area a bit
        gfx.fill(leftPos + 6, topPos + 4, leftPos + 152, topPos + 17, 0x44FFFFFF);

        renderScrollbar(gfx);
    }

    private void drawSlot(GuiGraphics gfx, int x, int y) {
        // Outer border for the slot
        gfx.fill(x, y, x + 18, y + 18, 0xFF373737);
        // Recessed inner background (slightly lighter than main bg for visibility)
        gfx.fill(x + 1, y + 1, x + 17, y + 17, 0xFF080A0E);
        
        // 3D Bevel effect - Highlights (Top & Left)
        gfx.fill(x, y, x + 18, y + 1, 0xFF121212); // Top shadow (recessed)
        gfx.fill(x, y, x + 1, y + 18, 0xFF121212); // Left shadow (recessed)
        
        // 3D Bevel effect - Lowlights (Bottom & Right)
        gfx.fill(x + 1, y + 17, x + 18, y + 18, 0xFF3D3D3D); // Bottom highlight
        gfx.fill(x + 17, y + 1, x + 18, y + 18, 0xFF3D3D3D); // Right highlight
    }

    private void renderSlotHighlight(GuiGraphics gfx, int x, int y) {
        // Subtle blue-ish highlight on hover
        gfx.fill(x + 1, y + 1, x + 17, y + 17, 0x334C89FF);
    }

    private void renderScrollbar(GuiGraphics gfx) {
        int total = menu.getBankSlotsTotal();
        if (total <= BankMenu.VISIBLE_SLOTS) return;

        int x = this.leftPos + this.imageWidth - 10;
        int y = this.topPos + 18;
        int h = 108;

        // track with border
        gfx.fill(x - 1, y - 1, x + 7, y + h + 1, 0xFF373737);
        gfx.fill(x, y, x + 6, y + h, 0xFF080A0E);

        int max = menu.getMaxRowOffset();
        int cur = menu.getRowOffset();

        int knobH = Math.max(14, (int)(h * (BankMenu.ROWS_VISIBLE / (float)Math.ceil(total / 9.0))));
        float t = (max == 0) ? 0f : (cur / (float)max);
        int knobY = y + (int)((h - knobH) * t);

        // Knob with 3D look
        gfx.fill(x + 1, knobY, x + 5, knobY + knobH, 0xFF4C89FF);
        gfx.fill(x + 1, knobY, x + 5, knobY + 1, 0xFF8DBBFF); // knob highlight
        gfx.fill(x + 1, knobY + knobH - 1, x + 5, knobY + knobH, 0xFF2A5BBF); // knob shadow
    }

    private boolean isMouseOverBankArea(double mouseX, double mouseY) {
        int bankX = this.leftPos + 6;
        int bankY = this.topPos + 18;
        int bankW = this.imageWidth - 12; // Adjusted for wider GUI
        int bankH = 108;
        return mouseX >= bankX && mouseX < bankX + bankW && mouseY >= bankY && mouseY < bankY + bankH;
    }

    // IMPORTANT: 1.21.1 signature uses (mouseX, mouseY, scrollX, scrollY)
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int total = menu.getBankSlotsTotal();
        if (total <= BankMenu.VISIBLE_SLOTS) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        if (isMouseOverBankArea(mouseX, mouseY)) {
            int step = (scrollY > 0) ? -1 : 1;
            sendScroll(menu.getRowOffset() + step);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        // scrollbar drag start
        int total = menu.getBankSlotsTotal();
        if (total > BankMenu.VISIBLE_SLOTS) {
            int x = this.leftPos + this.imageWidth - 10;
            int y = this.topPos + 18;
            int h = 108;

            if (mouseX >= x && mouseX <= x + 6 && mouseY >= y && mouseY <= y + h) {
                draggingScroll = true;
                updateScrollFromMouse(mouseY);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingScroll) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateScrollFromMouse(double mouseY) {
        int total = menu.getBankSlotsTotal();
        if (total <= BankMenu.VISIBLE_SLOTS) return;

        int y = this.topPos + 18;
        int h = 108;

        int max = menu.getMaxRowOffset();
        if (max <= 0) return;

        float t = (float)((mouseY - y) / (double)h);
        t = Mth.clamp(t, 0f, 1f);

        int newOffset = Math.round(t * max);
        sendScroll(newOffset);
    }

    private void onSearchChanged(String newQuery) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new BankSearchC2SPayload(newQuery));
        }
    }

    private void sendScroll(int newOffset) {
        newOffset = Mth.clamp(newOffset, 0, menu.getMaxRowOffset());

        // client prediction
        menu.setRowOffset(newOffset);

        // server sync
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new BankScrollC2SPayload(newOffset));
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);

        // Render slot highlight on hover
        if (this.hoveredSlot != null) {
            renderSlotHighlight(gfx, leftPos + hoveredSlot.x, topPos + hoveredSlot.y);
        }

        // search overlay hint (if empty)
        if (this.search.getValue().isEmpty() && !this.search.isFocused()) {
            gfx.drawString(this.font, "Search items...", leftPos + 10, topPos + 7, 0xFFAAAAAA, false);
        }

        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // We do NOT call super.renderLabels(gfx, mouseX, mouseY) 
        // because we want custom placement for Title and Inventory labels.

        // Bank Storage title (on its header)
        gfx.drawString(this.font, Component.literal("Bank Storage"), 8, 18 + 2, 0xFFFFFFFF, false);
        
        // Inventory title (on its header)
        gfx.drawString(this.font, Component.literal("Inventory"), 8, 130 + 2, 0xFFFFFFFF, false);

        // Bank Info (Slots | Stack) - moved slightly to the right of "Inventory" label to avoid overlap if the screen is narrow
        String info = RunerollerClientBankState.slots() + " Slots | Max Stack: " + RunerollerClientBankState.stackLimit();
        int infoX = this.imageWidth - 8 - this.font.width(info);
        
        // Ensure infoX doesn't collide with "Inventory" (width ~50)
        int minX = 8 + this.font.width("Inventory") + 10;
        if (infoX < minX) infoX = minX;

        gfx.drawString(this.font, info, infoX, 130 + 2, 0xFF5CABFF, false);
    }
}
