package ru.kotlovan.mod.module.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.kotlovan.mod.event.Event;
import ru.kotlovan.mod.event.EventRender3D;
import ru.kotlovan.mod.event.EventUpdate;
import ru.kotlovan.mod.module.EventTarget;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleInfo;
import ru.kotlovan.mod.setting.*;
import ru.kotlovan.mod.util.MathUtils;
import ru.kotlovan.mod.util.RenderUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "KillAura", category = "Combat", description = "Автоатака с плавным наведением")
public class KillAura extends Module {
    // Настройки
    private final ModeSetting targetMode = new ModeSetting("Target", "Single", "Single", "Switch");
    private final ModeSetting sorting = new ModeSetting("Sorting", "Distance", "Distance", "Health");
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private final BooleanSetting invisibles = new BooleanSetting("Invisibles", false);
    private final NumberSetting range = new NumberSetting("Range", 4.0, 2.0, 6.0, 0.1);
    private final BooleanSetting blockRaytrace = new BooleanSetting("BlockRaytrace", false);
    private final BooleanSetting shieldBreaker = new BooleanSetting("ShieldBreaker", true);
    private final NumberSetting minCPS = new NumberSetting("MinCPS", 10.0, 1.0, 20.0, 1.0);
    private final NumberSetting maxCPS = new NumberSetting("MaxCPS", 14.0, 1.0, 20.0, 1.0);
    private final NumberSetting rotationSpeed = new NumberSetting("RotationSpeed", 60.0, 10.0, 360.0, 5.0);
    private final BooleanSetting targetESP = new BooleanSetting("TargetESP", true);
    private final ColorSetting espColor = new ColorSetting("ESPColor", 255, 50, 50, 200);

    private LivingEntity target;
    private float targetYaw, targetPitch;
    private float currentYaw, currentPitch;
    private long lastAttackTime;
    private int switchIndex;
    private float espRotation;

    public KillAura() {
        addSetting(targetMode);
        addSetting(sorting);
        addSetting(players);
        addSetting(mobs);
        addSetting(invisibles);
        addSetting(range);
        addSetting(blockRaytrace);
        addSetting(shieldBreaker);
        addSetting(minCPS);
        addSetting(maxCPS);
        addSetting(rotationSpeed);
        addSetting(targetESP);
        addSetting(espColor);
    }

    @Override
    protected void onEnable() {
        target = null;
        currentYaw = 0;
        currentPitch = 0;
    }

    @Override
    protected void onDisable() {
        target = null;
    }

    @EventTarget(priority = Event.Priority.HIGH)
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        target = findTarget();
        if (target == null) return;

        // Рассчитываем целевые углы (Silent Rotation — пакеты отдельно от визуала)
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(distXZ, 0.0001)));
        targetPitch = MathUtils.clamp(targetPitch, -90.0f, 90.0f);

        // Плавная интерполяция углов (Smooth Rotation)
        float speed = rotationSpeed.floatValue();
        float deltaYaw = MathUtils.wrapDegrees(targetYaw - currentYaw);
        float deltaPitch = targetPitch - currentPitch;
        currentYaw += MathUtils.clamp(deltaYaw, -speed, speed);
        currentPitch += MathUtils.clamp(deltaPitch, -speed * 0.7f, speed * 0.7f);

        // CPS логика с рандомизацией (обход античитов)
        long now = System.currentTimeMillis();
        double cps = MathUtils.random(minCPS.getValue(), maxCPS.getValue());
        long interval = (long) (1000.0 / cps);

        // Ждём пока целевой углов пролетит足够的 близость
        float angleToTarget = (float) Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
        if (angleToTarget < 15.0f && now - lastAttackTime >= interval) {
            // AutoTool: лучший инструмент
            int bestSlot = findBestWeapon();
            if (bestSlot >= 0 && bestSlot != mc.player.inventory.selectedSlot) {
                mc.player.inventory.selectedSlot = bestSlot;
            }

            // ShieldBreaker: топор если цель блокирует щитом
            if (shieldBreaker.isOn() && target.isBlocking()) {
                int axeSlot = findAxe();
                if (axeSlot >= 0 && axeSlot != mc.player.inventory.selectedSlot) {
                    mc.player.inventory.selectedSlot = axeSlot;
                }
            }

            // Атака через пакеты (Silent — визуально не поворачиваем игрока)
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttackTime = now;
        }

        // Switch: смена цели каждые 20 тиков
        if (targetMode.is("Switch")) {
            switchIndex++;
        }
    }

    @EventTarget(priority = Event.Priority.LOW)
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        if (!targetESP.isOn()) return;
        if (target == null || target.removed) return;

        espRotation += event.getTickDelta() * 2.0f;

        // Анимированное 3D-кольцо вокруг цели
        Vec3d pos = target.getPos();
        float r = espColor.getRf();
        float g = espColor.getGf();
        float b = espColor.getBf();

        // Спираль/кольцо с затуханием
        float radius = (float) (target.getWidth() * 0.75);
        float yOffset = (float) (Math.sin(espRotation) * 0.3);

        RenderUtils.drawCircle(event.getMatrixStack(),
                pos.x, pos.y + target.getHeight() * 0.5 + yOffset, pos.z,
                radius, r, g, b, 0.8f);

        // Второе кольцо (более тонкое, выше)
        RenderUtils.drawCircle(event.getMatrixStack(),
                pos.x, pos.y + target.getHeight() * 0.5 + yOffset + 0.1, pos.z,
                radius * 0.8f, r, g, b, 0.4f);
    }

    private LivingEntity findTarget() {
        if (mc.player == null || mc.world == null) return null;

        Box searchBox = mc.player.getBoundingBox().expand(range.getValue());
        List<LivingEntity> candidates = new ArrayList<>();

        for (Entity entity : mc.world.getEntitiesByClass(Entity.class, searchBox, e -> e instanceof LivingEntity)) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living == mc.player) continue;
            if (living.removed) continue;
            if (!players.isOn() && living instanceof PlayerEntity) continue;
            if (!mobs.isOn() && living instanceof Monster) continue;
            if (!invisibles.isOn() && living.isInvisible()) continue;
            if (living.getHealth() <= 0) continue;

            double dist = mc.player.squaredDistanceTo(living);
            if (dist > range.getValue() * range.getValue()) continue;

            candidates.add(living);
        }

        if (candidates.isEmpty()) return null;

        // Сортировка
        if (sorting.is("Health")) {
            candidates.sort(Comparator.comparingDouble(LivingEntity::getHealth));
        } else {
            candidates.sort(Comparator.comparingDouble(mc.player::squaredDistanceTo));
        }

        // Switch логика
        if (targetMode.is("Switch") && candidates.size() > 1) {
            return candidates.get(switchIndex % candidates.size());
        }

        return candidates.get(0);
    }

    private int findBestWeapon() {
        if (mc.player == null) return -1;
        int best = -1;
        float bestDmg = -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.ItemStack stack = mc.player.inventory.getStack(i);
            if (stack.isEmpty()) continue;
            float dmg = getAttackDamage(stack);
            if (dmg > bestDmg) {
                bestDmg = dmg;
                best = i;
            }
        }
        return best;
    }

    private int findAxe() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.ItemStack stack = mc.player.inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof net.minecraft.item.AxeItem) return i;
        }
        return -1;
    }

    private float getAttackDamage(net.minecraft.item.ItemStack stack) {
        net.minecraft.item.Item item = stack.getItem();
        if (item instanceof net.minecraft.item.SwordItem) {
            return ((net.minecraft.item.SwordItem) item).getAttackDamage();
        }
        if (item instanceof net.minecraft.item.ToolItem) {
            return ((net.minecraft.item.ToolItem) item).getMaterial().getAttackDamage();
        }
        return 1.0f;
    }

    // Геттеры для TargetHUD
    public LivingEntity getTarget() {
        return target;
    }

    public float getCurrentYaw() {
        return currentYaw;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }
}
