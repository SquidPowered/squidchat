package com.squidpowered.squidchat.mixin;

import com.squidpowered.squidchat.chat.ChatWindowManager;
import com.squidpowered.squidchat.chat.ChatWindowManager.ResizeHandle;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void squidchat$onMouseClicked(Click click, boolean bl,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != 0) return;
        ChatWindowManager manager = ChatWindowManager.getInstance();
        if (manager == null) return;

        double mouseX = click.x();
        double mouseY = click.y();
        int[] bounds = squidchat$getChatBounds(manager);
        int left = bounds[0], top = bounds[1], right = bounds[2], bottom = bounds[3];

        ResizeHandle handle = manager.getHandleAt(mouseX, mouseY, left, top, right, bottom);
        if (handle != ResizeHandle.NONE) {
            manager.startResize(handle, left, top, right, bottom);
            ((ParentElement) (Object) this).setDragging(true);
            cir.setReturnValue(true);
            return;
        }

        if (manager.isInsideDragBar(mouseX, mouseY, left, top, right)) {
            manager.startDrag(mouseX, mouseY, left, top);
            ((ParentElement) (Object) this).setDragging(true);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int[] squidchat$getChatBounds(ChatWindowManager manager) {
        int left = manager.getRenderLeft();
        int top = manager.getRenderTop();
        int width = manager.getScreenChatWidth();
        int height = manager.getScreenChatHeight();

        return new int[]{left, top, left + width, top + height};
    }
}
