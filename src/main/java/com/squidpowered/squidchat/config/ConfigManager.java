package com.squidpowered.squidchat.config;

import com.squidpowered.squidchat.SquidChat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("squidchat.json");

    private static SquidChatConfig config = new SquidChatConfig();

    @SuppressWarnings("null")
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                SquidChatConfig loaded = GSON.fromJson(json, SquidChatConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                SquidChat.LOGGER.warn("Failed to load config, using defaults", e);
                config = new SquidChatConfig();
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            SquidChat.LOGGER.error("Failed to save config", e);
        }
    }

    public static SquidChatConfig getConfig() {
        return config;
    }
}
