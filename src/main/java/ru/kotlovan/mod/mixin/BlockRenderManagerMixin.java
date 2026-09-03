package ru.kotlovan.mod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kotlovan.mod.KotlovanMod;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

    // Руды и полезные блоки, которые видно в X-Ray
    private static final Set<Block> XRAY_BLOCKS = new HashSet<>();
    static {
        XRAY_BLOCKS.add(Blocks.DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.IRON_ORE);
        XRAY_BLOCKS.add(Blocks.COAL_ORE);
        XRAY_BLOCKS.add(Blocks.REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        XRAY_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.BEDROCK);
        XRAY_BLOCKS.add(Blocks.MOSSY_COBBLESTONE);
        XRAY_BLOCKS.add(Blocks.OBSIDIAN);
        XRAY_BLOCKS.add(Blocks.SPAWNER);
        XRAY_BLOCKS.add(Blocks.END_PORTAL_FRAME);
    }

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void kotlovan$xray(BlockState state, BlockPos pos, BlockRenderView world,
                              MatrixStack matrix, VertexConsumer vertexConsumer,
                              boolean cull, Random random, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (KotlovanMod.client().isXray() && !XRAY_BLOCKS.contains(state.getBlock())) {
                // Не рендерим обычные блоки — просвечивают только руды/бэдрок
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
        }
    }
}
