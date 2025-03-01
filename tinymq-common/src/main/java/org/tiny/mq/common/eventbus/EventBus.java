package org.tiny.mq.common.eventbus;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.tiny.mq.common.utils.ReflectUtils;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBus {
    private static final Map<Class<? extends Event>, List<Listener>> eventListenerMap = new ConcurrentHashMap<>();
    private String taskName = "event-bus-task-";

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), r -> {
        Thread thread = new Thread(r);
        thread.setName(taskName + UUID.randomUUID());
        return thread;
    });


    public EventBus(String taskName) {
        this.taskName = taskName;
        this.init();
    }

    public void init() {
        ServiceLoader<Listener> serviceLoader = ServiceLoader.load(Listener.class);
        for (Listener listener : serviceLoader) {
            Class clazz = ReflectUtils.getInterfaceT(listener, 0);
            this.register(clazz, listener);
        }
        System.out.println(eventListenerMap);
    }

    public <E extends Event> void register(Class<? extends Event> clazz, Listener<E> listener) {
        List<Listener> listeners = eventListenerMap.get(clazz);
        if (CollectionUtils.isEmpty(listeners)) {
            eventListenerMap.put(clazz, Lists.newArrayList(listener));
        } else {
            listeners.add(listener);
            eventListenerMap.put(clazz, listeners);
        }
        System.out.println(listeners);

    }

    public void publish(Event event) {
        List<Listener> listeners = eventListenerMap.get(event.getClass());
        threadPoolExecutor.execute(() -> {
            try {
                for (Listener listener : listeners) {
                    listener.onReceive(event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
