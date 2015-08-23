package org.meridor.perspective.worker.operation.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.meridor.perspective.worker.operation.Operation;
import org.meridor.perspective.worker.operation.OperationsAware;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class OperationsAwareImpl implements OperationsAware {

    private static final Logger LOG = LoggerFactory.getLogger(OperationsAwareImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<OperationType, Operation> operationInstances = new HashMap<>();

    @PostConstruct
    private void init() {
        Map<String, Operation> operationBeans = applicationContext.getBeansOfType(Operation.class);
        operationBeans.values().stream().forEach(bean -> {
            final Operation realBean = getRealBean(bean);
            Arrays.stream(realBean.getTypes()).forEach(t -> {
                operationInstances.put(t, realBean);
                LOG.debug(
                        "Added operation class {} with operation type = {}",
                        realBean.getClass().getCanonicalName(),
                        t
                );
            });

        });
    }

    private Operation getRealBean(Operation bean) {
        if (AopUtils.isAopProxy(bean)) {
            Advised advisedBean = (Advised) bean;
            try {
                return (Operation) advisedBean.getTargetSource().getTarget();
            } catch (Exception e) {
                LOG.debug("Failed to process AOP proxied bean {}", bean);
            }
        }
        return bean;
    }

    private Operation getOperation(OperationType operationType) {
        Optional<Operation> operation = Optional.ofNullable(operationInstances.get(operationType));
        if (!operation.isPresent()) {
            throw new IllegalArgumentException(String.format("Operation %s is not supported. This is probably a bug.", operationType));
        }
        return operation.get();
    }

    @Override
    public boolean isOperationSupported(OperationType operationType) {
        return operationInstances.containsKey(operationType);
    }

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception {
        Operation operation = getOperation(operationType);
        if (operation instanceof SupplyingOperation) {
            @SuppressWarnings("unchecked")
            boolean result = ((SupplyingOperation<T>) operation).perform(cloud, consumer);
            return result;
        } else {
            LOG.error("Operation {} should be a supplying operation", operationType);
            return false;
        }
    }

    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception {
        Operation operation = getOperation(operationType);
        if (operation instanceof ConsumingOperation) {
            @SuppressWarnings("unchecked")
            boolean result = ((ConsumingOperation<T>) operation).perform(cloud, supplier);
            return result;
        } else {
            LOG.error("Operation {} should be a consuming operation", operationType);
            return false;
        }
    }

}
