package com.squidpowered.squidchat.config;

import com.squidpowered.squidchat.chat.ChatWindowManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class ModMenuIntegration implements ModMenuApi {
    private static final int ACTION_BUTTON_WIDTH = 84;
    private static final int ACTION_BUTTON_HEIGHT = 20;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            SquidChatConfig config = ConfigManager.getConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("title.squidchat.config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory general = builder.getOrCreateCategory(
                    Component.translatable("category.squidchat"));

            SubCategoryBuilder sound = entryBuilder.startSubCategory(
                    Component.translatable("category.squidchat.sound"));

            sound.add(entryBuilder
                    .startBooleanToggle(
                            Component.translatable("option.squidchat.tone_enabled"),
                            config.toneEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> config.toneEnabled = val)
                    .build());

            sound.add(entryBuilder
                    .startIntSlider(
                            Component.translatable("option.squidchat.tone_volume"),
                            Math.round(config.toneVolume * 100),
                            0, 100)
                    .setDefaultValue(100)
                    .setTextGetter(val -> Component.literal(val + "%"))
                    .setSaveConsumer(val -> config.toneVolume = val / 100f)
                    .build());
            sound.setExpanded(true);
            general.addEntry(sound.build());

            SubCategoryBuilder chatWindow = entryBuilder.startSubCategory(
                    Component.translatable("category.squidchat.chat_window"));

            chatWindow.add(new ActionButtonEntry(
                    Component.translatable("option.squidchat.reset_position"),
                    Component.translatable("action.squidchat.reset"),
                    Component.translatable("tooltip.squidchat.reset_position"),
                    () -> {
                        config.resetPosition();
                        ChatWindowManager manager = ChatWindowManager.getInstance();
                        if (manager != null) {
                            manager.loadFromConfig();
                        }
                        ConfigManager.save();
                    }
            ));
            chatWindow.setExpanded(true);
            general.addEntry(chatWindow.build());

            builder.setSavingRunnable(ConfigManager::save);

            return builder.build();
        };
    }

    private static final class ActionButtonEntry extends AbstractConfigListEntry<Void> {
        private final Button button;
        private final Runnable action;
        private final Component tooltip;

        private ActionButtonEntry(Component fieldName, Component buttonText, Component tooltip, Runnable action) {
            super(fieldName, true);
            this.action = action;
            this.tooltip = tooltip;
            this.button = Button.builder(buttonText, widget -> this.action.run())
                    .bounds(0, 0, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
                    .build();
        }

        @Override
        public Void getValue() {
            return null;
        }

        @Override
        public Optional<Void> getDefaultValue() {
            return Optional.empty();
        }

        @Override
        public boolean isEdited() {
            return false;
        }

        @Override
        public int getItemHeight() {
            return 24;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int index, int y, int x, int entryWidth, int entryHeight,
                                       int mouseX, int mouseY, boolean selected, float delta) {
            super.extractRenderState(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, selected, delta);

            Minecraft client = Minecraft.getInstance();
            int textY = y + (entryHeight - client.font.lineHeight) / 2;
            int buttonX = x + entryWidth - ACTION_BUTTON_WIDTH;
            int buttonY = y + (entryHeight - ACTION_BUTTON_HEIGHT) / 2;

            context.text(client.font, getFieldName(), x, textY, getPreferredTextColor(), false);

            button.setX(buttonX);
            button.setY(buttonY);
            button.active = isEditable();
            button.extractRenderState(context, mouseX, mouseY, delta);

            if (button.isHovered()) {
                context.setTooltipForNextFrame(client.font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(button);
        }
    }
}
