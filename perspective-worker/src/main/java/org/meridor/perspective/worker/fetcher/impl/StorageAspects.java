package org.meridor.perspective.worker.fetcher.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.meridor.perspective.framework.messaging.IfNotLocked;
import org.meridor.perspective.framework.storage.Storage;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@Configurable
public class StorageAspects implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StorageAspects.class);

    @Autowired
    private Storage storage;
    
    @Autowired
    private WorkerMetadata workerMetadata;

    private boolean isApplicationRunning = true;

    @Around("@within(org.meridor.perspective.framework.messaging.IfNotLocked) || execution(@org.meridor.perspective.framework.messaging.IfNotLocked * *(..))")
    public Object ifNotLocked(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (!isApplicationRunning) {
            LOG.trace("Skipping method {} execution because application is stopping", method);
            return null;
        }

        IfNotLocked annotation = method.getAnnotation(IfNotLocked.class);
        String className = joinPoint.getSignature().getDeclaringType().getCanonicalName();
        String cloudType = workerMetadata.getCloudType().value();
        String lockName = annotation.lockName().isEmpty() ?
                String.format("%s_%s", cloudType, className) :
                String.format("%s_%s_%s", cloudType, className, annotation.lockName());
        if (!annotation.lockName().isEmpty()) {
            lockName += "_" + annotation.lockName();
        }
        long timeout = annotation.timeout();
        
        return storage.executeSynchronized(lockName, timeout, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                LOG.error(String.format("Exception in locked method = %s", method.getName()), throwable);
                return null;
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        isApplicationRunning = false;
    }
}