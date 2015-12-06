package org.meridor.perspective.framework.storage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public interface Storage {

    boolean isAvailable();

    <T> BlockingQueue<T> getQueue(String id);

    Lock getLock(String name);
    
    <T> T executeSynchronized(String lockName, long timeout, Supplier<T> action);
    
    long DEFAULT_LOCK_WAIT_TIMEOUT = 1000;

}
