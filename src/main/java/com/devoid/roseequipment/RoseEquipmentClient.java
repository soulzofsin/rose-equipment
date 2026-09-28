package com.devoid.roseequipment;

import com.devoid.roseequipment.config.RoseConfig;
import com.devoid.roseequipment.gui.RoseMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;

public final class RoseEquipmentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RoseConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("rose")
                    .executes(context -> openMenu()));
            dispatcher.register(ClientCommands.literal("rosemenu")
                    .executes(context -> openMenu()));
        });
    }

    private static int openMenu() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new RoseMenuScreen(minecraft.gui.screen()));
        return 1;
    }
}
