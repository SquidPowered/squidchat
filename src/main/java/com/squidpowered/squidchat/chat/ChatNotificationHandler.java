package com.squidpowered.squidchat.chat;

import com.squidpowered.squidchat.config.ConfigManager;
import com.squidpowered.squidchat.config.SquidChatConfig;
import com.squidpowered.squidchat.sound.SquidChatSounds;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class ChatNotificationHandler {

    public static void register() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            playNotificationIfEnabled();
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                playNotificationIfEnabled();
            }
        });
    }

    private static void playNotificationIfEnabled() {
        SquidChatConfig config = ConfigManager.getConfig();
        if (!config.toneEnabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.getSoundManager() == null) return;

        client.getSoundManager().play(
                SimpleSoundInstance.forUI(
                        SquidChatSounds.CHAT_NOTIFICATION,
                        1.0f,
                        config.toneVolume
                )
        );
    }
}
