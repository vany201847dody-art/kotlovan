package ru.kotlovan.mod.setting;

public class ColorSetting extends Setting<int[]> {
    private final boolean rainbow;

    public ColorSetting(String name, int r, int g, int b, int a) {
        super(name, new int[]{r, g, b, a});
        this.rainbow = false;
    }

    public int getR() {
        return getValue()[0];
    }

    public int getG() {
        return getValue()[1];
    }

    public int getB() {
        return getValue()[2];
    }

    public int getA() {
        return getValue()[3];
    }

    public float getRf() {
        return getR() / 255.0f;
    }

    public float getGf() {
        return getG() / 255.0f;
    }

    public float getBf() {
        return getB() / 255.0f;
    }

    public float getAf() {
        return getA() / 255.0f;
    }

    public int toInt() {
        return (getA() << 24) | (getR() << 16) | (getG() << 8) | getB();
    }
}
