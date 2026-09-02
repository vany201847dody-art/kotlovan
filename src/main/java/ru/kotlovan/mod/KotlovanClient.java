package ru.kotlovan.mod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class KotlovanClient {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    // ------ Toggles ------
    private boolean nuker;
    private boolean fly;
    private boolean speed;
    private boolean sprint;
    private boolean noFall;
    private boolean killAura;
    private boolean fullBright;
    private boolean step;
    private boolean airJump;
    private boolean spider;
    private boolean autoTool;
    private boolean instantAttack;

    // ------ Values ------
    private int nukerRadius = 5;
    private double speedMul = 2.0;
    private double killAuraRange = 4.5;
    private int oldGamma = -1;

    public void tick() {
        ClientPlayerEntity player = this.mc.player;
        if (player == null || this.mc.world == null) {
            if (this.fullBright && this.oldGamma >= 0) {
                this.mc.options.gamma = this.oldGamma;
                this.oldGamma = -1;
            }
            return;
        }

        this.onKey();
        if (this.sprint) this.applySprint(player);
        if (this.noFall) this.applyNoFall(player);
        if (this.step) player.stepHeight = 1.2f;
        else player.stepHeight = 0.6f;
        if (this.airJump) this.applyAirJump(player);
        if (this.fly) this.applyFly(player);
        if (this.speed) this.applySpeed(player);
        if (this.spider) this.applySpider(player);
        this.applyFullBright(player);
        if (this.nuker) this.doNukerTick(player);
        if (this.killAura) this.doKillAura(player);
    }

    // ---------------- NUKER ----------------
    public void doNukerTick(ClientPlayerEntity player) {
        // Активируемся при зажатии ПКМ (добыча) — очищаем куб вокруг целевого/своего блока
        if (!this.mc.options.keyUse.isPressed()) return;
        BlockPos center = null;
        if (this.mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult) {
            center = ((net.minecraft.util.hit.BlockHitResult) this.mc.crosshairTarget).getBlockPos();
        }
        if (center == null) {
            center = new BlockPos(player.getPos());
        }
        this.clearArea(center, player);
    }

    private void clearArea(BlockPos center, ClientPlayerEntity player) {
        ClientWorld world = this.mc.world;
        int r = Math.max(1, Math.min(this.nukerRadius, 8));
        int count = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (pos.equals(new BlockPos(player.getPos()))) continue;
                    BlockState bs = world.getBlockState(pos);
                    Block b = bs.getBlock();
                    if (b.equals(Blocks.BEDROCK) || b.equals(Blocks.AIR)
                            || b.equals(Blocks.CAVE_AIR) || b.equals(Blocks.VOID_AIR)
                            || b.equals(Blocks.WATER) || b.equals(Blocks.LAVA)) {
                        continue;
                    }
                    if (this.autoTool) {
                        int slot = bestSlotFor(world, pos, bs);
                        if (slot >= 0) player.inventory.selectedSlot = slot;
                    }
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    count++;
                }
            }
        }
        if (count > 0) {
            int size = r * 2 + 1;
            KotlovanMod.chat("Котлован " + size + "x" + size + "x" + size + " очищен (" + count + " блоков)");
        }
    }

    private int bestSlotFor(ClientWorld world, BlockPos pos, BlockState state) {
        if (this.mc.player == null) return -1;
        int best = -1;
        float bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.inventory.getStack(i);
            if (stack.isEmpty()) continue;
            float score = stack.getMiningSpeedMultiplier(state);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            score *= 1.0f + eff * 0.3f;
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    // ---------------- FLY ----------------
    private void applyFly(ClientPlayerEntity player) {
        player.abilities.flying = true;
        Vec3d vel = player.getVelocity();
        double sp = 0.4;
        if (this.speed) sp *= this.speedMul;
        boolean fwd = this.mc.options.keyForward.isPressed();
        boolean back = this.mc.options.keyBack.isPressed();
        boolean left = this.mc.options.keyLeft.isPressed();
        boolean right = this.mc.options.keyRight.isPressed();
        boolean up = this.mc.options.keyJump.isPressed();
        boolean down = this.mc.options.keySneak.isPressed();

        double yawRad = Math.toRadians(player.getYaw(1.0f));
        double forwardX = -Math.sin(yawRad) * sp;
        double forwardZ = Math.cos(yawRad) * sp;
        double strafeX = Math.cos(yawRad) * sp;
        double strafeZ = Math.sin(yawRad) * sp;

        double mx = 0, my = 0, mz = 0;
        if (fwd) { mx += forwardX; mz += forwardZ; }
        if (back) { mx -= forwardX; mz -= forwardZ; }
        if (right) { mx += strafeX; mz -= strafeZ; }
        if (left) { mx -= strafeX; mz += strafeZ; }
        if (up) my += sp;
        if (down) my -= sp;

        double nx = vel.x * 0.0 + mx;
        double ny = up || down ? my : (player.isOnGround() ? 0.0 : vel.y);
        double nz = vel.z * 0.0 + mz;
        player.setVelocity(nx, ny, nz);
    }

    // ---------------- SPEED ----------------
    private void applySpeed(ClientPlayerEntity player) {
        if (this.fly) return;
        if (!player.isOnGround()) return;
        boolean moving = this.mc.options.keyForward.isPressed()
                || this.mc.options.keyBack.isPressed()
                || this.mc.options.keyLeft.isPressed()
                || this.mc.options.keyRight.isPressed();
        if (!moving) return;
        Vec3d vel = player.getVelocity();
        player.setVelocity(vel.x * this.speedMul, vel.y, vel.z * this.speedMul);
    }

    // ---------------- AUTO SPRINT ----------------
    private void applySprint(ClientPlayerEntity player) {
        boolean moving = this.mc.options.keyForward.isPressed();
        if (moving && !player.isSneaking()) {
            player.setSprinting(true);
        }
    }

    // ---------------- NO FALL ----------------
    private void applyNoFall(ClientPlayerEntity player) {
        if (!player.isOnGround()) {
            player.fallDistance = 0.0f;
        }
    }

    // ---------------- AIR JUMP ----------------
    private void applyAirJump(ClientPlayerEntity player) {
        if (!player.isOnGround() && this.mc.options.keyJump.isPressed()) {
            Vec3d vel = player.getVelocity();
            player.setVelocity(vel.x, 0.42, vel.z);
            player.setJumping(false);
        }
    }

    // ---------------- SPIDER / WALL CLIMB ----------------
    private void applySpider(ClientPlayerEntity player) {
        if (player.horizontalCollision && this.mc.options.keyForward.isPressed()) {
            Vec3d vel = player.getVelocity();
            player.setVelocity(vel.x, 0.25, vel.z);
        }
    }

    // ---------------- FULLBRIGHT ----------------
    private void applyFullBright(ClientPlayerEntity player) {
        if (this.fullBright) {
            this.mc.options.gamma = 1000.0;
        } else if (this.mc.options.gamma > 1.0) {
            this.mc.options.gamma = 0.5;
        }
    }

    // ---------------- KILLAURA (плавная килл-аура) ----------------
    private void doKillAura(ClientPlayerEntity player) {
        if (this.mc.interactionManager == null) return;
        Box box = player.getBoundingBox().expand(this.killAuraRange, this.killAuraRange, this.killAuraRange);
        List<Entity> entities = this.mc.world.getOtherEntities(player, box,
                e -> e instanceof LivingEntity
                        && e != player
                        && e.isAlive()
                        && !(e instanceof PlayerEntity)
                        && (e instanceof Monster)
        );
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (Entity e : entities) {
            double d = player.squaredDistanceTo(e);
            if (d < best) {
                best = d;
                target = (LivingEntity) e;
            }
        }
        if (target == null) return;

        // Плавный доворот к цели (без рывков)
        smoothAim(player, target);

        // Атакуем только когда прицел уже близко к цели (плавность, без рывков-подскоков)
        if (isAimedAt(player, target, 8.0f)) {
            this.mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            if (this.instantAttack) player.resetLastAttackedTicks();
        }
    }

    private boolean isAimedAt(ClientPlayerEntity player, LivingEntity target, float maxDeg) {
        double dx = target.getX() - player.getX();
        double dy = (target.getY() + target.getHeight() * 0.5) - (player.getY() + 1.52);
        double dz = target.getZ() - player.getZ();
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(distXZ, 0.0001)));
        float dyaw = Math.abs(wrapDegrees(targetYaw - player.yaw));
        float dpitch = Math.abs(wrapDegrees(targetPitch - player.pitch));
        return dyaw < maxDeg && dpitch < maxDeg * 1.5f;
    }

    private float wrapDegrees(float deg) {
        return ((deg + 180f) % 360f + 360f) % 360f - 180f;
    }

    private void smoothAim(ClientPlayerEntity player, LivingEntity target) {
        double dx = target.getX() - player.getX();
        double dy = (target.getY() + target.getHeight() * 0.5) - (player.getY() + 1.52);
        double dz = target.getZ() - player.getZ();
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(distXZ, 0.0001)));

        // Скорость плавного поворота (градусов за тик)
        float turnSpeed = 6.0f;
        float newYaw = lerpAngle(player.yaw, targetYaw, turnSpeed);
        float newPitch = lerpFloat(player.pitch, targetPitch, turnSpeed * 0.7f);

        player.yaw = newYaw;
        player.pitch = newPitch;
        player.headYaw = newYaw;
        player.bodyYaw = newYaw;
        player.prevYaw = player.yaw;
        player.prevPitch = player.pitch;
    }

    private float lerpFloat(float from, float to, float maxStep) {
        float diff = to - from;
        if (diff > maxStep) diff = maxStep;
        if (diff < -maxStep) diff = -maxStep;
        return from + diff;
    }

    private float lerpAngle(float from, float to, float maxStep) {
        float diff = wrapDegrees(to - from);
        if (diff > maxStep) diff = maxStep;
        if (diff < -maxStep) diff = -maxStep;
        return from + diff;
    }

    // ---------------- TOGGLES ----------------
    private void onKey() {
        if (KotlovanMod.NUKER_KEY.wasPressed()) this.toggleNuker();
        if (KotlovanMod.FLY_KEY.wasPressed()) this.toggleFly();
        if (KotlovanMod.SPEED_KEY.wasPressed()) this.toggleSpeed();
        if (KotlovanMod.SPRINT_KEY.wasPressed()) this.toggleSprint();
        if (KotlovanMod.NOFALL_KEY.wasPressed()) this.toggleNoFall();
        if (KotlovanMod.KILLAURA_KEY.wasPressed()) this.toggleKillAura();
        if (KotlovanMod.FULLBRIGHT_KEY.wasPressed()) this.toggleFullBright();
        if (KotlovanMod.STEP_KEY.wasPressed()) this.toggleStep();
        if (KotlovanMod.AIRJUMP_KEY.wasPressed()) this.toggleAirJump();
        if (KotlovanMod.SPIDER_KEY.wasPressed()) this.toggleSpider();
        if (KotlovanMod.AUTOTOOL_KEY.wasPressed()) this.toggleAutoTool();
        if (KotlovanMod.INSTANT_KEY.wasPressed()) this.toggleInstantAttack();
    }

    public void toggleNuker() { this.nuker = !this.nuker; KotlovanMod.chat("Nuker " + this.on(this.nuker)); }
    public void toggleFly() {
        this.fly = !this.fly;
        if (this.mc.player != null && !this.fly) {
            this.mc.player.abilities.flying = false;
        }
        KotlovanMod.chat("Fly " + this.on(this.fly));
    }
    public void toggleSpeed() { this.speed = !this.speed; KotlovanMod.chat("Speed x" + this.speedMul + " " + this.on(this.speed)); }
    public void toggleSprint() { this.sprint = !this.sprint; KotlovanMod.chat("AutoSprint " + this.on(this.sprint)); }
    public void toggleNoFall() { this.noFall = !this.noFall; KotlovanMod.chat("NoFall " + this.on(this.noFall)); }
    public void toggleKillAura() { this.killAura = !this.killAura; KotlovanMod.chat("KillAura " + this.on(this.killAura)); }
    public void toggleFullBright() {
        this.fullBright = !this.fullBright;
        if (!this.fullBright && this.mc.options != null && this.mc.options.gamma > 1.0) {
            this.mc.options.gamma = 0.5;
        }
        KotlovanMod.chat("FullBright " + this.on(this.fullBright));
    }
    public void toggleStep() { this.step = !this.step; KotlovanMod.chat("Step " + this.on(this.step)); }
    public void toggleAirJump() { this.airJump = !this.airJump; KotlovanMod.chat("AirJump " + this.on(this.airJump)); }
    public void toggleSpider() { this.spider = !this.spider; KotlovanMod.chat("Spider " + this.on(this.spider)); }
    public void toggleAutoTool() { this.autoTool = !this.autoTool; KotlovanMod.chat("AutoTool " + this.on(this.autoTool)); }
    public void toggleInstantAttack() { this.instantAttack = !this.instantAttack; KotlovanMod.chat("InstantAttack " + this.on(this.instantAttack)); }

    private String on(boolean b) {
        return b ? "\u00a7a\u0412\u041a\u041b" : "\u00a7c\u0412\u042b\u041a\u041b";
    }

    // ---------------- GETTERS / SETTERS ----------------
    public boolean isNuker() { return nuker; }
    public boolean isFly() { return fly; }
    public boolean isSpeed() { return speed; }
    public boolean isSprint() { return sprint; }
    public boolean isNoFall() { return noFall; }
    public boolean isKillAura() { return killAura; }
    public boolean isFullBright() { return fullBright; }
    public boolean isStep() { return step; }
    public boolean isAirJump() { return airJump; }
    public boolean isSpider() { return spider; }
    public boolean isAutoTool() { return autoTool; }
    public boolean isInstantAttack() { return instantAttack; }
    public int getNukerRadius() { return nukerRadius; }
    public void setNukerRadius(int r) { this.nukerRadius = r; }
    public double getSpeedMul() { return speedMul; }
    public void setSpeedMul(double m) { this.speedMul = m; }
    public double getKillAuraRange() { return killAuraRange; }
    public void setKillAuraRange(double r) { this.killAuraRange = r; }
}
