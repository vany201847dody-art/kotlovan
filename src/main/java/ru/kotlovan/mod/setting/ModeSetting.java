package ru.kotlovan.mod.setting;

public class ModeSetting extends Setting<String> {
    private final String[] modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = modes;
    }

    public String[] getModes() {
        return modes;
    }

    public int getModeIndex() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(getValue())) return i;
        }
        return 0;
    }

    public void cycle() {
        int next = (getModeIndex() + 1) % modes.length;
        setValue(modes[next]);
    }

    public boolean is(String mode) {
        return getValue().equals(mode);
    }
}
