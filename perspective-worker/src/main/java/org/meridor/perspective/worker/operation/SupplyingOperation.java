package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;

import java.util.Set;
import java.util.function.Consumer;

public interface SupplyingOperation<T> extends Operation {

    boolean perform(Cloud cloud, Consumer<T> consumer);

    //Here ids are internal entity ids, not real ones. Need to determine real ids.
    boolean perform(Cloud cloud, Set<String> ids, Consumer<T> consumer);

}
