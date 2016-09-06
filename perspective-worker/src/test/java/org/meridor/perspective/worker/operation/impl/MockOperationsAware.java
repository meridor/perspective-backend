package org.meridor.perspective.worker.operation.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.DELETE_INSTANCE;

public class MockOperationsAware implements OperationsAware {
    
    public static final String TEST_STRING = "test";
    
    @Override
    public Optional<Operation> getOperation(OperationType operationType) {
        switch (operationType) {
            case LIST_INSTANCES: return Optional.of(new SupplyingOperation<String>() {
                @Override
                public boolean perform(Cloud cloud, Consumer<String> consumer) {
                    consumer.accept(TEST_STRING);
                    return true;
                }

                @Override
                public boolean perform(Cloud cloud, Set<String> ids, Consumer<String> consumer) {
                    return perform(cloud, consumer);
                }

                @Override
                public OperationType[] getTypes() {
                    return new OperationType[]{OperationType.LIST_INSTANCES};
                }
            });
            case ADD_INSTANCE: return Optional.of(new ConsumingOperation<String>() {

                @Override
                public boolean perform(Cloud cloud, Supplier<String> supplier) {
                    return true;
                }

                @Override
                public OperationType[] getTypes() {
                    return new OperationType[]{OperationType.ADD_INSTANCE};
                }
            });
            
            case DELETE_INSTANCE: return Optional.of(new ProcessingOperation<String, String>() {

                @Override
                public String perform(Cloud cloud, Supplier<String> supplier) {
                    return supplier.get();
                }

                @Override
                public OperationType[] getTypes() {
                    return new OperationType[]{DELETE_INSTANCE};
                }
            });
            default: return Optional.empty();
        }
    }
    
}
