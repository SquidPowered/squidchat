package com.squidpowered.squidchat.chat;

import com.squidpowered.squidchat.config.ConfigManager;
import com.squidpowered.squidchat.config.SquidChatConfig;
import net.minecraft.client.MinecraftClient;

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

    private static final int HANDLE_SIZE = 6;
    private static final int DRAG_BAR_HEIGHT = 10;
    private static final int MIN_WIDTH = 60;
    private static final int MIN_HEIGHT = 40;

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
        if (mouseX >= windowLeft - HANDLE_SIZE && mouseX <= windowLeft + HANDLE_SIZE
                && mouseY >= windowTop - HANDLE_SIZE && mouseY <= windowTop + HANDLE_SIZE) {
            return ResizeHandle.TOP_LEFT;
        }
        // Top-right
        if (mouseX >= windowRight - HANDLE_SIZE && mouseX <= windowRight + HANDLE_SIZE
                && mouseY >= windowTop - HANDLE_SIZE && mouseY <= windowTop + HANDLE_SIZE) {
            return ResizeHandle.TOP_RIGHT;
        }
        // Bottom-left
        if (mouseX >= windowLeft - HANDLE_SIZE && mouseX <= windowLeft + HANDLE_SIZE
                && mouseY >= windowBottom - HANDLE_SIZE && mouseY <= windowBottom + HANDLE_SIZE) {
            return ResizeHandle.BOTTOM_LEFT;
        }
        // Bottom-right
        if (mouseX >= windowRight - HANDLE_SIZE && mouseX <= windowRight + HANDLE_SIZE
                && mouseY >= windowBottom - HANDLE_SIZE && mouseY <= windowBottom + HANDLE_SIZE) {
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

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int newX = (int) mouseX - dragOffsetX;
        int newY = (int) mouseY - dragOffsetY;

        int w = getScreenChatWidth();
        int h = getScreenChatHeight();
        newX = Math.max(0, Math.min(newX, screenWidth - w));
        newY = Math.max(0, Math.min(newY, screenHeight - h));

        chatX = newX;
        chatY = newY;
    }

    public void updateResize(double mouseX, double mouseY) {
        if (!resizing || activeHandle == ResizeHandle.NONE) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int newLeft = origLeft;
        int newTop = origTop;
        int newRight = origRight;
        int newBottom = origBottom;

        switch (activeHandle) {
            case NONE -> {
                return;
            }
            case TOP_LEFT -> {
                newLeft = clamp((int) mouseX, 0, origRight - MIN_WIDTH);
                newTop = clamp((int) mouseY, 0, origBottom - MIN_HEIGHT);
            }
            case TOP_RIGHT -> {
                newRight = clamp((int) mouseX, origLeft + MIN_WIDTH, screenWidth);
                newTop = clamp((int) mouseY, 0, origBottom - MIN_HEIGHT);
            }
            case BOTTOM_LEFT -> {
                newLeft = clamp((int) mouseX, 0, origRight - MIN_WIDTH);
                newBottom = clamp((int) mouseY, origTop + MIN_HEIGHT, screenHeight);
            }
            case BOTTOM_RIGHT -> {
                newRight = clamp((int) mouseX, origLeft + MIN_WIDTH, screenWidth);
                newBottom = clamp((int) mouseY, origTop + MIN_HEIGHT, screenHeight);
            }
        }

        chatX = newLeft;
        chatY = newTop;
        chatWidth = newRight - newLeft;
        chatHeight = newBottom - newTop;
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() == null) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int w = getScreenChatWidth();
        int h = getScreenChatHeight();

        if (chatX + w > screenWidth) chatX = Math.max(0, screenWidth - w);
        if (chatY + h > screenHeight) chatY = Math.max(0, screenHeight - h);
        if (chatX < 0) chatX = 0;
        if (chatY < 0) chatY = 0;

        if (isCustomSize()) {
            chatWidth = Math.min(chatWidth, screenWidth);
            chatHeight = Math.min(chatHeight, screenHeight);
        }
    }

    public static int getVanillaChatWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        return (int) (client.options.getChatWidth().getValue() * 280 + 40);
    }

    public static int getVanillaChatHeight() {
        MinecraftClient client = MinecraftClient.getInstance();
        return (int) (client.options.getChatHeightFocused().getValue() * 160 + 20);
    }

    public int getHandleSize() {
        return HANDLE_SIZE;
    }

    public int getDragBarHeight() {
        return DRAG_BAR_HEIGHT;
    }

    public int getRenderedChatWidth() {
        return isCustomSize() ? chatWidth : getVanillaChatWidth();
    }

    public int getRenderedChatHeight() {
        return isCustomSize() ? chatHeight : getVanillaChatHeight();
    }

    public double getChatScale() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.options.getChatScale().getValue();
    }

    public int getScreenChatWidth() {
        return Math.max(1, (int) Math.round(getRenderedChatWidth() * getChatScale()));
    }

    public int getScreenChatHeight() {
        return Math.max(1, (int) Math.round(getRenderedChatHeight() * getChatScale()));
    }

    public int getRenderLeft() {
        return isCustomPosition() ? chatX : DEFAULT_CHAT_LEFT;
    }

    public int getRenderTop() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();
        return isCustomPosition() ? chatY : screenHeight - DEFAULT_CHAT_BOTTOM_OFFSET - getScreenChatHeight();
    }

    public int getRenderOffsetX() {
        return getRenderLeft() - DEFAULT_CHAT_LEFT;
    }

    public int getRenderOffsetY() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();
        int defaultTop = screenHeight - DEFAULT_CHAT_BOTTOM_OFFSET - getScreenChatHeight();
        return getRenderTop() - defaultTop;
    }

    public int getRenderWindowHeight() {
        return getRenderTop() + getScreenChatHeight() + DEFAULT_CHAT_BOTTOM_OFFSET;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private void refreshChatHud() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().reset();
        }
    }
}
