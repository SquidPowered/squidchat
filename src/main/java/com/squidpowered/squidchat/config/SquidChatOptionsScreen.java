package com.squidpowered.squidchat.config;

import com.mojang.serialization.Codec;
import com.squidpowered.squidchat.chat.ChatWindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SquidChatOptionsScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("title.squidchat.options");
    private static final Component SQUIDCHAT_HEADER = Component.translatable("category.squidchat");
    private static final Component VANILLA_HEADER = Component.translatable("category.squidchat.vanilla_chat");
    private static final Component RESET_POSITION = Component.translatable("option.squidchat.reset_position");
    private static final Component RESET_POSITION_TOOLTIP = Component.translatable("tooltip.squidchat.reset_position");

    private final SquidChatConfig config;
    private final OptionInstance<Boolean> toneEnabled;
    private final OptionInstance<Integer> toneVolume;

    public SquidChatOptionsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
        this.config = ConfigManager.getConfig();
        this.toneEnabled = OptionInstance.createBoolean(
                "option.squidchat.tone_enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("tooltip.squidchat.tone_enabled")),
                config.toneEnabled,
                value -> config.toneEnabled = value
        );
        this.toneVolume = new OptionInstance<>(
                "option.squidchat.tone_volume",
                OptionInstance.cachedConstantTooltip(Component.translatable("tooltip.squidchat.tone_volume")),
                (caption, value) -> Component.translatable("options.percent_value", caption, value),
                new OptionInstance.IntRange(0, 100),
                Codec.INT,
                Math.round(config.toneVolume * 100.0f),
                value -> config.toneVolume = value / 100.0f
        );
    }

    @Override
    protected void addOptions() {
        list.addHeader(SQUIDCHAT_HEADER);
        list.addSmall(toneEnabled, toneVolume);
        list.addSmall(List.of(createResetPositionButton()));

        list.addHeader(VANILLA_HEADER);
        list.addSmall(
                options.chatVisibility(),
                options.chatColors(),
                options.chatLinks(),
                options.chatLinksPrompt(),
                options.chatOpacity(),
                options.textBackgroundOpacity(),
                options.chatLineSpacing(),
                options.chatDelay(),
                options.narrator(),
                options.autoSuggestions(),
                options.hideMatchedNames(),
                options.reducedDebugInfo(),
                options.onlyShowSecureChat(),
                options.saveChatDrafts()
        );
    }

    @Override
    public void removed() {
        super.removed();
        ConfigManager.save();
    }

    private AbstractWidget createResetPositionButton() {
        Button button = Button.builder(RESET_POSITION, widget -> resetChatPlacement())
                .tooltip(Tooltip.create(RESET_POSITION_TOOLTIP))
                .width(Button.SMALL_WIDTH)
                .build();
        return button;
    }

    private void resetChatPlacement() {
        config.resetPosition();

        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager != null) {
            manager.loadFromConfig();
            manager.resetChatScale();
        }

        ConfigManager.save();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui != null) {
            minecraft.gui.hud.getChat().rescaleChat();
        }
    }
}
