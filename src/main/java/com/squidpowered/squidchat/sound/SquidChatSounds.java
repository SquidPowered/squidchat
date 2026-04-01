package com.squidpowered.squidchat.sound;

import com.squidpowered.squidchat.SquidChat;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SquidChatSounds {

    public static final Identifier CHAT_NOTIFICATION_ID =
            Identifier.of(SquidChat.MOD_ID, "chat_notification");

    public static final SoundEvent CHAT_NOTIFICATION =
            SoundEvent.of(CHAT_NOTIFICATION_ID);

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, CHAT_NOTIFICATION_ID, CHAT_NOTIFICATION);
    }
}
