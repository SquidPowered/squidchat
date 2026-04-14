package com.squidpowered.squidchat.chat;

import com.squidpowered.squidchat.config.ConfigManager;
import com.squidpowered.squidchat.config.SquidChatConfig;
import net.minecraft.client.Minecraft;

public class ChatWindowManager {
    private static final int DEFAULT_CHAT_LEFT = 4;
    private static final int DEFAULT_CHAT_BOTTOM_OFFSET = 40;

    private static ChatWindowManager instance;

    private int chatX;
    private int chatY;
    private int chatWidth;
    private int chatHeight;
    private boolean chatVisible;

    private boolean dragging;
    private boolean resizing;
    private int dragOffsetX;
    private int dragOffsetY;
    private ResizeHandle activeHandle;
    private int origLeft, origTop, origRight, origBottom;

    public enum ResizeHandle {
        NONE,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private static final int HANDLE_HITBOX_SIZE = 4;
    private static final int HANDLE_DRAW_SIZE = 2;
    private static final int DRAG_BAR_HEIGHT = 10;
    private static final int MIN_WIDTH = 60;
    private static final int MIN_HEIGHT = 40;
    private static final int FRAME_LEFT_PADDING = 4;
    private static final int FRAME_RIGHT_PADDING = 8;
    private static final int FRAME_BOTTOM_PADDING = 0;

    public static void init() {
        instance = new ChatWindowManager();
        instance.loadFromConfig();
    }

    public static ChatWindowManager getInstance() {
        return instance;
    }

    public void loadFromConfig() {
        SquidChatConfig config = ConfigManager.getConfig();
        this.chatX = config.chatX;
        this.chatY = config.chatY;
        this.chatWidth = config.chatWidth;
        this.chatHeight = config.chatHeight;
        this.chatVisible = config.chatVisible;
    }

    public boolean isCustomPosition() {
        return chatX != 0 || chatY != 0;
    }

    public boolean isCustomSize() {
        return chatWidth > 0 && chatHeight > 0;
    }

    public int getChatX() {
        return chatX;
    }

    public int getChatY() {
        return chatY;
    }

    public int getChatWidth() {
        return chatWidth;
    }

    public int getChatHeight() {
        return chatHeight;
    }

    public boolean isChatVisible() {
        return chatVisible;
    }

    public void setChatVisible(boolean visible) {
        this.chatVisible = visible;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isResizing() {
        return resizing;
    }

    // Returns which resize handle is at the given screen position,
    // given the chat window's bounding box on screen.
    public ResizeHandle getHandleAt(double mouseX, double mouseY,
                                     int windowLeft, int windowTop,
                                     int windowRight, int windowBottom) {
        // Top-left
        if (mouseX >= windowLeft - HANDLE_HITBOX_SIZE && mouseX <= windowLeft + HANDLE_HITBOX_SIZE
                && mouseY >= windowTop - HANDLE_HITBOX_SIZE && mouseY <= windowTop + HANDLE_HITBOX_SIZE) {
            return ResizeHandle.TOP_LEFT;
        }
        // Top-right
        if (mouseX >= windowRight - HANDLE_HITBOX_SIZE && mouseX <= windowRight + HANDLE_HITBOX_SIZE
                && mouseY >= windowTop - HANDLE_HITBOX_SIZE && mouseY <= windowTop + HANDLE_HITBOX_SIZE) {
            return ResizeHandle.TOP_RIGHT;
        }
        // Bottom-left
        if (mouseX >= windowLeft - HANDLE_HITBOX_SIZE && mouseX <= windowLeft + HANDLE_HITBOX_SIZE
                && mouseY >= windowBottom - HANDLE_HITBOX_SIZE && mouseY <= windowBottom + HANDLE_HITBOX_SIZE) {
            return ResizeHandle.BOTTOM_LEFT;
        }
        // Bottom-right
        if (mouseX >= windowRight - HANDLE_HITBOX_SIZE && mouseX <= windowRight + HANDLE_HITBOX_SIZE
                && mouseY >= windowBottom - HANDLE_HITBOX_SIZE && mouseY <= windowBottom + HANDLE_HITBOX_SIZE) {
            return ResizeHandle.BOTTOM_RIGHT;
        }
        return ResizeHandle.NONE;
    }

    public boolean isInsideWindow(double mouseX, double mouseY,
                                   int windowLeft, int windowTop,
                                   int windowRight, int windowBottom) {
        return mouseX >= windowLeft && mouseX <= windowRight
                && mouseY >= windowTop && mouseY <= windowBottom;
    }

        public boolean isInsideDragBar(double mouseX, double mouseY,
                       int windowLeft, int windowTop,
                       int windowRight) {
            int barTop = Math.max(0, windowTop - DRAG_BAR_HEIGHT / 2);
            int barBottom = barTop + DRAG_BAR_HEIGHT;
        return mouseX >= windowLeft && mouseX <= windowRight
                && mouseY >= barTop && mouseY <= barBottom;
        }

    public void startDrag(double mouseX, double mouseY, int windowLeft, int windowTop) {
        dragging = true;
        dragOffsetX = (int) mouseX - windowLeft;
        dragOffsetY = (int) mouseY - windowTop;
    }

    public void startResize(ResizeHandle handle, int origLeft, int origTop,
                            int origRight, int origBottom) {
        resizing = true;
        activeHandle = handle;
        this.origLeft = origLeft;
        this.origTop = origTop;
        this.origRight = origRight;
        this.origBottom = origBottom;
    }

    public void updateDrag(double mouseX, double mouseY) {
        if (!dragging) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int newFrameLeft = (int) mouseX - dragOffsetX;
        int newFrameTop = (int) mouseY - dragOffsetY;

        int frameWidth = getScreenFrameWidth();
        int frameHeight = getScreenFrameHeight();
        newFrameLeft = Math.max(0, Math.min(newFrameLeft, screenWidth - frameWidth));
        newFrameTop = Math.max(0, Math.min(newFrameTop, screenHeight - frameHeight));

        chatX = newFrameLeft + FRAME_LEFT_PADDING;
        chatY = newFrameTop;
    }

    public void updateResize(double mouseX, double mouseY) {
        if (!resizing || activeHandle == ResizeHandle.NONE) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int minFrameWidth = MIN_WIDTH + FRAME_LEFT_PADDING + getScreenRightPadding();
        int minFrameHeight = getMinimumScreenChatHeight() + FRAME_BOTTOM_PADDING;

        int newLeft = origLeft;
        int newTop = origTop;
        int newRight = origRight;
        int newBottom = origBottom;

        switch (activeHandle) {
            case NONE -> {
                return;
            }
            case TOP_LEFT -> {
                newLeft = clamp((int) mouseX, 0, origRight - minFrameWidth);
                newTop = clamp((int) mouseY, 0, origBottom - minFrameHeight);
            }
            case TOP_RIGHT -> {
                newRight = clamp((int) mouseX, origLeft + minFrameWidth, screenWidth);
                newTop = clamp((int) mouseY, 0, origBottom - minFrameHeight);
            }
            case BOTTOM_LEFT -> {
                newLeft = clamp((int) mouseX, 0, origRight - minFrameWidth);
                newBottom = clamp((int) mouseY, origTop + minFrameHeight, screenHeight);
            }
            case BOTTOM_RIGHT -> {
                newRight = clamp((int) mouseX, origLeft + minFrameWidth, screenWidth);
                newBottom = clamp((int) mouseY, origTop + minFrameHeight, screenHeight);
            }
        }

        chatX = newLeft + FRAME_LEFT_PADDING;
        chatY = newTop;
        chatWidth = newRight - newLeft - FRAME_LEFT_PADDING - getScreenRightPadding();
        chatHeight = screenToRenderedHeight(newBottom - newTop - FRAME_BOTTOM_PADDING);
        refreshChatHud();
    }

    public void endInteraction() {
        dragging = false;
        resizing = false;
        activeHandle = ResizeHandle.NONE;
        saveToConfig();
    }

    public void saveToConfig() {
        SquidChatConfig config = ConfigManager.getConfig();
        config.chatX = chatX;
        config.chatY = chatY;
        config.chatWidth = chatWidth;
        config.chatHeight = chatHeight;
        config.chatVisible = chatVisible;
        ConfigManager.save();
    }

    public void clampToScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() == null) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (isCustomSize()) {
            int maxChatWidth = Math.max(MIN_WIDTH, screenWidth - FRAME_LEFT_PADDING - getScreenRightPadding());
            int maxChatHeight = Math.max(MIN_HEIGHT, getMaximumRenderedHeight(screenHeight));

            chatWidth = clamp(chatWidth, MIN_WIDTH, maxChatWidth);
            chatHeight = clamp(chatHeight, MIN_HEIGHT, maxChatHeight);
        }

        if (isCustomPosition()) {
            int maxChatX = Math.max(FRAME_LEFT_PADDING, screenWidth - getScreenChatWidth() - FRAME_RIGHT_PADDING);
            int maxChatY = Math.max(0, screenHeight - getScreenChatHeight() - FRAME_BOTTOM_PADDING);

            chatX = clamp(chatX, FRAME_LEFT_PADDING, maxChatX);
            chatY = clamp(chatY, 0, maxChatY);
        }
    }

    public static int getVanillaChatWidth() {
        Minecraft client = Minecraft.getInstance();
        return (int) (client.options.chatWidth().get() * 280 + 40);
    }

    public static int getVanillaChatHeight() {
        Minecraft client = Minecraft.getInstance();
        return (int) (client.options.chatHeightFocused().get() * 160 + 20);
    }

    public int getHandleDrawSize() {
        return HANDLE_DRAW_SIZE;
    }

    public int getDragBarHeight() {
        return DRAG_BAR_HEIGHT;
    }

    public int getFrameLeft() {
        return getRenderLeft() - FRAME_LEFT_PADDING;
    }

    public int getFrameTop() {
        return getRenderTop();
    }

    public int getFrameRight() {
        return getRenderLeft() + getScreenChatWidth() + getScreenRightPadding();
    }

    public int getFrameBottom() {
        return getRenderTop() + getScreenChatHeight() + FRAME_BOTTOM_PADDING;
    }

    public int getRenderedChatWidth() {
        return isCustomSize() ? chatWidth : getVanillaChatWidth();
    }

    public int getRenderedChatHeight() {
        return isCustomSize() ? chatHeight : getVanillaChatHeight();
    }

    public double getChatScale() {
        Minecraft client = Minecraft.getInstance();
        return client.options.chatScale().get();
    }

    public int getScreenChatWidth() {
        return getRenderedChatWidth();
    }

    public int getScreenChatHeight() {
        return Math.max(1, (int) Math.round(getRenderedChatHeight() * getChatScale()));
    }

    public int getScreenFrameWidth() {
        return getScreenChatWidth() + FRAME_LEFT_PADDING + getScreenRightPadding();
    }

    public int getScreenFrameHeight() {
        return getScreenChatHeight() + FRAME_BOTTOM_PADDING;
    }

    private int getMinimumScreenChatHeight() {
        return Math.max(1, (int) Math.round(MIN_HEIGHT * getChatScale()));
    }

    private int getScreenRightPadding() {
        // Vanilla chat backgrounds keep an extra 8 local units on the right,
        // so the visible margin scales with chat scale.
        return Math.max(1, (int) Math.round(FRAME_RIGHT_PADDING * getChatScale()));
    }

    private int screenToRenderedHeight(int screenHeight) {
        return Math.max(MIN_HEIGHT, (int) Math.round(screenHeight / getChatScale()));
    }

    private int getMaximumRenderedHeight(int screenHeight) {
        return (int) Math.floor((screenHeight - FRAME_BOTTOM_PADDING) / getChatScale());
    }

    public int getRenderLeft() {
        return isCustomPosition() ? chatX : DEFAULT_CHAT_LEFT;
    }

    public int getRenderTop() {
        Minecraft client = Minecraft.getInstance();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        return isCustomPosition() ? chatY : screenHeight - DEFAULT_CHAT_BOTTOM_OFFSET - getScreenChatHeight();
    }

    public int getRenderOffsetX() {
        return getRenderLeft() - DEFAULT_CHAT_LEFT;
    }

    public int getRenderOffsetY() {
        Minecraft client = Minecraft.getInstance();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int defaultTop = screenHeight - DEFAULT_CHAT_BOTTOM_OFFSET - getScreenChatHeight();
        return getRenderTop() - defaultTop;
    }

    public int getRenderWindowHeight() {
        return getRenderTop() + getScreenFrameHeight() + DEFAULT_CHAT_BOTTOM_OFFSET;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private void refreshChatHud() {
        Minecraft client = Minecraft.getInstance();
        if (client.gui != null) {
            client.gui.getChat().rescaleChat();
        }
    }
}
