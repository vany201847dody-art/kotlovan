package ru.kotlovan.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kotlovan.mod.KotlovanClient;
import ru.kotlovan.mod.KotlovanMod;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At("RETURN"))
    private void kotlovan$espTracers(MatrixStack matrices, float tickDelta, long limitTime,
                                     boolean renderBlockOutline, Camera camera,
                                     GameRenderer gameRenderer, LightmapTextureManager lightmap,
                                     Matrix4f matrix4f, CallbackInfo ci) {
        try {
            KotlovanClient k = KotlovanMod.client();
            if (!k.isEsp() && !k.isTracers()) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null) return;

            Vec3d camPos = camera.getPos();
            VertexConsumerProvider.Immediate vcp = this.bufferBuilders.getEntityVertexConsumers();

            // Большая область вокруг камеры (64 блока) для ESP/Tracers
            Box area = new Box(camPos.x - 64, camPos.y - 32, camPos.z - 64,
                    camPos.x + 64, camPos.y + 64, camPos.z + 64);
            java.util.List<Entity> list = mc.world.getEntitiesByClass(Entity.class, area,
                    e -> e instanceof LivingEntity);
            if (list == null) return;

            matrices.push();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            for (Entity e : list) {
                if (!(e instanceof LivingEntity) || e == mc.player) continue;
                if (e.removed) continue;

                // ESP: цветной бокс вокруг моба
                if (k.isEsp()) {
                    Box box = e.getBoundingBox();
                    VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());
                    WorldRenderer.drawBox(matrices, vc, box, 1.0f, 0.2f, 0.2f, 0.8f);
                }

                // Tracers: линия от камеры к мобу
                if (k.isTracers()) {
                    Vec3d target = e.getCameraPosVec(tickDelta);
                    VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());
                    float dx = (float) (target.x - camPos.x);
                    float dy = (float) (target.y - camPos.y);
                    float dz = (float) (target.z - camPos.z);
                    vc.vertex(matrices.peek().getModel(), 0.0f, 0.0f, 0.0f).color(0.2f, 1.0f, 0.2f, 1.0f).next();
                    vc.vertex(matrices.peek().getModel(), dx, dy, dz).color(0.2f, 1.0f, 0.2f, 1.0f).next();
                }
            }

            if (k.isEsp() || k.isTracers()) {
                vcp.draw(RenderLayer.getLines());
            }
            matrices.pop();
        } catch (Throwable ignored) {
        }
    }
}
