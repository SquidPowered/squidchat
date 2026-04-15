package com.squidpowered.squidchat.mixin;

import com.squidpowered.squidchat.config.SquidChatOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.ChatOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatOptionsScreen.class)
public abstract class ChatOptionsScreenMixin extends OptionsSubScreen {
    protected ChatOptionsScreenMixin(Screen lastScreen, net.minecraft.client.Options options, net.minecraft.network.chat.Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("HEAD"), cancellable = true)
    private void squidchat$replaceVanillaChatOptions(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof SquidChatOptionsScreen)) {
            minecraft.setScreen(new SquidChatOptionsScreen(lastScreen, options));
        }
        ci.cancel();
    }
}
