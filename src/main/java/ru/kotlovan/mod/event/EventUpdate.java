package ru.kotlovan.mod.event;

public class EventUpdate extends Event {
    private final float tickDelta;

    public EventUpdate(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
