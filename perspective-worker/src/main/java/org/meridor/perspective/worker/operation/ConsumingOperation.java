package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;

import java.util.function.Supplier;

public interface ConsumingOperation<T> extends Operation {

    boolean perform(Cloud cloud, Supplier<T> supplier);

}
