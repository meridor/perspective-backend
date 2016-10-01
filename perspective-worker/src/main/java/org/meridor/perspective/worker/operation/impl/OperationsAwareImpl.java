package org.meridor.perspective.worker.operation.impl;

import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.OperationsRegistry;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.meridor.perspective.worker.operation.Operation;
import org.meridor.perspective.worker.operation.OperationsAware;
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

@Component
public class OperationsAwareImpl implements OperationsAware {

    private static final Logger LOG = LoggerFactory.getLogger(OperationsAwareImpl.class);

    private final ApplicationContext applicationContext;
    
    private final OperationsRegistry operationsRegistry;
    
    private final WorkerMetadata workerMetadata;

    private final Map<OperationType, Operation> operationInstances = new HashMap<>();

    @Autowired
    public OperationsAwareImpl(ApplicationContext applicationContext, OperationsRegistry operationsRegistry, WorkerMetadata workerMetadata) {
        this.applicationContext = applicationContext;
        this.operationsRegistry = operationsRegistry;
        this.workerMetadata = workerMetadata;
    }

    @PostConstruct
    private void init() {
        Map<String, Operation> operationBeans = applicationContext.getBeansOfType(Operation.class);
        operationBeans.values().forEach(bean -> {
            final Operation realBean = getRealBean(bean);
            Arrays.stream(realBean.getTypes()).forEach(t -> {
                operationInstances.put(t, realBean);
                operationsRegistry.addOperation(workerMetadata.getCloudType(), t);
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

    @Override
    public Optional<Operation> getOperation(OperationType operationType) {
        return Optional.ofNullable(operationInstances.get(operationType));
    }

}
