package com.devoid.roseequipment;

import com.devoid.roseequipment.config.RoseConfig;
import com.devoid.roseequipment.gui.RoseMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RoseEquipmentClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("RoseEquipment");

    private static boolean announcedLoaded = false;
    private static boolean f8WasDown = false;

    @Override
    public void onInitializeClient() {
        RoseConfig.load();
        LOGGER.info("Rose Equipment 1.0.1 client initializer started");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("rose")
                    .executes(context -> {
                        context.getSource().sendFeedback(
                                Component.literal("[Rose] /rose received.")
                        );
                        openMenuSoon();
                        return 1;
                    }));

            dispatcher.register(ClientCommands.literal("rosemenu")
                    .executes(context -> {
                        context.getSource().sendFeedback(
                                Component.literal("[Rose] /rosemenu received.")
                        );
                        openMenuSoon();
                        return 1;
                    }));

            dispatcher.register(ClientCommands.literal("roseping")
                    .executes(context -> {
                        context.getSource().sendFeedback(
                                Component.literal("[Rose] Rose Equipment 1.0.1 is loaded.")
                        );
                        return 1;
                    }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!announcedLoaded && client.player != null && client.level != null) {
                announcedLoaded = true;
                client.player.sendSystemMessage(
                        Component.literal("[Rose] Rose Equipment 1.0.1 loaded. Use /rose, /roseping, or F8.")
                );
            }

            long window = client.getWindow().handle();
            boolean f8Down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F8) == GLFW.GLFW_PRESS;

            if (f8Down && !f8WasDown) {
                LOGGER.info("F8 pressed - opening Rose menu");
                openMenu(client);
            }

            f8WasDown = f8Down;
        });
    }

    private static void openMenuSoon() {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> openMenu(client));
    }

    private static void openMenu(Minecraft client) {
        try {
            LOGGER.info("Opening Rose Equipment screen");
            client.setScreenAndShow(new RoseMenuScreen(null));
        } catch (Throwable throwable) {
            LOGGER.error("Could not open Rose Equipment screen", throwable);

            if (client.player != null) {
                client.player.sendSystemMessage(
                        Component.literal("[Rose] Menu error: "
                                + throwable.getClass().getSimpleName()
                                + ": "
                                + String.valueOf(throwable.getMessage()))
                );
            }
        }
    }
}
