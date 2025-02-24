package org.tiny.mq.common.cache;

import org.tiny.mq.common.remote.SyncFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NameServerSyncFutureManager {
    private static Map<String, SyncFuture> manager = new ConcurrentHashMap<>();


    public static void put(String key, SyncFuture syncFuture) {
        manager.put(key, syncFuture);
    }

    public static SyncFuture get(String key) {
        return manager.get(key);
    }

    public static void remove(String msgId) {
        manager.remove(msgId);
    }
}
