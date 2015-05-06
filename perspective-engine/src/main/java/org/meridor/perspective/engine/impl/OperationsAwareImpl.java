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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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
            OperationId operationId = getOperationId(bean);
            operationInstances.put(operationId, bean);

            Map<OperationId, Method> operationMethods = getOperationMethods(bean);
            this.operationMethods.putAll(operationMethods);
            LOG.debug(
                    "Added operation class {} with cloud type = {}, operation type = {} having {} operation methods",
                    bean.getClass().getCanonicalName(),
                    operationId.getCloudType(),
                    operationId.getOperationType(),
                    operationMethods.size()
            );
        });
    }

    private OperationId getOperationId(Object bean) {
        Operation operation = bean.getClass().getAnnotation(Operation.class);
        return getOperationId(operation.cloud(), operation.type());
    }

    private OperationId getOperationId(CloudType cloudType, OperationType operationType) {
        return new OperationId(cloudType, operationType);
    }
    
    private Map<OperationId, Method> getOperationMethods(Object bean) {
        return Arrays
                .stream(bean.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(EntryPoint.class) && m.getParameterCount() == 1)
                .collect(Collectors.toMap(
                        m -> getOperationId(bean),
                        Function.identity()
                ));
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

            if (cloudType != that.cloudType) return false;
            return operationType == that.operationType;

        }

        @Override
        public int hashCode() {
            return (cloudType.name() + operationType.name()).hashCode();
        }
    }

}
