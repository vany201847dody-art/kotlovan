package ru.kotlovan.mod.module;

import ru.kotlovan.mod.module.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        modules.add(new KillAura());
        modules.add(new TargetHUD());
        modules.add(new Fly());
        modules.add(new Jesus());
        modules.add(new ESP());
        modules.add(new Panic());
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getByCategory(String category) {
        return modules.stream()
                .filter(m -> m.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public void disableAll() {
        for (Module module : modules) {
            module.setEnabled(false);
        }
    }
}
