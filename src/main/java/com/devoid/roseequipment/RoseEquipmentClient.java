package com.devoid.roseequipment;

import com.devoid.roseequipment.config.RoseConfig;
import com.devoid.roseequipment.gui.RoseMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class RoseEquipmentClient implements ClientModInitializer {
    private static boolean openMenuNextTick = false;

    @Override
    public void onInitializeClient() {
        RoseConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("rose")
                    .executes(context -> queueMenu()));

            dispatcher.register(ClientCommands.literal("rosemenu")
                    .executes(context -> queueMenu()));
        });

        // Opening a screen directly from the chat-command callback can be
        // immediately overwritten when Minecraft closes the chat screen.
        // Waiting until the end of the next client tick avoids that race.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!openMenuNextTick) return;

            openMenuNextTick = false;
            client.gui.setScreen(new RoseMenuScreen(null));
        });
    }

    private static int queueMenu() {
        openMenuNextTick = true;
        return 1;
    }
}
