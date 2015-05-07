package org.meridor.perspective.engine.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationsAware;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OperationsAwareImpl implements OperationsAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(OperationsAwareImpl.class);
    
    @Autowired
    private ApplicationContext applicationContext;

    private final Map<OperationId, Object> operationInstances = new HashMap<>();
    
    private final Map<OperationId, Method> operationMethods = new HashMap<>();

    @PostConstruct
    private void init() {
        Map<String, Object> operationBeans = applicationContext.getBeansWithAnnotation(Operation.class);
        operationBeans.values().stream().forEach(bean -> {
            List<OperationId> operationId = getOperationIds(bean);
            operationId.stream().forEach(id -> {
                Optional<Method> operationMethod = getOperationMethod(bean);
                if (operationMethod.isPresent()) {
                    operationInstances.put(id, bean);
                    this.operationMethods.put(id, operationMethod.get());
                    LOG.debug(
                            "Added operation class {} with cloud type = {} and operation type = {}",
                            bean.getClass().getCanonicalName(),
                            id.getCloudType(),
                            id.getOperationType()
                    );
                } else {
                    LOG.warn("Skipping operation class {} because it contains no method marked as entry point.");
                }
            });

        });
    }

    private List<OperationId> getOperationIds(Object bean) {
        Operation operation = bean.getClass().getAnnotation(Operation.class);
        return Arrays.stream(operation.type())
                .map(t -> getOperationId(operation.cloud(), t))
                .collect(Collectors.toList());
    }

    private OperationId getOperationId(CloudType cloudType, OperationType operationType) {
        return new OperationId(cloudType, operationType);
    }
    
    private Optional<Method> getOperationMethod(Object bean) {
        return Arrays
                .stream(bean.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(EntryPoint.class) && m.getParameterCount() == 1)
                .findFirst();
    }

    @Override
    public boolean isOperationSupported(CloudType cloudType, OperationType operationType) {
        OperationId operationId = getOperationId(cloudType, operationType);
        return operationMethods.containsKey(operationId);
    }

    @Override
    public void act(CloudType cloudType, OperationType operationType, Object dataContainer) throws Exception {
        OperationId operationId = getOperationId(cloudType, operationType);
        Object operationInstance = operationInstances.get(operationId);
        Method method = operationMethods.get(operationId);
        Class<?> parameterClass = method.getParameters()[0].getType();
        Class<?> dataContainerClass = dataContainer.getClass();
        if (!parameterClass.isAssignableFrom(dataContainerClass)) {
            throw new IllegalStateException(String.format(
                    "Data container with class %s can not be substitued as parameter %s",
                    parameterClass.getClass().getCanonicalName(),
                    dataContainerClass.getClass().getCanonicalName()
            ));
        }
        method.invoke(operationInstance, dataContainer);
    }

    private static class OperationId {
        private final CloudType cloudType;
        private final OperationType operationType;

        public OperationId(CloudType cloudType, OperationType operationType) {
            this.cloudType = cloudType;
            this.operationType = operationType;
        }

        public CloudType getCloudType() {
            return cloudType;
        }

        public OperationType getOperationType() {
            return operationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OperationId that = (OperationId) o;

            return cloudType == that.cloudType && operationType == that.operationType;

        }

        @Override
        public int hashCode() {
            return (cloudType.name() + operationType.name()).hashCode();
        }
    }

}
