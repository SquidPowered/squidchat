package com.squidpowered.squidchat.chat;

import com.squidpowered.squidchat.config.ConfigManager;
import com.squidpowered.squidchat.config.SquidChatConfig;
import com.squidpowered.squidchat.sound.SquidChatSounds;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;

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

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSoundManager() == null) return;

        client.getSoundManager().play(
                PositionedSoundInstance.ui(
                        SquidChatSounds.CHAT_NOTIFICATION,
                        1.0f,
                        config.toneVolume
                )
        );
    }
}
