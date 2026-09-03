package ru.kotlovan.mod;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import ru.kotlovan.mod.event.EventBus;
import ru.kotlovan.mod.event.EventRender2D;
import ru.kotlovan.mod.event.EventRender3D;
import ru.kotlovan.mod.event.EventUpdate;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleManager;

public class Kotlovan {
    private static Kotlovan INSTANCE;
    private boolean initialized;

    public static Kotlovan getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Kotlovan();
        }
        return INSTANCE;
    }

    public void init() {
        if (initialized) return;

        ModuleManager.getInstance().init();

        // Регистрируем все модули в EventBus
        for (Module module : ModuleManager.getInstance().getModules()) {
            EventBus.getInstance().register(module);
        }

        // Тик-ивенты
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        initialized = true;
    }

    private void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        EventBus.getInstance().post(new EventUpdate(mc.getTickDelta()));
    }

    public void onRender3D(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta, long nanoTime, net.minecraft.util.math.Matrix4f projectionMatrix) {
        if (!initialized) return;
        EventBus.getInstance().post(new EventRender3D(matrices, tickDelta, nanoTime, projectionMatrix));
    }

    public void onRender2D(float tickDelta, net.minecraft.client.gui.screen.Screen screen) {
        if (!initialized) return;
        EventBus.getInstance().post(new EventRender2D(tickDelta, screen));
    }
}
