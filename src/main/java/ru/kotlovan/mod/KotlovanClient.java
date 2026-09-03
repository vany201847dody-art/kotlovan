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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.decoration.ArmorStandEntity;

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
    private boolean glide;
    private boolean longJump;
    private boolean criticals;
    private boolean autoSword;
    private boolean freecam;
    private boolean chestStealer;

    // ------ Новые модули ------
    private boolean timer;
    private boolean velocity;
    private boolean noWeb;
    private boolean fastLadder;
    private boolean safeWalk;
    private boolean esp;
    private boolean tracers;
    private boolean xray;
    private boolean noRender;
    private int timerMul = 2;
    private double velocityReduce = 0.8;

    // ------ Режим скрытия HUD (чтобы в записи/на скрине не светились читы) ------
    private boolean hideHud = false;

    // ------ Freecam state ------
    private ArmorStandEntity cameraStand;
    private Vec3d freecamPlayerPos;
    private float freecamPlayerYaw;
    private float freecamPlayerPitch;
    private boolean wasFlying;

    // ------ Значения ------
    private int nukerRadius = 5;
    private double speedMul = 2.0;
    private double killAuraRange = 4.5;
    private int oldGamma = -1;

    // ------ NameSpoof ------
    private String spoofName = "";

    public void tick() {
        ClientPlayerEntity player = this.mc.player;
        if (player == null || this.mc.world == null) {
            if (this.fullBright && this.oldGamma >= 0) {
                this.mc.options.gamma = this.oldGamma;
                this.oldGamma = -1;
            }
            if (this.freecam) this.disableFreecam();
            return;
        }

        this.onKey();
        if (this.freecam) {
            this.tickFreecam(player);
            return;
        }
        if (this.sprint) this.applySprint(player);
        if (this.noFall) this.applyNoFall(player);
        if (this.step) player.stepHeight = 1.2f;
        else player.stepHeight = 0.6f;
        if (this.airJump) this.applyAirJump(player);
        if (this.fly) this.applyFly(player);
        if (this.speed) this.applySpeed(player);
        if (this.glide) this.applyGlide(player);
        if (this.longJump) this.applyLongJump(player);
        if (this.spider) this.applySpider(player);
        this.applyFullBright(player);
        if (this.nuker) this.doNukerTick(player);
        if (this.chestStealer) this.tryChestSteal();
        if (this.killAura) this.doKillAura(player);
        if (this.velocity) this.applyVelocity(player);
        if (this.noWeb) this.applyNoWeb(player);
        if (this.fastLadder && player.isClimbing()) {
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, 0.22, v.z);
        }
        if (this.safeWalk) {
            player.setVelocity(player.getVelocity());
        }
    }

    // ================ VELOCITY (анти-отброс) ================
    private void applyVelocity(ClientPlayerEntity player) {
        Vec3d v = player.getVelocity();
        if (Math.abs(v.y) < 0.3 && v.y < 0) v = new Vec3d(v.x, v.y * (1.0 - this.velocityReduce), v.z);
        player.setVelocity(v.x * (1.0 - this.velocityReduce * 0.5), v.y, v.z * (1.0 - this.velocityReduce * 0.5));
    }

    // ================ NO WEB (не вязнуть в паутине) ================
    private void applyNoWeb(ClientPlayerEntity player) {
        player.stepHeight = 1.2f;
        Vec3d v = player.getVelocity();
        player.setVelocity(v.x * 0.98, v.y, v.z * 0.98);
    }

    // ================ NUKER (реальное ломание блоков пакетами) ================
    private BlockPos nukerCurrent;
    private int nukerIndex;
    private List<BlockPos> nukerQueue = new ArrayList<>();
    private int nukerScanTick;

    public void doNukerTick(ClientPlayerEntity player) {
        if (player == null || this.mc.interactionManager == null) return;
        // Активируемся при зажатии ПКМ (добыча)
        boolean active = this.mc.options.keyUse.isPressed();
        if (!active) {
            this.nukerCurrent = null;
            this.nukerQueue.clear();
            if (this.mc.interactionManager.isBreakingBlock()) {
                this.mc.interactionManager.cancelBlockBreaking();
            }
            return;
        }

        BlockPos center = null;
        if (this.mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult) {
            center = ((net.minecraft.util.hit.BlockHitResult) this.mc.crosshairTarget).getBlockPos();
        }
        if (center == null) {
            center = new BlockPos(player.getPos());
        }

        // Пересобираем очередь блоков вокруг цели каждые 20 тиков (дешево)
        this.nukerScanTick++;
        if (this.nukerQueue.isEmpty() || this.nukerScanTick >= 20) {
            this.nukerQueue.clear();
            int r = Math.max(1, Math.min(this.nukerRadius, 8));
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        BlockPos pos = center.add(dx, dy, dz);
                        BlockState bs = this.mc.world.getBlockState(pos);
                        Block b = bs.getBlock();
                        if (bs.isAir() || b.equals(Blocks.BEDROCK)
                                || b.equals(Blocks.BARRIER)
                                || b.equals(Blocks.WATER) || b.equals(Blocks.LAVA)) {
                            continue;
                        }
                        this.nukerQueue.add(pos);
                    }
                }
            }
            this.nukerScanTick = 0;
            this.nukerIndex = 0;
        }

        if (this.nukerQueue.isEmpty()) return;

        // Берём первый живой блок в очереди
        BlockPos target = null;
        while (this.nukerIndex < this.nukerQueue.size()) {
            BlockPos p = this.nukerQueue.get(this.nukerIndex);
            BlockState bs = this.mc.world.getBlockState(p);
            if (!bs.isAir() && !bs.getBlock().equals(Blocks.BEDROCK)) {
                target = p;
                break;
            }
            this.nukerIndex++;
        }
        if (target == null) return;

        // Реальные пакеты ломания: updateBlockBreakingProgress возвращает true, когда блок сломан
        if (this.autoTool) {
            BlockState bs = this.mc.world.getBlockState(target);
            int slot = bestSlotFor(this.mc.world, target, bs);
            if (slot >= 0 && slot != player.inventory.selectedSlot) {
                player.inventory.selectedSlot = slot;
            }
        }

        boolean broken = this.mc.interactionManager.updateBlockBreakingProgress(target, Direction.UP);
        this.nukerCurrent = target;
        if (broken) {
            this.nukerIndex++;
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

    // ================ CHEST STEALER ================
    private int chestStealCooldown = 0;

    private void tryChestSteal() {
        if (this.mc.player == null || this.mc.interactionManager == null) return;
        if (chestStealCooldown > 0) { chestStealCooldown--; return; }
        net.minecraft.screen.ScreenHandler handler = this.mc.player.currentScreenHandler;
        if (handler == null) return;
        // Определяем, открыт ли контейнер (не обычный игровой экран без контейнера)
        int containerSlots = handler.slots.size() - 36;
        if (containerSlots <= 0) return;

        boolean moved = false;
        for (int i = 0; i < containerSlots; i++) {
            try {
                ItemStack stack = handler.getSlot(i).getStack();
                if (!stack.isEmpty()) {
                    this.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                    moved = true;
                    chestStealCooldown = 3;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        if (moved) {
            KotlovanMod.chat("§aChestStealer: забираю предметы...");
        }
    }

    // ================ FLY ================
    private void applyFly(ClientPlayerEntity player) {
        player.abilities.flying = true;
        Vec3d vel = player.getVelocity();
        double sp = 0.4;
        if (this.speed) sp *= this.speedMul;
        if (this.timer) sp *= this.timerMul;
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

        double nx = mx;
        double ny = up || down ? my : (player.isOnGround() ? 0.0 : vel.y);
        double nz = mz;
        player.setVelocity(nx, ny, nz);
    }

    // ================ SPEED ================
    private void applySpeed(ClientPlayerEntity player) {
        if (this.fly) return;
        if (!player.isOnGround()) return;
        boolean moving = this.mc.options.keyForward.isPressed()
                || this.mc.options.keyBack.isPressed()
                || this.mc.options.keyLeft.isPressed()
                || this.mc.options.keyRight.isPressed();
        if (!moving) return;
        Vec3d vel = player.getVelocity();
        double tm = this.timer ? this.timerMul : 1.0;
        player.setVelocity(vel.x * this.speedMul * tm, vel.y, vel.z * this.speedMul * tm);
    }

    // ================ GLIDE (плавное замедленное падение) ================
    private void applyGlide(ClientPlayerEntity player) {
        if (player.isOnGround() || this.mc.options.keyJump.isPressed()) return;
        Vec3d vel = player.getVelocity();
        if (vel.y < 0) {
            player.setVelocity(vel.x, vel.y * 0.55, vel.z);
        }
    }

    // ================ LONG JUMP (дальний прыжок) ================
    private void applyLongJump(ClientPlayerEntity player) {
        boolean moving = this.mc.options.keyForward.isPressed();
        if (!moving || !this.mc.options.keyJump.isPressed()) return;
        if (!player.isOnGround()) return;
        double yawRad = Math.toRadians(player.getYaw(1.0f));
        double boost = 2.6 * (this.speed ? this.speedMul : 1.0);
        double vx = -Math.sin(yawRad) * boost;
        double vz = Math.cos(yawRad) * boost;
        player.setVelocity(vx, 0.5, vz);
    }

    // ================ AUTO SPRINT ================
    private void applySprint(ClientPlayerEntity player) {
        boolean moving = this.mc.options.keyForward.isPressed();
        if (moving && !player.isSneaking()) {
            player.setSprinting(true);
        }
    }

    // ================ NO FALL ================
    private void applyNoFall(ClientPlayerEntity player) {
        if (!player.isOnGround()) {
            player.fallDistance = 0.0f;
        }
    }

    // ================ AIR JUMP ================
    private void applyAirJump(ClientPlayerEntity player) {
        if (!player.isOnGround() && this.mc.options.keyJump.isPressed()) {
            Vec3d vel = player.getVelocity();
            player.setVelocity(vel.x, 0.42, vel.z);
            player.setJumping(false);
        }
    }

    // ================ SPIDER / WALL CLIMB ================
    private void applySpider(ClientPlayerEntity player) {
        if (player.horizontalCollision && this.mc.options.keyForward.isPressed()) {
            Vec3d vel = player.getVelocity();
            player.setVelocity(vel.x, 0.25, vel.z);
        }
    }

    // ================ FULLBRIGHT ================
    private void applyFullBright(ClientPlayerEntity player) {
        if (this.fullBright) {
            this.mc.options.gamma = 1000.0;
        } else if (this.mc.options.gamma > 1.0) {
            this.mc.options.gamma = 0.5;
        }
    }

    // ================ KILLAURA (плавная, +Criticals +AutoSword) ================
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

        // AutoSword: выбираем лучший меч-инструмент в хотбаре
        if (this.autoSword) {
            int slot = bestMeleeSlot();
            if (slot >= 0 && slot != player.inventory.selectedSlot) {
                player.inventory.selectedSlot = slot;
            }
        }

        // Плавный доворот к цели (без рывков)
        smoothAim(player, target);

        // Criticals: прыжок перед ударом для крит-урона
        if (this.criticals && player.isOnGround()) {
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, 0.42, v.z);
            player.setOnGround(false);
        }

        // Атакуем только когда прицел уже близко к цели
        if (isAimedAt(player, target, 8.0f)) {
            this.mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            if (this.instantAttack) player.resetLastAttackedTicks();
        }
    }

    private int bestMeleeSlot() {
        if (this.mc.player == null) return -1;
        int best = -1;
        float bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.inventory.getStack(i);
            if (stack.isEmpty()) continue;
            float dmg = getItemDamage(stack);
            if (dmg > bestScore) {
                bestScore = dmg;
                best = i;
            }
        }
        return best;
    }

    private float getItemDamage(ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.item.SwordItem) {
            return ((net.minecraft.item.SwordItem) stack.getItem()).getAttackDamage() + 1.0f;
        }
        if (stack.getItem() instanceof net.minecraft.item.AxeItem) {
            return ((net.minecraft.item.AxeItem) stack.getItem()).getAttackDamage();
        }
        return 1.0f;
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

    // ================ FREE CAM (камера отлетает от тела, «чтобы не палили») ================
    private void tickFreecam(ClientPlayerEntity player) {
        if (this.cameraStand == null) {
            this.enableFreecam(player);
            return;
        }
        // Тело игрока замирает на месте (не двигаем физически), но смотрит мышью
        if (this.freecamPlayerPos != null) {
            player.setPosition(this.freecamPlayerPos.x, this.freecamPlayerPos.y, this.freecamPlayerPos.z);
        }
        player.setVelocity(0, 0, 0);
        player.abilities.flying = true;
        player.noClip = true;

        // Ключевой фикс: мышь уже обновила player.yaw/pitch НА ЭТОТ кадр,
        // а камера смотрит из cameraStand (cameraEntity). Копируем поворот
        // на stand, иначе камеру нельзя крутить мышью (была заморожена).
        this.cameraStand.yaw = player.yaw;
        this.cameraStand.pitch = player.pitch;
        this.cameraStand.headYaw = player.yaw;
        this.cameraStand.bodyYaw = player.yaw;
        this.cameraStand.prevYaw = player.yaw;
        this.cameraStand.prevPitch = player.pitch;

        double sp = 0.6;
        boolean fwd = this.mc.options.keyForward.isPressed();
        boolean back = this.mc.options.keyBack.isPressed();
        boolean left = this.mc.options.keyLeft.isPressed();
        boolean right = this.mc.options.keyRight.isPressed();
        boolean up = this.mc.options.keyJump.isPressed();
        boolean down = this.mc.options.keySneak.isPressed();
        // Движение по направлению взгляда КАМЕРЫ (player.yaw == взгляд)
        double yawRad = Math.toRadians(player.yaw);

        double mx = 0, my = 0, mz = 0;
        if (fwd) { mx += -Math.sin(yawRad) * sp; mz += Math.cos(yawRad) * sp; }
        if (back) { mx -= -Math.sin(yawRad) * sp; mz -= Math.cos(yawRad) * sp; }
        if (right) { mx += Math.cos(yawRad) * sp; mz -= Math.sin(yawRad) * sp; }
        if (left) { mx -= Math.cos(yawRad) * sp; mz += Math.sin(yawRad) * sp; }
        if (up) my += sp;
        if (down) my -= sp;

        Vec3d cp = this.cameraStand.getPos();
        this.cameraStand.setPosition(cp.x + mx, cp.y + my, cp.z + mz);
        this.cameraStand.refreshPositionAndAngles(cp.x + mx, cp.y + my, cp.z + mz, this.cameraStand.yaw, this.cameraStand.pitch);
    }

    private void enableFreecam(ClientPlayerEntity player) {
        try {
            this.wasFlying = player.abilities.flying;
            this.freecamPlayerPos = player.getPos();
            this.freecamPlayerYaw = player.yaw;
            this.freecamPlayerPitch = player.pitch;

            Vec3d p = player.getPos();
            this.cameraStand = new ArmorStandEntity(this.mc.world, p.x, p.y + 1.6, p.z);
            this.cameraStand.setNoGravity(true);
            this.cameraStand.setInvisible(true);
            this.cameraStand.setSilent(true);
            this.cameraStand.setInvulnerable(true);
            this.cameraStand.yaw = player.yaw;
            this.cameraStand.pitch = player.pitch;
            this.cameraStand.headYaw = player.yaw;
            this.cameraStand.bodyYaw = player.yaw;
            this.cameraStand.refreshPositionAndAngles(p.x, p.y + 1.6, p.z, player.yaw, player.pitch);

            this.mc.cameraEntity = this.cameraStand;
            player.abilities.flying = true;
            player.noClip = true;
            KotlovanMod.chat("§aFreecam §aВКЛ");
        } catch (Exception e) {
            KotlovanMod.chat("§cFreecam: ошибка запуска");
        }
    }

    private void disableFreecam() {
        try {
            this.mc.cameraEntity = this.mc.player;
            if (this.cameraStand != null) {
                this.cameraStand.remove();
                this.cameraStand = null;
            }
            if (this.mc.player != null) {
                if (!this.wasFlying) this.mc.player.abilities.flying = false;
                this.mc.player.noClip = false;
            }
            this.freecamPlayerPos = null;
            this.freecam = false;
            KotlovanMod.chat("§cFreecam §cВЫКЛ");
        } catch (Exception ignored) {
        }
    }

    // ================ TOGGLES ================
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
        if (KotlovanMod.GLIDE_KEY.wasPressed()) this.toggleGlide();
        if (KotlovanMod.LONGJUMP_KEY.wasPressed()) this.toggleLongJump();
        if (KotlovanMod.CRITICALS_KEY.wasPressed()) this.toggleCriticals();
        if (KotlovanMod.AUTOSWORD_KEY.wasPressed()) this.toggleAutoSword();
        if (KotlovanMod.FREECAM_KEY.wasPressed()) this.toggleFreecam();
        if (KotlovanMod.CHESTSTEAL_KEY.wasPressed()) this.toggleChestStealer();
        if (KotlovanMod.HIDEHUD_KEY.wasPressed()) this.toggleHideHud();
        if (KotlovanMod.PANIC_KEY.wasPressed()) this.panic();
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
    public void toggleGlide() { this.glide = !this.glide; KotlovanMod.chat("Glide " + this.on(this.glide)); }
    public void toggleLongJump() { this.longJump = !this.longJump; KotlovanMod.chat("LongJump " + this.on(this.longJump)); }
    public void toggleCriticals() { this.criticals = !this.criticals; KotlovanMod.chat("Criticals " + this.on(this.criticals)); }
    public void toggleAutoSword() { this.autoSword = !this.autoSword; KotlovanMod.chat("AutoSword " + this.on(this.autoSword)); }
    public void toggleChestStealer() { this.chestStealer = !this.chestStealer; KotlovanMod.chat("ChestStealer " + this.on(this.chestStealer)); }
    public void toggleFreecam() {
        if (this.freecam) {
            this.disableFreecam();
        } else {
            this.enableFreecam(this.mc.player);
        }
    }
    public void toggleHideHud() { this.hideHud = !this.hideHud; KotlovanMod.chat("Скрыть HUD-модули " + this.on(this.hideHud)); }
    public void toggleTimer() { this.timer = !this.timer; KotlovanMod.chat("Timer x" + this.timerMul + " " + this.on(this.timer)); }
    public void toggleVelocity() { this.velocity = !this.velocity; KotlovanMod.chat("Velocity " + this.on(this.velocity)); }
    public void toggleNoWeb() { this.noWeb = !this.noWeb; KotlovanMod.chat("NoWeb " + this.on(this.noWeb)); }
    public void toggleFastLadder() { this.fastLadder = !this.fastLadder; KotlovanMod.chat("FastLadder " + this.on(this.fastLadder)); }
    public void toggleSafeWalk() { this.safeWalk = !this.safeWalk; KotlovanMod.chat("SafeWalk " + this.on(this.safeWalk)); }
    public void toggleEsp() { this.esp = !this.esp; KotlovanMod.chat("ESP " + this.on(this.esp)); }
    public void toggleTracers() { this.tracers = !this.tracers; KotlovanMod.chat("Tracers " + this.on(this.tracers)); }
    public void toggleXray() { this.xray = !this.xray; KotlovanMod.chat("X-Ray " + this.on(this.xray)); }
    public void toggleNoRender() { this.noRender = !this.noRender; KotlovanMod.chat("NoRender " + this.on(this.noRender)); }

    public boolean isHideHud() { return this.hideHud; }

    // ================ ПАНИКА (ТРЕВОГА): выключить всё и спрятать GUI ================
    public void panic() {
        this.nuker = false;
        this.fly = false;
        this.speed = false;
        this.sprint = false;
        this.noFall = false;
        this.killAura = false;
        this.fullBright = false;
        this.step = false;
        this.airJump = false;
        this.spider = false;
        this.autoTool = false;
        this.instantAttack = false;
        this.glide = false;
        this.longJump = false;
        this.criticals = false;
        this.autoSword = false;
        this.chestStealer = false;
        this.timer = false;
        this.velocity = false;
        this.noWeb = false;
        this.fastLadder = false;
        this.safeWalk = false;
        this.esp = false;
        this.tracers = false;
        this.xray = false;
        this.noRender = false;

        if (this.freecam) this.disableFreecam();

        if (this.mc.player != null) {
            this.mc.player.abilities.flying = false;
            this.mc.player.noClip = false;
            this.mc.player.stepHeight = 0.6f;
            if (this.mc.options != null && this.mc.options.gamma > 1.0) {
                this.mc.options.gamma = 0.5;
            }
        }
        // Скрыть GUI, если чит-меню открыто
        if (this.mc.currentScreen instanceof ClickGuiScreen) {
            this.mc.openScreen(null);
        }
        KotlovanMod.chat("§c⚠ ТРЕВОГА — все читы выключены!");
    }

    // ================ NameSpoof ================
    public void setName(String name) {
        this.spoofName = name;
    }
    public String getName() {
        return this.spoofName;
    }

    // ================ КОНФИГ ================
    public void updateConfig() {
        KotlovanConfig.save(this);
    }
    public void loadConfig() {
        KotlovanConfig.load(this);
    }

    public void enableConfigLoaded() {}

    private String on(boolean b) {
        return b ? "\u00a7a\u0412\u041a\u041b" : "\u00a7c\u0412\u042b\u041a\u041b";
    }

    // ---------------- SETTERS для загрузки конфига ----------------
    public void setFlyOn(boolean v){ this.fly=v; }
    public void setSpeedOn(boolean v){ this.speed=v; }
    public void setNukerOn(boolean v){ this.nuker=v; }
    public void setKillAuraOn(boolean v){ this.killAura=v; }
    public void setSprintOn(boolean v){ this.sprint=v; }
    public void setNoFallOn(boolean v){ this.noFall=v; }
    public void setFullBrightOn(boolean v){ this.fullBright=v; }
    public void setStepOn(boolean v){ this.step=v; }
    public void setAirJumpOn(boolean v){ this.airJump=v; }
    public void setSpiderOn(boolean v){ this.spider=v; }
    public void setAutoToolOn(boolean v){ this.autoTool=v; }
    public void setInstantAttackOn(boolean v){ this.instantAttack=v; }
    public void setGlideOn(boolean v){ this.glide=v; }
    public void setLongJumpOn(boolean v){ this.longJump=v; }
    public void setCriticalsOn(boolean v){ this.criticals=v; }
    public void setAutoSwordOn(boolean v){ this.autoSword=v; }
    public void setChestStealerOn(boolean v){ this.chestStealer=v; }
    public void setHideHudOn(boolean v){ this.hideHud=v; }
    public void setTimerOn(boolean v){ this.timer=v; }
    public void setVelocityOn(boolean v){ this.velocity=v; }
    public void setNoWebOn(boolean v){ this.noWeb=v; }
    public void setFastLadderOn(boolean v){ this.fastLadder=v; }
    public void setSafeWalkOn(boolean v){ this.safeWalk=v; }
    public void setEspOn(boolean v){ this.esp=v; }
    public void setTracersOn(boolean v){ this.tracers=v; }
    public void setXrayOn(boolean v){ this.xray=v; }
    public void setNoRenderOn(boolean v){ this.noRender=v; }

    // ---------------- GETTERS ----------------
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
    public boolean isGlide() { return glide; }
    public boolean isLongJump() { return longJump; }
    public boolean isCriticals() { return criticals; }
    public boolean isAutoSword() { return autoSword; }
    public boolean isFreecam() { return freecam; }
    public boolean isChestStealer() { return chestStealer; }
    public boolean isTimer() { return timer; }
    public boolean isVelocity() { return velocity; }
    public boolean isNoWeb() { return noWeb; }
    public boolean isFastLadder() { return fastLadder; }
    public boolean isSafeWalk() { return safeWalk; }
    public boolean isEsp() { return esp; }
    public boolean isTracers() { return tracers; }
    public boolean isXray() { return xray; }
    public boolean isNoRender() { return noRender; }
    public int getTimerMul() { return timerMul; }
    public void setTimerMul(int m) { this.timerMul = Math.max(1, Math.min(10, m)); }
    public double getVelocityReduce() { return velocityReduce; }
    public void setVelocityReduce(double v) { this.velocityReduce = v; }
    public int getNukerRadius() { return nukerRadius; }
    public void setNukerRadius(int r) { this.nukerRadius = r; }
    public double getSpeedMul() { return speedMul; }
    public void setSpeedMul(double m) { this.speedMul = m; }
    public double getKillAuraRange() { return killAuraRange; }
    public void setKillAuraRange(double r) { this.killAuraRange = r; }
}
