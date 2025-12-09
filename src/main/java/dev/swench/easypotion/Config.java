package dev.swench.easypotion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class Config {
    public static boolean enabled = false;
    public static int slot = 3;
    public static boolean randomize = false;
    public static boolean onlyOnThrow = false;
    public static KeyBinding configKeyBinding;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    private static class ConfigData {
        boolean enabled = true;
        int slot = 3;
        boolean randomize = true;
        boolean onlyOnThrow = false;
    }

    public static void init() {
        if (configFile == null) {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.runDirectory != null) {
                    Path configDir = client.runDirectory.toPath().resolve("config");
                    configDir.toFile().mkdirs();
                    configFile = configDir.resolve("easypotion.json").toFile();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void save() {
        init();
        try {
            ConfigData data = new ConfigData();
            data.enabled = enabled;
            data.slot = slot;
            data.randomize = randomize;
            data.onlyOnThrow = onlyOnThrow;

            FileWriter writer = new FileWriter(configFile);
            GSON.toJson(data, writer);
            writer.close();
        } catch (IOException e) {
        }
    }

    public static void load() {
        init();
        if (!configFile.exists()) {
            enabled = true;
            slot = 3;
            randomize = true;
            onlyOnThrow = false;

            save();
            return;
        }

        try {
            FileReader reader = new FileReader(configFile);
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            reader.close();

            if (data != null) {
                enabled = data.enabled;
                slot = Math.max(1, Math.min(9, data.slot));
                randomize = data.randomize;
                onlyOnThrow = data.onlyOnThrow;
            }
        } catch (IOException e) {
        }
    }
}

