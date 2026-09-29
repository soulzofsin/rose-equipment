package com.devoid.roseequipment.gui;

import com.devoid.roseequipment.config.RoseConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RoseMenuScreen extends Screen {
    private static final int ROSE = 0xFFFF76A6;
    private static final int ROSE_LIGHT = 0xFFFFB7CD;
    private static final int ROSE_DARK = 0xFF9F315A;
    private static final int TEXT = 0xFFFFEDF3;
    private static final int MUTED = 0xFFDDA4BA;
    private static final int VINE = 0xFF4E8A55;
    private static final int LEAF = 0xFF79B07E;
    private static final int PANEL = 0xE82B101A;
    private static final int PANEL_EDGE = 0xF0451628;

    private final Screen parent;

    public RoseMenuScreen(Screen parent) {
        super(Component.literal("Rose Equipment"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();

        int cx = width / 2;
        int left = cx - 105;
        int y = height / 2 - 58;

        addRenderableWidget(toggle(left, y, "Equipment Display",
                () -> RoseConfig.enabled,
                value -> RoseConfig.enabled = value));
        y += 24;

        addRenderableWidget(toggle(left, y, "Main Hand",
                () -> RoseConfig.showMainHand,
                value -> RoseConfig.showMainHand = value));
        y += 24;

        addRenderableWidget(toggle(left, y, "Off Hand",
                () -> RoseConfig.showOffHand,
                value -> RoseConfig.showOffHand = value));
        y += 24;

        addRenderableWidget(toggle(left, y, "Armor",
                () -> RoseConfig.showArmor,
                value -> RoseConfig.showArmor = value));
        y += 24;

        addRenderableWidget(Button.builder(distanceText(), button -> {
            int next = RoseConfig.maxDistance + 8;
            RoseConfig.maxDistance = next > 64 ? 16 : next;
            RoseConfig.save();
            button.setMessage(distanceText());
        }).bounds(left, y, 210, 20).build());

        y += 28;

        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(cx - 55, y, 110, 20)
                .build());
    }

    private Button toggle(int x, int y, String label, BoolGetter getter, BoolSetter setter) {
        return Button.builder(toggleText(label, getter.get()), button -> {
            boolean value = !getter.get();
            setter.set(value);
            RoseConfig.save();
            button.setMessage(toggleText(label, value));
        }).bounds(x, y, 210, 20).build();
    }

    private Component toggleText(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    private Component distanceText() {
        return Component.literal("Render Distance: " + RoseConfig.maxDistance + " blocks");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        /*
         * Let Minecraft submit the screen background and all widgets first.
         * Anything we draw after this appears above them, so the rose decoration
         * deliberately stays around the outside and never paints over the
         * center where the buttons are.
         */
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int cx = width / 2;
        int panelX = cx - 136;
        int panelY = height / 2 - 110;
        int panelW = 272;
        int panelH = 220;

        // Header/footer and narrow side rails. The middle remains clear so the
        // vanilla buttons stay fully visible and clickable.
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 43, PANEL);
        graphics.fill(panelX, panelY + panelH - 27, panelX + panelW, panelY + panelH, PANEL);
        graphics.fill(panelX, panelY + 43, panelX + 18, panelY + panelH - 27, PANEL);
        graphics.fill(panelX + panelW - 18, panelY + 43, panelX + panelW, panelY + panelH - 27, PANEL);

        // Thin rose-colored inner edge.
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, PANEL_EDGE);
        graphics.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, PANEL_EDGE);
        graphics.fill(panelX, panelY, panelX + 2, panelY + panelH, PANEL_EDGE);
        graphics.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, PANEL_EDGE);

        graphics.text(font, "✿ Rose Equipment ✿", cx - 52, panelY + 12, ROSE, true);
        graphics.text(font, "Client-side equipment display", cx - 74, panelY + 28, ROSE_LIGHT, false);
        graphics.text(font, "Open anytime with /rose", cx - 61, panelY + panelH - 18, TEXT, false);

        drawVines(graphics, panelX, panelY, panelW, panelH);
    }

    private void drawVines(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        // Left and right climbing vines.
        for (int py = y + 48; py < y + h - 31; py += 12) {
            g.fill(x + 8, py, x + 10, py + 10, VINE);
            g.fill(x + 5, py + 2, x + 8, py + 5, LEAF);
            g.fill(x + 10, py + 6, x + 13, py + 9, LEAF);

            g.fill(x + w - 10, py, x + w - 8, py + 10, VINE);
            g.fill(x + w - 13, py + 6, x + w - 10, py + 9, LEAF);
            g.fill(x + w - 8, py + 2, x + w - 5, py + 5, LEAF);
        }

        // Small vine accents along the header/footer.
        for (int px = x + 24; px < x + w - 24; px += 18) {
            g.fill(px, y + 36, px + 12, y + 38, VINE);
            g.fill(px + 3, y + 33, px + 6, y + 36, LEAF);

            g.fill(px, y + h - 25, px + 12, y + h - 23, VINE);
            g.fill(px + 6, y + h - 23, px + 9, y + h - 20, LEAF);
        }

        drawRose(g, x + 14, y + 14);
        drawRose(g, x + w - 14, y + 14);
        drawRose(g, x + 14, y + h - 14);
        drawRose(g, x + w - 14, y + h - 14);

        // Two little flowers midway down each side.
        drawSmallRose(g, x + 9, y + h / 2);
        drawSmallRose(g, x + w - 9, y + h / 2);
    }

    private void drawRose(GuiGraphicsExtractor g, int cx, int cy) {
        g.fill(cx - 3, cy - 3, cx + 4, cy + 4, ROSE_DARK);
        g.fill(cx - 6, cy - 2, cx - 3, cy + 3, ROSE_LIGHT);
        g.fill(cx + 4, cy - 2, cx + 7, cy + 3, ROSE_LIGHT);
        g.fill(cx - 2, cy - 6, cx + 3, cy - 3, ROSE_LIGHT);
        g.fill(cx - 2, cy + 4, cx + 3, cy + 7, ROSE_LIGHT);
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, ROSE);
    }

    private void drawSmallRose(GuiGraphicsExtractor g, int cx, int cy) {
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, ROSE);
        g.fill(cx - 4, cy, cx - 2, cy + 2, ROSE_LIGHT);
        g.fill(cx + 3, cy, cx + 5, cy + 2, ROSE_LIGHT);
        g.fill(cx, cy - 4, cx + 2, cy - 2, ROSE_LIGHT);
    }

    @Override
    public void onClose() {
        RoseConfig.save();
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }

    @FunctionalInterface
    private interface BoolGetter {
        boolean get();
    }

    @FunctionalInterface
    private interface BoolSetter {
        void set(boolean value);
    }
}
