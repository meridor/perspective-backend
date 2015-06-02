package org.meridor.perspective.rest.storage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

@Component
@Aspect
@Configurable
public class StorageAspects implements ApplicationListener<ContextClosedEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(StorageAspects.class);
    
    @Autowired
    private Storage storage;
    
    private boolean isApplicationRunning = true;

    @Around("@within(org.meridor.perspective.rest.storage.IfNotLocked) || execution(@org.meridor.perspective.rest.storage.IfNotLocked * *(..))")
    public Object ifNotLocked(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        if (!isApplicationRunning) {
            LOG.debug("Skipping method {} execution because application is stopping", method);
            return null;
        }
        
        IfNotLocked annotation = method.getAnnotation(IfNotLocked.class);
        String lockName = annotation.lockName();
        if (lockName.isEmpty()) {
            lockName = joinPoint.getSignature().getDeclaringType().getCanonicalName();
        }
        long timeout = annotation.timeout();
        
        LOG.debug("Trying to obtain lock {}", lockName);
        Lock lock = storage.getLock(lockName);
        if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
            try {
                return joinPoint.proceed();
            } finally {
                LOG.debug("Releasing the lock {}", lockName);
                lock.unlock();
            }
        } else {
            LOG.debug("Failed to obtain lock {}. Will do nothing.", lockName);
            return null;
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        isApplicationRunning = false;
    }
}