package com.devoid.roseequipment.render;

import com.devoid.roseequipment.config.RoseConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentOverlayRenderer {
    private static final int LINE_SPACING = 10;

    private EquipmentOverlayRenderer() {
    }

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(EquipmentOverlayRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        if (!RoseConfig.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        PoseStack poseStack = context.poseStack();

        double maxDistanceSq = (double) RoseConfig.maxDistance * RoseConfig.maxDistance;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player || player.isRemoved()) continue;
            if (player.distanceToSqr(mc.player) > maxDistanceSq) continue;

            List<String> hands = new ArrayList<>();
            List<String> armor = new ArrayList<>();

            if (RoseConfig.showMainHand) addItem(hands, "Main", player.getMainHandItem());
            if (RoseConfig.showOffHand) addItem(hands, "Off", player.getOffhandItem());

            if (RoseConfig.showArmor) {
                addItem(armor, "Head", player.getItemBySlot(EquipmentSlot.HEAD));
                addItem(armor, "Chest", player.getItemBySlot(EquipmentSlot.CHEST));
                addItem(armor, "Legs", player.getItemBySlot(EquipmentSlot.LEGS));
                addItem(armor, "Feet", player.getItemBySlot(EquipmentSlot.FEET));
            }

            if (hands.isEmpty() && armor.isEmpty()) continue;

            double x = player.getX() - cameraPos.x;
            double y = player.getY() + player.getBbHeight() + 0.55 - cameraPos.y;
            double z = player.getZ() - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            int offset = 0;

            if (!hands.isEmpty()) {
                context.submitNodeCollector().submitNameTag(
                        poseStack,
                        Vec3.ZERO,
                        offset,
                        roseText(String.join("  |  ", hands)),
                        true,
                        0xF000F0,
                        context.levelState().cameraRenderState
                );
                offset -= LINE_SPACING;
            }

            if (!armor.isEmpty()) {
                context.submitNodeCollector().submitNameTag(
                        poseStack,
                        Vec3.ZERO,
                        offset,
                        roseText(String.join("  |  ", armor)),
                        true,
                        0xF000F0,
                        context.levelState().cameraRenderState
                );
            }

            poseStack.popPose();
        }
    }

    private static void addItem(List<String> output, String label, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        String count = stack.getCount() > 1 ? " x" + stack.getCount() : "";
        output.add(label + ": " + stack.getHoverName().getString() + count);
    }

    private static Component roseText(String value) {
        return Component.literal(value).withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
