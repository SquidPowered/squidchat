package com.squidpowered.squidchat;

import com.squidpowered.squidchat.sound.SquidChatSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquidChat implements ModInitializer {

    public static final String MOD_ID = "squidchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SquidChatSounds.register();
        LOGGER.info("SquidChat initialized");
    }
}
