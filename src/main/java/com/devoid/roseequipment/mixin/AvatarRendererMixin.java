package com.devoid.roseequipment.mixin;

import com.devoid.roseequipment.config.RoseConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    private static final int LINE_SPACING = 10;

    @Inject(method = "submitNameDisplay*", at = @At("HEAD"))
    private void roseEquipment$submitEquipment(
            AvatarRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!RoseConfig.enabled) return;

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) return;

        if (!(level.getEntity(state.id) instanceof Player player)) return;
        if (player == minecraft.player) return;

        double maxDistanceSq = (double) RoseConfig.maxDistance * RoseConfig.maxDistance;
        if (player.distanceToSqr(minecraft.player) > maxDistanceSq) return;

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

        /*
         * Magnolia/OneClient can change or suppress the normal player nametag.
         * Running at HEAD means our equipment display is submitted before
         * vanilla/server nametag logic can return early.
         */
        poseStack.pushPose();

        int offset = -LINE_SPACING;

        if (!hands.isEmpty()) {
            collector.submitNameTag(
                    poseStack,
                    state.nameTagAttachment,
                    offset,
                    roseText(String.join("  |  ", hands)),
                    !state.isDiscrete,
                    state.lightCoords,
                    camera
            );
            offset -= LINE_SPACING;
        }

        if (!armor.isEmpty()) {
            collector.submitNameTag(
                    poseStack,
                    state.nameTagAttachment,
                    offset,
                    roseText(String.join("  |  ", armor)),
                    !state.isDiscrete,
                    state.lightCoords,
                    camera
            );
        }

        poseStack.popPose();
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
