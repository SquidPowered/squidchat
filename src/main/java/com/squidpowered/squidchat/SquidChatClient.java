package com.squidpowered.squidchat;

import com.squidpowered.squidchat.chat.ChatNotificationHandler;
import com.squidpowered.squidchat.chat.ChatWindowManager;
import com.squidpowered.squidchat.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SquidChatClient implements ClientModInitializer {

    public static KeyBinding toggleChatKeyBinding;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ChatWindowManager.init();

        toggleChatKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.squidchat.toggle_chat",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KeyBinding.Category.create(Identifier.of(SquidChat.MOD_ID, "general"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleChatKeyBinding.wasPressed()) {
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
