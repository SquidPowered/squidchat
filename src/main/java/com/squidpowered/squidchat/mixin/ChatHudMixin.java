package com.squidpowered.squidchat.mixin;

import com.squidpowered.squidchat.chat.ChatWindowManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void squidchat$beforeRender(DrawContext context, TextRenderer textRenderer,
                                         int currentTick, int mouseX, int mouseY,
                                         boolean focused, boolean chatOpen,
                                         CallbackInfo ci) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null) return;

        if (!manager.isChatVisible()) {
            ci.cancel();
            return;
        }

        manager.clampToScreen();

        if (manager.isCustomPosition()) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(manager.getRenderOffsetX(), manager.getRenderOffsetY());
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void squidchat$afterRender(DrawContext context, TextRenderer textRenderer,
                                        int currentTick, int mouseX, int mouseY,
                                        boolean focused, boolean chatOpen,
                                        CallbackInfo ci) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null) return;

        if (manager.isCustomPosition()) {
            context.getMatrices().popMatrix();
        }

        if (manager.isChatVisible() && focused) {
            drawInteractionOverlay(context, manager);
        }
    }

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private void squidchat$getWidth(CallbackInfoReturnable<Integer> cir) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager != null && manager.isCustomSize()) {
            cir.setReturnValue(manager.getRenderedChatWidth());
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void squidchat$getHeight(CallbackInfoReturnable<Integer> cir) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager != null && manager.isCustomSize()) {
            cir.setReturnValue(manager.getRenderedChatHeight());
        }
    }

    private void drawInteractionOverlay(DrawContext context, ChatWindowManager manager) {
        int handleSize = manager.getHandleSize();
        int dragBarHeight = manager.getDragBarHeight();
        int left = manager.getRenderLeft();
        int top = manager.getRenderTop();
        int right = left + manager.getScreenChatWidth();
        int bottom = top + manager.getScreenChatHeight();
        int barTop = Math.max(0, top - dragBarHeight / 2);
        int barBottom = barTop + dragBarHeight;

        int outlineColor = 0x90FFFFFF;
        int dragBarColor = 0xFFFFFFFF;
        int handleColor = 0xFFFFFFFF;

        context.fill(left, barTop, right, barBottom, dragBarColor);
        context.fill(left, top, right, top + 1, outlineColor);
        context.fill(left, bottom - 2, right, bottom + 1, outlineColor);
        context.fill(left - 1, top, left + 2, bottom, outlineColor);
        context.fill(right - 2, top, right + 1, bottom, outlineColor);

        // Top-left
        drawRoundedHandle(context, left - handleSize, top - handleSize, left + handleSize, top + handleSize, handleColor);
        // Top-right
        drawRoundedHandle(context, right - handleSize, top - handleSize, right + handleSize, top + handleSize, handleColor);
        // Bottom-left
        drawRoundedHandle(context, left - handleSize, bottom - handleSize, left + handleSize, bottom + handleSize, handleColor);
        // Bottom-right
        drawRoundedHandle(context, right - handleSize, bottom - handleSize, right + handleSize, bottom + handleSize, handleColor);
    }

    private void drawRoundedHandle(DrawContext context, int left, int top, int right, int bottom, int color) {
        context.fill(left + 1, top, right - 1, bottom, color);
        context.fill(left, top + 1, right, bottom - 1, color);
    }
}
