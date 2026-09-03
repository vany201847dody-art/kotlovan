package ru.kotlovan.mod.module.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.kotlovan.mod.event.Event;
import ru.kotlovan.mod.event.EventRender3D;
import ru.kotlovan.mod.module.EventTarget;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleInfo;
import ru.kotlovan.mod.setting.*;
import ru.kotlovan.mod.util.ColorUtils;
import ru.kotlovan.mod.util.RenderUtils;

@ModuleInfo(name = "ESP", category = "Render", description = "Видение игроков и мобов")
public class ESP extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Boxes", "Boxes", "Skeletons", "Glow");
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting wallhack = new BooleanSetting("Wallhack", true);
    private final NumberSetting range = new NumberSetting("Range", 64.0, 16.0, 256.0, 8.0);
    private final NumberSetting lineWidth = new NumberSetting("LineWidth", 2.0, 0.5, 5.0, 0.5);
    private final BooleanSetting rainbow = new BooleanSetting("Rainbow", true);
    private final ColorSetting boxColor = new ColorSetting("BoxColor", 0, 255, 0, 200);

    public ESP() {
        addSetting(mode);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(wallhack);
        addSetting(range);
        addSetting(lineWidth);
        addSetting(rainbow);
        addSetting(boxColor);
    }

    @EventTarget(priority = Event.Priority.LOW)
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        float time = event.getTickDelta();
        int color = rainbow.isOn() ? ColorUtils.chroma(0, 3.0f, 1.0f, 1.0f) : boxColor.toInt();
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        Box searchBox = mc.player.getBoundingBox().expand(range.getValue());

        for (Entity entity : mc.world.getEntitiesByClass(Entity.class, searchBox, e -> e instanceof LivingEntity)) {
            if (entity == mc.player) continue;
            if (entity.removed) continue;
            if (!(entity instanceof LivingEntity)) continue;

            LivingEntity living = (LivingEntity) entity;
            if (!players.isOn() && living instanceof PlayerEntity) continue;
            if (!mobs.isOn() && living instanceof Monster) continue;
            if (!animals.isOn() && living instanceof AnimalEntity) continue;

            if (mode.is("Boxes")) {
                Box box = living.getBoundingBox();
                RenderUtils.drawBox(event.getMatrixStack(), box, r, g, b, 0.8f);

                // Рамка сверху/снизу ярче
                RenderUtils.drawBox(event.getMatrixStack(),
                        box.minX, box.maxY - 0.05, box.minZ,
                        box.maxX, box.maxY, box.maxZ,
                        r, g, b, 1.0f);

            } else if (mode.is("Skeletons")) {
                drawSkeleton(event.getMatrixStack(), living, r, g, b);

            } else if (mode.is("Glow")) {
                Box box = living.getBoundingBox();
                float pulse = (float) (Math.sin(time * 4) * 0.15 + 0.85);
                RenderUtils.drawBox(event.getMatrixStack(), box,
                        r * pulse, g * pulse, b * pulse, 0.6f);
            }
        }
    }

    private void drawSkeleton(net.minecraft.client.util.math.MatrixStack matrices, LivingEntity entity, float r, float g, float b) {
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double ex = entity.getX() - cam.x;
        double ey = entity.getY() - cam.y;
        double ez = entity.getZ() - cam.z;
        float hw = (float) (entity.getWidth() * 0.5);

        // Голова
        RenderUtils.drawLine(matrices, (float) ex, (float) (ey + entity.getHeight()), (float) ez,
                (float) ex, (float) (ey + entity.getHeight() - 0.2), (float) ez, r, g, b, 0.8f);

        // Тело
        RenderUtils.drawLine(matrices, (float) ex, (float) (ey + entity.getHeight() - 0.2), (float) ez,
                (float) ex, (float) (ey + entity.getHeight() * 0.5), (float) ez, r, g, b, 0.8f);

        // Руки
        RenderUtils.drawLine(matrices, (float) ex, (float) (ey + entity.getHeight() - 0.15), (float) ez,
                (float) (ex - hw), (float) (ey + entity.getHeight() * 0.45), (float) ez, r, g, b, 0.8f);
        RenderUtils.drawLine(matrices, (float) ex, (float) (ey + entity.getHeight() - 0.15), (float) ez,
                (float) (ex + hw), (float) (ey + entity.getHeight() * 0.45), (float) ez, r, g, b, 0.8f);

        // Ноги
        RenderUtils.drawLine(matrices, (float) ex, (float) (ey + entity.getHeight() * 0.5), (float) ez,
                (float) (ex - hw * 0.5f), (float) ey, (float) ez, r, g, b, 0.8f);
        RenderUtils.drawLine(matrices, (float) ex, (float) (ey + entity.getHeight() * 0.5), (float) ez,
                (float) (ex + hw * 0.5f), (float) ey, (float) ez, r, g, b, 0.8f);
    }
}
