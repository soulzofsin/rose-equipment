package com.devoid.roseequipment.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RoseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("rose-equipment.json");

    public static boolean enabled = true;
    public static boolean showMainHand = true;
    public static boolean showOffHand = true;
    public static boolean showArmor = true;
    public static int maxDistance = 32;

    private RoseConfig() {}

    private static final class Data {
        boolean enabled = true;
        boolean showMainHand = true;
        boolean showOffHand = true;
        boolean showArmor = true;
        int maxDistance = 32;
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                save();
                return;
            }

            try (Reader reader = Files.newBufferedReader(FILE)) {
                Data data = GSON.fromJson(reader, Data.class);
                if (data == null) return;
                enabled = data.enabled;
                showMainHand = data.showMainHand;
                showOffHand = data.showOffHand;
                showArmor = data.showArmor;
                maxDistance = Math.max(8, Math.min(64, data.maxDistance));
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Data data = new Data();
            data.enabled = enabled;
            data.showMainHand = showMainHand;
            data.showOffHand = showOffHand;
            data.showArmor = showArmor;
            data.maxDistance = maxDistance;

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception ignored) {
        }
    }
}
