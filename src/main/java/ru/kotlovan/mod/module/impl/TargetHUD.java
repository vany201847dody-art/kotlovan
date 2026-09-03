package ru.kotlovan.mod.module.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import ru.kotlovan.mod.event.Event;
import ru.kotlovan.mod.event.EventRender2D;
import ru.kotlovan.mod.module.EventTarget;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleInfo;
import ru.kotlovan.mod.setting.BooleanSetting;
import ru.kotlovan.mod.setting.NumberSetting;
import ru.kotlovan.mod.util.MathUtils;
import ru.kotlovan.mod.util.RenderUtils;

@ModuleInfo(name = "TargetHUD", category = "Combat", description = "Интерфейс текущей цели KillAura")
public class TargetHUD extends Module {
    private final NumberSetting posX = new NumberSetting("X", 20.0, 0.0, 500.0, 5.0);
    private final NumberSetting posY = new NumberSetting("Y", 120.0, 0.0, 500.0, 5.0);
    private final NumberSetting width = new NumberSetting("Width", 160.0, 80.0, 300.0, 10.0);
    private final BooleanSetting showArmor = new BooleanSetting("ShowArmor", true);
    private final BooleanSetting showEffects = new BooleanSetting("ShowEffects", true);

    private float smoothHealth;
    private float displayHealth;
    private long lastDamageTime;

    public TargetHUD() {
        addSetting(posX);
        addSetting(posY);
        addSetting(width);
        addSetting(showArmor);
        addSetting(showEffects);
        smoothHealth = 0;
        displayHealth = 0;
    }

    @EventTarget(priority = Event.Priority.LOW)
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        KillAura killAura = getKillAura();
        if (killAura == null || !killAura.isEnabled()) return;

        LivingEntity target = killAura.getTarget();
        if (target == null || target.removed) return;

        // Плавное здоровье (Smooth Health Bar)
        float targetHealth = target.getHealth();
        if (targetHealth < displayHealth) {
            // Урон — плавное «сгорание» полоски
            displayHealth = MathUtils.lerp(displayHealth, targetHealth, 0.15f);
            lastDamageTime = System.currentTimeMillis();
        } else {
            displayHealth = targetHealth;
        }
        smoothHealth = MathUtils.lerp(smoothHealth, displayHealth, 0.2f);

        float maxHealth = target.getMaxHealth();
        float healthRatio = MathUtils.clamp(smoothHealth / maxHealth, 0.0f, 1.0f);

        MatrixStack matrices = new MatrixStack();
        float x = posX.floatValue();
        float y = posY.floatValue();
        float w = width.floatValue();
        float h = 45.0f;

        // Фон панели (полупрозрачный тёмный)
        RenderUtils.drawRect(x - 2, y - 2, w + 4, h + 4, 0xCC101018);

        // Рамка (неоновая)
        RenderUtils.drawRect(x - 2, y - 2, w + 4, 1, 0xFF00E5FF); // верх
        RenderUtils.drawRect(x - 2, y + h + 1, w + 4, 1, 0xFF00E5FF); // низ
        RenderUtils.drawRect(x - 2, y - 2, 1, h + 4, 0xFF00E5FF); // лево
        RenderUtils.drawRect(x + w + 1, y - 2, 1, h + 4, 0xFF00E5FF); // право

        // Никнейм
        String name = target.getName().getString();
        mc.textRenderer.drawWithShadow(matrices, "§f" + name, x + 4, y + 4, 0xFFFFFF);

        // Здоровье (текст)
        String healthText = String.format("§c%.1f§7 / §c%.1f", smoothHealth, maxHealth);
        mc.textRenderer.drawWithShadow(matrices, healthText, x + 4, y + 14, 0xFFFFFF);

        // Полоска здоровья (интерполированная)
        float barX = x + 4;
        float barY = y + 25;
        float barW = w - 8;
        float barH = 6.0f;

        // Фон полоски
        RenderUtils.drawRect(barX, barY, barW, barH, 0xFF333333);

        // Полоска текущего здоровья (плавная)
        int healthColor = getHealthColor(healthRatio);
        RenderUtils.drawRect(barX, barY, barW * healthRatio, barH, healthColor);

        // «Сгоревшая» полоска (от предыдущего значения, красная)
        if (displayHealth < smoothHealth + 0.5f) {
            float burnRatio = MathUtils.clamp(displayHealth / maxHealth, 0.0f, 1.0f);
            RenderUtils.drawRect(barX + barW * burnRatio, barY,
                    barW * (healthRatio - burnRatio), barH, 0xAAFF0000);
        }

        // Броня
        if (showArmor.isOn() && target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            float armorX = x + 4;
            float armorY = y + 34;
            for (int i = 0; i < 4; i++) {
                net.minecraft.item.ItemStack armor = player.inventory.armor.get(i);
                if (!armor.isEmpty()) {
                    mc.textRenderer.drawWithShadow(matrices, "§7" + armor.getCount(), armorX + i * 20, armorY, 0xFFFFFF);
                }
            }
        }
    }

    private int getHealthColor(float ratio) {
        if (ratio > 0.5f) {
            int g = (int) (255 * ratio);
            return 0xFF000000 | (100 << 16) | (g << 8);
        } else {
            int r = (int) (255 * (1 - ratio));
            return 0xFF000000 | (r << 16) | (100 << 8);
        }
    }

    private KillAura getKillAura() {
        try {
            return (KillAura) ru.kotlovan.mod.module.ModuleManager.getInstance().getByName("KillAura");
        } catch (Exception e) {
            return null;
        }
    }
}
