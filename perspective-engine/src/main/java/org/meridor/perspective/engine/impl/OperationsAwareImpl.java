package org.meridor.perspective.engine.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.engine.OperationsAware;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.meridor.perspective.config.OperationType;
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
    
    private final Map<OperationMethodId, Method> operationMethods = new HashMap<>();

    @PostConstruct
    private void init() {
        Map<String, Object> operationBeans = applicationContext.getBeansWithAnnotation(Operation.class);
        operationBeans.values().stream().forEach(bean -> {
            OperationId operationId = getOperationId(bean);
            operationInstances.put(operationId, bean);

            Map<OperationMethodId, Method> operationMethods = getOperationMethods(bean);
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
    
    private OperationMethodId getOperationMethodId(Object bean, Class<?> parameterClass) {
        return getOperationMethodId(getOperationId(bean), parameterClass); 
    }
    
    private OperationMethodId getOperationMethodId(OperationId operationId, Class<?> parameterClass) {
        return new OperationMethodId(operationId, parameterClass);
    }

    private Map<OperationMethodId, Method> getOperationMethods(Object bean) {
        return Arrays
                .stream(bean.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(EntryPoint.class) && m.getParameterCount() == 1)
                .collect(Collectors.toMap(
                        m -> {
                            Class<?> parameterClass = m.getParameters()[0].getType();
                            return getOperationMethodId(bean, parameterClass);
                        },
                        Function.identity()
                ));
    }

    @Override
    public boolean isOperationSupported(CloudType cloudType, OperationType operationType, Object dataContainer) {
        OperationMethodId operationMethodId = getOperationMethodId(getOperationId(cloudType, operationType), dataContainer.getClass());
        return operationMethods.containsKey(operationMethodId);
    }

    @Override
    public void act(CloudType cloudType, OperationType operationType, Object dataContainer) throws Exception {
        OperationId operationId = getOperationId(cloudType, operationType);
        OperationMethodId operationMethodId = getOperationMethodId(operationId, dataContainer.getClass());
        Object operationInstance = operationInstances.get(operationId);
        Method method = operationMethods.get(operationMethodId);
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
    
    private static class OperationMethodId {
        private final OperationId operationId;
        private final Class<?> argumentClass;

        public OperationMethodId(OperationId operationId, Class<?> argumentClass) {
            this.operationId = operationId;
            this.argumentClass = argumentClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OperationMethodId that = (OperationMethodId) o;

            if (!operationId.equals(that.operationId)) return false;
            return argumentClass.equals(that.argumentClass);

        }

        @Override
        public int hashCode() {
            int result = operationId.hashCode();
            result = 31 * result + argumentClass.hashCode();
            return result;
        }
    } 
    
}
