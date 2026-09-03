package ru.kotlovan.mod.module;

import net.minecraft.client.MinecraftClient;
import ru.kotlovan.mod.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name;
    private final String category;
    private final String description;
    private int key;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module() {
        ModuleInfo info = this.getClass().getAnnotation(ModuleInfo.class);
        if (info != null) {
            this.name = info.name();
            this.category = info.category();
            this.description = info.description();
            this.key = info.key();
        } else {
            this.name = "Unknown";
            this.category = "Misc";
            this.description = "";
        }
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }
}
