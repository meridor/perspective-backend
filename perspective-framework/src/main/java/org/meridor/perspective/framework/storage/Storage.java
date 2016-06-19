package org.meridor.perspective.framework.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Storage {

    boolean isAvailable();

    <T> BlockingQueue<T> getQueue(String id);

    Lock getLock(String name);
    
    <T> T executeSynchronized(String lockName, long timeout, Supplier<T> action);

    <K> Set<K> getMapKeys(String mapId);
    
    <K, T> void modifyMap(String mapId, K key, Consumer<Map<K, T>> action);

    <K, I, O> O readFromMap(String mapId, K key, Function<Map<K, I>, O> function);

}
