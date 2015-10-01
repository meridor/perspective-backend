package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;

import java.util.function.Supplier;

public interface ProcessingOperation<I, O> extends Operation {
    
    O perform(Cloud cloud, Supplier<I> supplier);
    
}
