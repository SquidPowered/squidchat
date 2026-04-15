package com.squidpowered.squidchat.mixin;

import com.squidpowered.squidchat.chat.ChatWindowManager;
import com.squidpowered.squidchat.chat.ChatWindowManager.ResizeHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void squidchat$onMouseClicked(MouseButtonEvent click, boolean doubled,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != 0) return;
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null) return;

        double mouseX = click.x();
        double mouseY = click.y();
        int[] bounds = squidchat$getChatFrameBounds(manager);
        int left = bounds[0], top = bounds[1], right = bounds[2], bottom = bounds[3];

        ResizeHandle handle = manager.getHandleAt(mouseX, mouseY, left, top, right, bottom);
        if (handle != ResizeHandle.NONE) {
            manager.startResize(handle, left, top, right, bottom);
            ((ContainerEventHandler) (Object) this).setDragging(true);
            cir.setReturnValue(true);
            return;
        }

        if (manager.isInsideDragBar(mouseX, mouseY, left, top, right)) {
            manager.startDrag(mouseX, mouseY, left, top);
            ((ContainerEventHandler) (Object) this).setDragging(true);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void squidchat$onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY,
                                           CallbackInfoReturnable<Boolean> cir) {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null || !Minecraft.getInstance().hasControlDown()) {
            return;
        }

        int[] bounds = squidchat$getChatFrameBounds(manager);
        if (!manager.isInsideWindow(mouseX, mouseY, bounds[0], bounds[1], bounds[2], bounds[3])) {
            return;
        }

        if (manager.adjustChatScale(scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/ActiveTextCollector$ClickableStyleFinder;<init>(Lnet/minecraft/client/gui/Font;II)V"
            ),
            index = 1
    )
    private int squidchat$adjustClickHandlerMouseX(int mouseX) {
        return mouseX - squidchat$getMouseOffsetX();
    }

    @ModifyArg(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/ActiveTextCollector$ClickableStyleFinder;<init>(Lnet/minecraft/client/gui/Font;II)V"
            ),
            index = 2
    )
    private int squidchat$adjustClickHandlerMouseY(int mouseY) {
        return mouseY - squidchat$getMouseOffsetY();
    }

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"
            ),
            index = 3
    )
    private int squidchat$adjustRenderMouseX(int mouseX) {
        return mouseX - squidchat$getMouseOffsetX();
    }

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"
            ),
            index = 4
    )
    private int squidchat$adjustRenderMouseY(int mouseY) {
        return mouseY - squidchat$getMouseOffsetY();
    }

    @Unique
    private static int[] squidchat$getChatFrameBounds(ChatWindowManager manager) {
        return new int[]{
                manager.getFrameLeft(),
                manager.getFrameTop(),
                manager.getFrameRight(),
                manager.getFrameBottom()
        };
    }

    @Unique
    private static int squidchat$getMouseOffsetX() {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        return manager != null && manager.isCustomPosition() ? manager.getRenderOffsetX() : 0;
    }

    @Unique
    private static int squidchat$getMouseOffsetY() {
        ChatWindowManager manager = ChatWindowManager.getInstance();
        return manager != null && manager.isCustomPosition() ? manager.getRenderOffsetY() : 0;
    }
}
