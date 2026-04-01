package com.squidpowered.squidchat.config;

public class SquidChatConfig {

    public boolean toneEnabled = true;
    public float toneVolume = 1.0f;

    public int chatX = 0;
    public int chatY = 0;
    public int chatWidth = 0;
    public int chatHeight = 0;

    public boolean chatVisible = true;

    public SquidChatConfig() {
    }

    public void resetPosition() {
        chatX = 0;
        chatY = 0;
        chatWidth = 0;
        chatHeight = 0;
    }
}
