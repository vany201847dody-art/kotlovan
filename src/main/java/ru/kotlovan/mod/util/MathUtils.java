package ru.kotlovan.mod.util;

public class MathUtils {
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float wrapDegrees(float degrees) {
        return ((degrees + 180.0f) % 360.0f + 360.0f) % 360.0f - 180.0f;
    }

    public static double wrapDegrees(double degrees) {
        return ((degrees + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    public static float radToDeg(float rad) {
        return (float) Math.toDegrees(rad);
    }

    public static float degToRad(float deg) {
        return (float) Math.toRadians(deg);
    }

    public static float random(float min, float max) {
        return min + (float) (Math.random() * (max - min));
    }

    public static double random(double min, double max) {
        return min + Math.random() * (max - min);
    }

    public static int random(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    public static float getAngle(float from, float to, float maxStep) {
        float diff = wrapDegrees(to - from);
        return clamp(diff, -maxStep, maxStep);
    }
}
