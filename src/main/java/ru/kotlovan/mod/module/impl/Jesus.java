package ru.kotlovan.mod.module.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.kotlovan.mod.event.Event;
import ru.kotlovan.mod.event.EventUpdate;
import ru.kotlovan.mod.module.EventTarget;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleInfo;
import ru.kotlovan.mod.setting.ModeSetting;
import ru.kotlovan.mod.setting.NumberSetting;

@ModuleInfo(name = "Jesus", category = "Movement", description = "Хождение по воде")
public class Jesus extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Solid", "Solid", "Dolphin", "Trampoline");
    private final NumberSetting trampolineHeight = new NumberSetting("BounceHeight", 0.42, 0.1, 1.0, 0.05);

    private boolean wasTouchingWater;
    private int dolphinTimer;

    public Jesus() {
        addSetting(mode);
        addSetting(trampolineHeight);
    }

    @Override
    protected void onEnable() {
        wasTouchingWater = false;
        dolphinTimer = 0;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d vel = mc.player.getVelocity();
        BlockPos below = new BlockPos(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        boolean onLiquid = isLiquid(below);

        if (mode.is("Solid")) {
            if (onLiquid && !mc.player.isCreative()) {
                mc.player.setPosition(mc.player.getX(), Math.floor(mc.player.getY()) + 0.99, mc.player.getZ());
                mc.player.setVelocity(vel.x, 0, vel.z);
                if (mc.player.input.jumping) {
                    mc.player.setVelocity(vel.x, 0.31, vel.z);
                }
            }
        } else if (mode.is("Dolphin")) {
            if (mc.player.isTouchingWater() && mc.player.input.jumping) {
                dolphinTimer++;
                if (dolphinTimer > 5) {
                    mc.player.setVelocity(vel.x, 0.33, vel.z);
                    dolphinTimer = 0;
                }
            } else {
                dolphinTimer = 0;
            }
        } else if (mode.is("Trampoline")) {
            if (onLiquid && mc.player.input.jumping) {
                mc.player.setVelocity(vel.x, trampolineHeight.getValue(), vel.z);
            }
        }

        wasTouchingWater = mc.player.isTouchingWater();
    }

    private boolean isLiquid(BlockPos pos) {
        return mc.world.getBlockState(pos).getMaterial().isLiquid();
    }
}
