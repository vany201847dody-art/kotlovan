package ru.kotlovan.mod.util;

public class ColorUtils {
    public static int chroma(float offset, float speed, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % (long) (speed * 1000)) / (speed * 1000.0f) + offset;
        return hsvToRgb(hue % 1.0f, saturation, brightness);
    }

    public static int hsvToRgb(float h, float s, float v) {
        int r, g, b;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: r = (int) (v * 255); g = (int) (t * 255); b = (int) (p * 255); break;
            case 1: r = (int) (q * 255); g = (int) (v * 255); b = (int) (p * 255); break;
            case 2: r = (int) (p * 255); g = (int) (v * 255); b = (int) (t * 255); break;
            case 3: r = (int) (p * 255); g = (int) (q * 255); b = (int) (v * 255); break;
            case 4: r = (int) (t * 255); g = (int) (p * 255); b = (int) (v * 255); break;
            default: r = (int) (v * 255); g = (int) (p * 255); b = (int) (q * 255); break;
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int healthColor(float health, float maxHealth) {
        float ratio = health / maxHealth;
        int r = (int) (255 * (1 - ratio));
        int g = (int) (255 * ratio);
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    public static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static float[] getRGB(int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255.0f,
                (color >> 8 & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f,
                (color >> 24 & 0xFF) / 255.0f
        };
    }
}
