package ru.kotlovan.mod.module.impl;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.kotlovan.mod.event.EventUpdate;
import ru.kotlovan.mod.module.EventTarget;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleInfo;
import ru.kotlovan.mod.setting.ModeSetting;
import ru.kotlovan.mod.setting.NumberSetting;
import ru.kotlovan.mod.setting.BooleanSetting;

@ModuleInfo(name = "Fly", category = "Movement", description = "Полёт")
public class Fly extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Packet", "MatrixBypass");
    private final NumberSetting speed = new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.1);
    private final BooleanSetting antiKick = new BooleanSetting("AntiKick", true);

    private int tickCounter;
    private double startY;

    public Fly() {
        addSetting(mode);
        addSetting(speed);
        addSetting(antiKick);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;
        if (mc.player != null) {
            startY = mc.player.getY();
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.abilities.flying = false;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;

        mc.player.abilities.flying = false;
        mc.player.setVelocity(mc.player.getVelocity().multiply(0, 0, 0));

        double spd = speed.getValue();
        tickCounter++;

        if (mode.is("Vanilla")) {
            mc.player.setVelocity(
                    -Math.sin(Math.toRadians(mc.player.yaw)) * spd * mc.player.input.movementForward,
                    mc.player.input.jumping ? spd : (mc.player.input.sneaking ? -spd : 0),
                    Math.cos(Math.toRadians(mc.player.yaw)) * spd * mc.player.input.movementForward
            );
        } else if (mode.is("Packet")) {
            double y = mc.player.getY();
            if (antiKick.isOn() && tickCounter % 20 == 0) {
                y -= 0.04;
            }
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), y, mc.player.getZ(), false)
            );
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), y + 0.01, mc.player.getZ(), false)
            );
            mc.player.setPosition(mc.player.getX(), y, mc.player.getZ());
        } else if (mode.is("MatrixBypass")) {
            double y = mc.player.getY();
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), y, mc.player.getZ(), false)
            );
            if (tickCounter % 10 == 0) {
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), y - 0.02, mc.player.getZ(), false)
                );
                mc.player.setPosition(mc.player.getX(), y - 0.02, mc.player.getZ());
            }
        }
    }
}
