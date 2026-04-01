package com.squidpowered.squidchat.config;

import com.squidpowered.squidchat.chat.ChatWindowManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

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
                    .setTitle(Text.translatable("title.squidchat.config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                        ConfigCategory general = builder.getOrCreateCategory(
                                        Text.translatable("category.squidchat"));

                        SubCategoryBuilder sound = entryBuilder.startSubCategory(
                                        Text.translatable("category.squidchat.sound"));

                        sound.add(entryBuilder
                    .startBooleanToggle(
                            Text.translatable("option.squidchat.tone_enabled"),
                            config.toneEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> config.toneEnabled = val)
                    .build());

                        sound.add(entryBuilder
                    .startIntSlider(
                            Text.translatable("option.squidchat.tone_volume"),
                            Math.round(config.toneVolume * 100),
                            0, 100)
                    .setDefaultValue(100)
                    .setTextGetter(val -> Text.literal(val + "%"))
                    .setSaveConsumer(val -> config.toneVolume = val / 100f)
                    .build());
                        sound.setExpanded(true);
                        general.addEntry(sound.build());

                        SubCategoryBuilder chatWindow = entryBuilder.startSubCategory(
                                        Text.translatable("category.squidchat.chat_window"));

                        chatWindow.add(new ActionButtonEntry(
                                        Text.translatable("option.squidchat.reset_position"),
                                        Text.translatable("action.squidchat.reset"),
                                        Text.translatable("tooltip.squidchat.reset_position"),
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
                private final ButtonWidget button;
                private final Runnable action;
                private final Text tooltip;

                private ActionButtonEntry(Text fieldName, Text buttonText, Text tooltip, Runnable action) {
                        super(fieldName, true);
                        this.action = action;
                        this.tooltip = tooltip;
                        this.button = ButtonWidget.builder(buttonText, widget -> this.action.run())
                                        .dimensions(0, 0, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
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
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                                                   int mouseX, int mouseY, boolean selected, float delta) {
                        super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, selected, delta);

                        MinecraftClient client = MinecraftClient.getInstance();
                        int textY = y + (entryHeight - client.textRenderer.fontHeight) / 2;
                        int buttonX = x + entryWidth - ACTION_BUTTON_WIDTH;
                        int buttonY = y + (entryHeight - ACTION_BUTTON_HEIGHT) / 2;

                        context.drawText(client.textRenderer, getFieldName(), x, textY, getPreferredTextColor(), false);

                        button.setPosition(buttonX, buttonY);
                        button.active = isEditable();
                        button.render(context, mouseX, mouseY, delta);

                        if (button.isHovered()) {
                                context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
                        }
                }

                @Override
                public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
                        return button.mouseClicked(click, doubled);
                }

                @Override
                public List<? extends Element> children() {
                        return List.of(button);
                }

                @Override
                public List<? extends Selectable> narratables() {
                        return List.of(button);
                }
        }
}
