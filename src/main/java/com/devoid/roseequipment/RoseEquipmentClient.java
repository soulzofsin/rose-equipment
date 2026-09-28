package com.devoid.roseequipment;

import com.devoid.roseequipment.config.RoseConfig;
import com.devoid.roseequipment.gui.RoseMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class RoseEquipmentClient implements ClientModInitializer {
    private static int openDelayTicks = -1;
    private static boolean commandWasCaught = false;

    @Override
    public void onInitializeClient() {
        RoseConfig.load();

        // Normal Fabric client commands.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("rose")
                    .executes(context -> queueMenu()));

            dispatcher.register(ClientCommands.literal("rosemenu")
                    .executes(context -> queueMenu()));
        });

        /*
         * Fallback for clients / servers with unusual command routing.
         * Fabric's ALLOW_COMMAND runs when a command is about to be sent and
         * receives the command WITHOUT the leading slash.
         *
         * If Magnolia/OneClient tries to forward /rose to the server instead of
         * executing the Fabric client command normally, this catches it first,
         * prevents it from reaching the server, and opens our menu locally.
         */
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            String normalized = command.trim();

            if (normalized.equalsIgnoreCase("rose")
                    || normalized.equalsIgnoreCase("rosemenu")) {
                commandWasCaught = true;
                scheduleMenuOpen();
                return false;
            }

            return true;
        });

        /*
         * Wait a couple of ticks before opening.
         * This avoids the vanilla/OneClient chat screen closing immediately
         * after Enter is pressed and replacing our custom screen.
         */
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openDelayTicks < 0) {
                return;
            }

            if (openDelayTicks > 0) {
                openDelayTicks--;
                return;
            }

            openDelayTicks = -1;

            try {
                if (client.player != null && commandWasCaught) {
                    client.player.displayClientMessage(
                            Component.literal("[Rose] Opening equipment menu..."),
                            false
                    );
                }

                commandWasCaught = false;
                client.gui.setScreen(new RoseMenuScreen(null));
            } catch (Throwable throwable) {
                commandWasCaught = false;

                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.literal("[Rose] Menu error: "
                                    + throwable.getClass().getSimpleName()
                                    + ": "
                                    + String.valueOf(throwable.getMessage())),
                            false
                    );
                }

                throwable.printStackTrace();
            }
        });
    }

    private static int queueMenu() {
        commandWasCaught = true;
        scheduleMenuOpen();
        return 1;
    }

    private static void scheduleMenuOpen() {
        // Two full ticks gives chat/command UIs time to finish closing.
        openDelayTicks = 2;
    }
}
