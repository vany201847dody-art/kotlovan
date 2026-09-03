package ru.kotlovan.mod.event;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class EventRender3D extends Event {
    private final MatrixStack matrixStack;
    private final float tickDelta;
    private final long nanoTime;
    private final Matrix4f projectionMatrix;

    public EventRender3D(MatrixStack matrixStack, float tickDelta, long nanoTime, Matrix4f projectionMatrix) {
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
        this.nanoTime = nanoTime;
        this.projectionMatrix = projectionMatrix;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}
