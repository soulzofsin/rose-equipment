package com.devoid.roseequipment;

import com.devoid.roseequipment.config.RoseConfig;
import com.devoid.roseequipment.gui.RoseMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.network.chat.Component;

public final class RoseEquipmentClient implements ClientModInitializer {
    private static int openDelayTicks = -1;
    private static boolean commandWasCaught = false;

    @Override
    public void onInitializeClient() {
        RoseConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("rose").executes(context -> queueMenu()));
            dispatcher.register(ClientCommands.literal("rosemenu").executes(context -> queueMenu()));
        });

        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            String normalized = command.trim();
            if (normalized.equalsIgnoreCase("rose") || normalized.equalsIgnoreCase("rosemenu")) {
                commandWasCaught = true;
                scheduleMenuOpen();
                return false;
            }
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openDelayTicks < 0) return;
            if (openDelayTicks > 0) {
                openDelayTicks--;
                return;
            }
            openDelayTicks = -1;
            try {
                if (client.player != null && commandWasCaught) {
                    client.player.sendSystemMessage(Component.literal("[Rose] Opening equipment menu..."));
                }
                commandWasCaught = false;
                client.gui.setScreen(new RoseMenuScreen(null));
            } catch (Throwable throwable) {
                commandWasCaught = false;
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("[Rose] Menu error: " + throwable.getClass().getSimpleName() + ": " + String.valueOf(throwable.getMessage())));
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
        openDelayTicks = 2;
    }
}
