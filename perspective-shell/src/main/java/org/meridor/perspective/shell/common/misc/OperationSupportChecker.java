package org.meridor.perspective.shell.common.misc;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

import java.util.Collection;
import java.util.function.Function;

public interface OperationSupportChecker {

    boolean isOperationSupported(CloudType cloudType, OperationType operationType);

    <T> Collection<T> filter(Collection<T> input, Function<T, CloudType> cloudTypeProvider, OperationType operationType);

}
