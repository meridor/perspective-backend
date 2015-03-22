package org.meridor.perspective.agent.impl;

import org.meridor.perspective.agent.Operation;
import org.meridor.perspective.agent.OperationType;
import org.meridor.perspective.agent.OperationsRegistry;
import org.meridor.stecker.PluginLoader;
import org.meridor.stecker.PluginRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OperationsRegistryImpl implements OperationsRegistry {
    
    private final Map<OperationType, Operation<?>> operationsMap = new HashMap<>();
    
    public OperationsRegistryImpl() throws Exception {
        init();
    }
    
    private void init() throws Exception {
        Path pluginsPath = Paths.get("plugins");
        PluginRegistry pluginRegistry = PluginLoader
                .withPluginDirectory(pluginsPath)
                .withExtensionPoints(Operation.class)
                .load();
        List<Class> operationClasses = pluginRegistry.getImplementations(Operation.class);
        for (Class operationClass : operationClasses) {
            Operation<?> operation = (Operation<?>) operationClass.newInstance();
            operationsMap.put(operation.getType(), operation);
        }
    }
    
    @Override
    public <T> Optional<Operation<T>> getOperation(OperationType operationType) {
        @SuppressWarnings("unchecked")
        Operation<T> operationCandidate = (Operation<T>) operationsMap.get(operationType);
        return Optional.ofNullable(operationCandidate);
    }
}
