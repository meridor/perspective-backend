package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;

import java.util.function.Consumer;

public interface SupplyingOperation<T> extends Operation {

    boolean perform(Cloud cloud, Consumer<T> consumer);

}
