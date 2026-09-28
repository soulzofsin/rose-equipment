package com.devoid.roseequipment;

import com.devoid.roseequipment.config.RoseConfig;
import com.devoid.roseequipment.gui.RoseMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public final class RoseEquipmentClient implements ClientModInitializer {
    private static int openDelayTicks = -1;
    private static boolean commandWasCaught = false;
    private static KeyMapping debugOpenKey;

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

        debugOpenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.roseequipment.open",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_F8,
                KeyMapping.Category.MISC
        ));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("[Rose] Rose Equipment loaded. Use /rose or press F8.")
                    );
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (debugOpenKey != null && debugOpenKey.consumeClick()) {
                openDelayTicks = -1;
                commandWasCaught = false;
                client.setScreenAndShow(new RoseMenuScreen(null));
            }

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
                    client.player.sendSystemMessage(
                            Component.literal("[Rose] Command caught; opening equipment menu.")
                    );
                }

                commandWasCaught = false;
                client.setScreenAndShow(new RoseMenuScreen(null));
            } catch (Throwable throwable) {
                commandWasCaught = false;

                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("[Rose] Menu error: "
                                    + throwable.getClass().getSimpleName()
                                    + ": "
                                    + String.valueOf(throwable.getMessage()))
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
        openDelayTicks = 2;
    }
}
