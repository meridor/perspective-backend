package org.meridor.perspective.framework.storage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

public interface Storage {

    boolean isAvailable();

    <T> BlockingQueue<T> getQueue(String id);

    Lock getLock(String name);

}
