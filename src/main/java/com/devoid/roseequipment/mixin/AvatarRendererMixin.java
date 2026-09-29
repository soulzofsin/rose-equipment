package com.devoid.roseequipment.mixin;

import com.devoid.roseequipment.config.RoseConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class AvatarRendererMixin {
    private static final int LINE_SPACING = 10;

    /*
     * Minecraft 26.2's actual per-entity submit method lives on
     * LivingEntityRenderer, not AvatarRenderer. Injecting here guarantees this
     * runs for every rendered player even if a server/client changes nametags.
     */
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void roseEquipment$submitEquipment(
            LivingEntityRenderState livingState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!RoseConfig.enabled) return;
        if (!(livingState instanceof AvatarRenderState state)) return;

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

        if (hands.isEmpty() && armor.isEmpty()) return;

        poseStack.pushPose();

        // Negative offsets stack lines upward above the player's normal nametag.
        int offset = -LINE_SPACING;

        if (!hands.isEmpty()) {
            collector.submitNameTag(
                    poseStack,
                    new Vec3(0.0, state.boundingBoxHeight + 0.55, 0.0),
                    offset,
                    roseText(String.join("  |  ", hands)),
                    true,
                    state.lightCoords,
                    camera
            );
            offset -= LINE_SPACING;
        }

        if (!armor.isEmpty()) {
            collector.submitNameTag(
                    poseStack,
                    new Vec3(0.0, state.boundingBoxHeight + 0.55, 0.0),
                    offset,
                    roseText(String.join("  |  ", armor)),
                    true,
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
