package ru.kotlovan.mod.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {

    public static void drawBox(MatrixStack matrices, Box box, float r, float g, float b, float a) {
        drawBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
    }

    public static void drawBox(MatrixStack matrices, double x1, double y1, double z1,
                               double x2, double y2, double z2, float r, float g, float b, float a) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double dx = x1 - cam.x;
        double dy = y1 - cam.y;
        double dz = z1 - cam.z;
        double sx = x2 - x1;
        double sy = y2 - y1;
        double sz = z2 - z1;

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(2.0f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        bb.begin(1, VertexFormats.POSITION_COLOR);

        addBoxVertices(bb, dx, dy, dz, sx, sy, sz, r, g, b, a);

        bb.end();
        tess.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private static void addBoxVertices(BufferBuilder bb, double x, double y, double z,
                                       double w, double h, double d, float r, float g, float b, float a) {
        bb.vertex(x, y, z).color(r, g, b, a).next();
        bb.vertex(x + w, y, z).color(r, g, b, a).next();
        bb.vertex(x + w, y, z).color(r, g, b, a).next();
        bb.vertex(x + w, y, z + d).color(r, g, b, a).next();
        bb.vertex(x + w, y, z + d).color(r, g, b, a).next();
        bb.vertex(x, y, z + d).color(r, g, b, a).next();
        bb.vertex(x, y, z + d).color(r, g, b, a).next();
        bb.vertex(x, y, z).color(r, g, b, a).next();

        bb.vertex(x, y + h, z).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, z).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, z).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        bb.vertex(x, y + h, z + d).color(r, g, b, a).next();
        bb.vertex(x, y + h, z + d).color(r, g, b, a).next();
        bb.vertex(x, y + h, z).color(r, g, b, a).next();

        bb.vertex(x, y, z).color(r, g, b, a).next();
        bb.vertex(x, y + h, z).color(r, g, b, a).next();
        bb.vertex(x + w, y, z).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, z).color(r, g, b, a).next();
        bb.vertex(x + w, y, z + d).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        bb.vertex(x, y, z + d).color(r, g, b, a).next();
        bb.vertex(x, y + h, z + d).color(r, g, b, a).next();
    }

    public static void drawLine(MatrixStack matrices, float x1, float y1, float z1,
                                float x2, float y2, float z2, float r, float g, float b, float a) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        bb.begin(1, VertexFormats.POSITION_COLOR);

        bb.vertex(x1 - cam.x, y1 - cam.y, z1 - cam.z).color(r, g, b, a).next();
        bb.vertex(x2 - cam.x, y2 - cam.y, z2 - cam.z).color(r, g, b, a).next();

        bb.end();
        tess.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawCircle(MatrixStack matrices, double x, double y, double z,
                                  float radius, float r, float g, float b, float a) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        bb.begin(1, VertexFormats.POSITION_COLOR);

        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;
            bb.vertex(x + Math.cos(angle1) * radius - cam.x, y - cam.y, z + Math.sin(angle1) * radius - cam.z)
                    .color(r, g, b, a).next();
            bb.vertex(x + Math.cos(angle2) * radius - cam.x, y - cam.y, z + Math.sin(angle2) * radius - cam.z)
                    .color(r, g, b, a).next();
        }

        bb.end();
        tess.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawRect(float x, float y, float w, float h, int color) {
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.color4f(r, g, b, a);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        bb.begin(7, VertexFormats.POSITION_COLOR);

        bb.vertex(x, y, 0).color(r, g, b, a).next();
        bb.vertex(x, y + h, 0).color(r, g, b, a).next();
        bb.vertex(x + w, y + h, 0).color(r, g, b, a).next();
        bb.vertex(x + w, y, 0).color(r, g, b, a).next();

        bb.end();
        tess.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawGradientRect(float x, float y, float w, float h, int topColor, int bottomColor) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();

        float ta = (topColor >> 24 & 0xFF) / 255.0f;
        float tr = (topColor >> 16 & 0xFF) / 255.0f;
        float tg = (topColor >> 8 & 0xFF) / 255.0f;
        float tb = (topColor & 0xFF) / 255.0f;

        float ba2 = (bottomColor >> 24 & 0xFF) / 255.0f;
        float br = (bottomColor >> 16 & 0xFF) / 255.0f;
        float bg = (bottomColor >> 8 & 0xFF) / 255.0f;
        float bb2 = (bottomColor & 0xFF) / 255.0f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(7, VertexFormats.POSITION_COLOR);

        buf.vertex(x, y, 0).color(tr, tg, tb, ta).next();
        buf.vertex(x, y + h, 0).color(br, bg, bb2, ba2).next();
        buf.vertex(x + w, y + h, 0).color(br, bg, bb2, ba2).next();
        buf.vertex(x + w, y, 0).color(tr, tg, tb, ta).next();

        buf.end();
        tess.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
