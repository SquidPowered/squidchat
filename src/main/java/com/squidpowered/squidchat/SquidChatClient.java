package com.squidpowered.squidchat;

import com.squidpowered.squidchat.chat.ChatNotificationHandler;
import com.squidpowered.squidchat.chat.ChatWindowManager;
import com.squidpowered.squidchat.config.ConfigManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SquidChatClient implements ClientModInitializer {

    public static KeyMapping toggleChatKeyBinding;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ChatWindowManager.init();

        toggleChatKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.squidchat.toggle_chat",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SquidChat.MOD_ID, "general"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleChatKeyBinding.consumeClick()) {
                ChatWindowManager manager = ChatWindowManager.getInstance();
                manager.setChatVisible(!manager.isChatVisible());
                ConfigManager.getConfig().chatVisible = manager.isChatVisible();
                ConfigManager.save();
            }
        });

        ChatNotificationHandler.register();

        // Register screen events for chat window drag/resize
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen) {
                ScreenMouseEvents.allowMouseDrag(screen).register((scr, click, dx, dy) -> {
                    ChatWindowManager manager = ChatWindowManager.getInstance();
                    if (manager == null) return true;

                    if (manager.isDragging()) {
                        manager.updateDrag(click.x(), click.y());
                        return false;
                    } else if (manager.isResizing()) {
                        manager.updateResize(click.x(), click.y());
                        return false;
                    }
                    return true;
                });

                ScreenMouseEvents.beforeMouseRelease(screen).register((scr, click) -> {
                    ChatWindowManager manager = ChatWindowManager.getInstance();
                    if (manager == null) return;

                    if (manager.isDragging() || manager.isResizing()) {
                        manager.endInteraction();
                    }
                });
            }
        });

        SquidChat.LOGGER.info("SquidChat client initialized");
    }
}
