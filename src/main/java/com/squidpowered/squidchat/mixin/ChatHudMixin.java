package com.squidpowered.squidchat.mixin;

import com.squidpowered.squidchat.chat.ChatWindowManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Shadow
    public abstract boolean isChatFocused();

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void squidchat$beforeRender(GuiGraphicsExtractor context, Font font,
                                        int currentTick, int mouseX, int mouseY,
                                        ChatComponent.DisplayMode displayMode, boolean insertionClickMode,
                                        CallbackInfo ci) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null) return;

        if (!manager.isChatVisible()) {
            ci.cancel();
            return;
        }

        manager.clampToScreen();

        if (manager.isCustomPosition()) {
            context.pose().pushMatrix();
            context.pose().translate((float) manager.getRenderOffsetX(), (float) manager.getRenderOffsetY());
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void squidchat$afterRender(GuiGraphicsExtractor context, Font font,
                                       int currentTick, int mouseX, int mouseY,
                                       ChatComponent.DisplayMode displayMode, boolean insertionClickMode,
                                       CallbackInfo ci) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null) return;

        if (manager.isCustomPosition()) {
            context.pose().popMatrix();
        }

        if (manager.isChatVisible() && isChatFocused()) {
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

    private void drawInteractionOverlay(GuiGraphicsExtractor context, ChatWindowManager manager) {
        int handleSize = manager.getHandleDrawSize();
        int handleDiameter = handleSize * 2;
        int dragBarHeight = manager.getDragBarHeight();
        int left = manager.getFrameLeft();
        int top = manager.getFrameTop();
        int right = manager.getFrameRight();
        int bottom = manager.getFrameBottom();
        int barTop = Math.max(0, top - dragBarHeight / 2);
        int barBottom = barTop + dragBarHeight;

        int outlineColor = 0x90FFFFFF;
        int dragBarColor = 0xFFFFFFFF;
        int handleColor = 0xFFFFFFFF;

        context.fill(left, barTop, right, barBottom, dragBarColor);
        context.fill(left, top, right, top + 1, outlineColor);
        context.fill(left, bottom - 1, right, bottom + 1, outlineColor);
        context.fill(left, top, left + 1, bottom, outlineColor);
        context.fill(right - 1, top, right, bottom, outlineColor);

        // Top-left
        drawHollowHandle(context, left, top, left + handleDiameter, top + handleDiameter, handleColor);
        // Top-right
        drawHollowHandle(context, right - handleDiameter, top, right, top + handleDiameter, handleColor);
        // Bottom-left
        drawHollowHandle(context, left, bottom - handleDiameter, left + handleDiameter, bottom, handleColor);
        // Bottom-right
        drawHollowHandle(context, right - handleDiameter, bottom - handleDiameter, right, bottom, handleColor);
    }

    private void drawHollowHandle(GuiGraphicsExtractor context, int left, int top, int right, int bottom, int color) {
        context.fill(left, top, right, top + 1, color);
        context.fill(left, bottom - 1, right, bottom, color);
        context.fill(left, top, left + 1, bottom, color);
        context.fill(right - 1, top, right, bottom, color);
    }
}
