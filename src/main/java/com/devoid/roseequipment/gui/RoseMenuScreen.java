package com.devoid.roseequipment.gui;

import com.devoid.roseequipment.config.RoseConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RoseMenuScreen extends Screen {
    private static final int ROSE = 0xFFFF76A6;
    private static final int SOFT_ROSE = 0xFFFFB7CD;
    private static final int TEXT = 0xFFFFEDF3;
    private static final int DARK = 0xFF2B101A;

    private final Screen parent;

    public RoseMenuScreen(Screen parent) {
        super(Component.literal("Rose Equipment"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int left = cx - 105;
        int y = height / 2 - 58;

        addRenderableWidget(toggle(left, y, "Equipment Display", () -> RoseConfig.enabled, v -> RoseConfig.enabled = v));
        y += 24;
        addRenderableWidget(toggle(left, y, "Main Hand", () -> RoseConfig.showMainHand, v -> RoseConfig.showMainHand = v));
        y += 24;
        addRenderableWidget(toggle(left, y, "Off Hand", () -> RoseConfig.showOffHand, v -> RoseConfig.showOffHand = v));
        y += 24;
        addRenderableWidget(toggle(left, y, "Armor", () -> RoseConfig.showArmor, v -> RoseConfig.showArmor = v));
        y += 24;

        addRenderableWidget(Button.builder(distanceText(), button -> {
            int next = RoseConfig.maxDistance + 8;
            RoseConfig.maxDistance = next > 64 ? 16 : next;
            RoseConfig.save();
            button.setMessage(distanceText());
        }).bounds(left, y, 210, 20).build());

        y += 28;
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(cx - 55, y, 110, 20).build());
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
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int cx = width / 2;
        int top = height / 2 - 108;

        graphics.fill(cx - 126, top, cx + 126, height / 2 + 108, DARK);
        graphics.text(font, "✿ Rose Equipment ✿", cx - 52, top + 14, ROSE, true);
        graphics.text(font, "Client-side equipment display", cx - 74, top + 30, SOFT_ROSE, false);
        graphics.text(font, "Open anytime with /rose", cx - 61, height / 2 + 91, TEXT, false);
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
