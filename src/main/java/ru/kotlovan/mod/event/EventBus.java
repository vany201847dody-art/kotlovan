package ru.kotlovan.mod.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final Map<Class<? extends Event>, List<Handler>> handlerMap = new ConcurrentHashMap<>();

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            ru.kotlovan.mod.module.EventTarget annotation = method.getAnnotation(ru.kotlovan.mod.module.EventTarget.class);
            if (annotation == null) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) continue;
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) params[0];
            Handler handler = new Handler(listener, method, annotation.priority());
            handlerMap.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(handler);
            Collections.sort(handlerMap.get(eventClass), Comparator.comparingInt(h -> h.priority.ordinal()));
        }
    }

    public void unregister(Object listener) {
        for (List<Handler> handlers : handlerMap.values()) {
            handlers.removeIf(h -> h.instance == listener);
        }
    }

    public <T extends Event> T post(T event) {
        List<Handler> handlers = handlerMap.get(event.getClass());
        if (handlers != null) {
            for (Handler handler : handlers) {
                try {
                    handler.method.invoke(handler.instance, event);
                } catch (Exception ignored) {
                }
            }
        }
        return event;
    }

    public void clear() {
        handlerMap.clear();
    }

    private static class Handler {
        final Object instance;
        final Method method;
        final Event.Priority priority;

        Handler(Object instance, Method method, Event.Priority priority) {
            this.instance = instance;
            this.method = method;
            this.priority = priority;
        }
    }
}
