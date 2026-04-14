package com.squidpowered.squidchat.sound;

import com.squidpowered.squidchat.SquidChat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class SquidChatSounds {

    public static final Identifier CHAT_NOTIFICATION_ID =
            Identifier.fromNamespaceAndPath(SquidChat.MOD_ID, "chat_notification");

    public static final SoundEvent CHAT_NOTIFICATION =
            SoundEvent.createVariableRangeEvent(CHAT_NOTIFICATION_ID);

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, CHAT_NOTIFICATION_ID, CHAT_NOTIFICATION);
    }
}
