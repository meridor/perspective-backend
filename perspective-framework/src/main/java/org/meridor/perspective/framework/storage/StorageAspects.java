package org.meridor.perspective.framework.storage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.meridor.perspective.framework.messaging.IfNotLocked;
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
        String lockName = annotation.lockName();
        if (lockName.isEmpty()) {
            lockName = joinPoint.getSignature().getDeclaringType().getCanonicalName();
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