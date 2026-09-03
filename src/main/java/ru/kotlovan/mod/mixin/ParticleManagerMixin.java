package ru.kotlovan.mod.mixin;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kotlovan.mod.KotlovanMod;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
    private void kotlovan$noRenderParticles(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                                            LightmapTextureManager lightmap, Camera camera, float tickDelta,
                                            CallbackInfo ci) {
        try {
            if (KotlovanMod.client().isNoRender()) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}
