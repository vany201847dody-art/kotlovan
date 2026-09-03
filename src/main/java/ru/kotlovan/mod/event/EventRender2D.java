package ru.kotlovan.mod.event;

import net.minecraft.client.gui.screen.Screen;

public class EventRender2D extends Event {
    private final float tickDelta;
    private final Screen currentScreen;

    public EventRender2D(float tickDelta, Screen currentScreen) {
        this.tickDelta = tickDelta;
        this.currentScreen = currentScreen;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }
}
