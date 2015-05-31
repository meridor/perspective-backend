package org.meridor.perspective.engine.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationsAware;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
            final Object realBean = getRealBean(bean);
            List<OperationId> operationId = getOperationIds(realBean);
            operationId.stream().forEach(id -> {
                Optional<Method> operationMethod = getOperationMethod(realBean);
                if (operationMethod.isPresent()) {
                    operationInstances.put(id, realBean);
                    this.operationMethods.put(id, operationMethod.get());
                    LOG.debug(
                            "Added operation class {} with cloud type = {} and operation type = {}",
                            realBean.getClass().getCanonicalName(),
                            id.getCloudType(),
                            id.getOperationType()
                    );
                } else {
                    LOG.warn("Skipping operation class {} because it contains no method marked as entry point.");
                }
            });

        });
    }
    
    private Object getRealBean(Object bean) {
        if (AopUtils.isAopProxy(bean)) {
            Advised advisedBean = (Advised) bean;
            try {
                return advisedBean.getTargetSource().getTarget();
            } catch (Exception e) {
                LOG.debug("Failed to process AOP proxied bean {}", bean);
            }
        }
        return bean;
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
    public <T> boolean consume(CloudType cloudType, OperationType operationType, Consumer<T> consumer) throws Exception {
        return doAct(cloudType, operationType, consumer);
    }
    
    @Override
    public <T> boolean supply(CloudType cloudType, OperationType operationType, Supplier<T> supplier) throws Exception {
        return doAct(cloudType, operationType, supplier);
    }

    private boolean doAct(CloudType cloudType, OperationType operationType, Object consumerOrSupplier) throws Exception {
        OperationId operationId = getOperationId(cloudType, operationType);
        Object operationInstance = operationInstances.get(operationId);
        Method method = operationMethods.get(operationId);
        Class<?> parameterClass = method.getParameters()[0].getType();
        if (!Consumer.class.isAssignableFrom(parameterClass) && !Supplier.class.isAssignableFrom(parameterClass)) {
            throw new UnsupportedOperationException(String.format(
                    "Operation class entry point should have one parameter of Consumer<T> or Supplier<T> type. However for cloud %s operation %s this type is %s",
                    cloudType,
                    operationType,
                    parameterClass.getClass().getCanonicalName()
            ));
        }

        Class<?> booleanClass = ClassUtils.forName("boolean", null);
        if (method.getReturnType().equals(booleanClass)) {
            return (boolean) method.invoke(operationInstance, consumerOrSupplier);
        }
        method.invoke(operationInstance, consumerOrSupplier);
        return true;
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
