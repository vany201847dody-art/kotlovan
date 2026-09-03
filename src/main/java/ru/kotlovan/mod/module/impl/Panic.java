package ru.kotlovan.mod.module.impl;

import ru.kotlovan.mod.event.Event;
import ru.kotlovan.mod.event.EventBus;
import ru.kotlovan.mod.event.EventUpdate;
import ru.kotlovan.mod.module.EventTarget;
import ru.kotlovan.mod.module.Module;
import ru.kotlovan.mod.module.ModuleInfo;
import ru.kotlovan.mod.module.ModuleManager;

@ModuleInfo(name = "Panic", category = "System", description = "Экстренное отключение всех модулей")
public class Panic extends Module {

    @Override
    protected void onEnable() {
        // Экстренное скрытие: отключаем ВСЕ модули, обнуляем цели
        ModuleManager mm = ModuleManager.getInstance();
        for (Module m : mm.getModules()) {
            if (m != this) {
                m.setEnabled(false);
            }
        }

        // Отписываем весь клиент от шины событий — модули перестают реагировать
        for (Module m : mm.getModules()) {
            EventBus.getInstance().unregister(m);
        }

        // Полностью деактивируем и панику
        setEnabled(false);
    }

    // Паника срабатывает мгновенно при включении — onDisable не вызываем повторно
    @EventTarget(priority = Event.Priority.HIGHEST)
    public void onUpdate(EventUpdate event) {
        if (!isEnabled()) return;
        onEnable();
    }
}
