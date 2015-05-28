package org.meridor.perspective.rest.storage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
@Aspect
@Configurable
public class StorageAspects {
    
    private static final Logger LOG = LoggerFactory.getLogger(StorageAspects.class);
    
    @Autowired
    private Storage storage;

    @Around("@within(org.meridor.perspective.rest.storage.IfNotLocked) || execution(@org.meridor.perspective.rest.storage.IfNotLocked * *(..))")
    public Object ifNotLocked(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        if (storage == null || !storage.isAvailable()) {
            LOG.debug("Skipping method {} execution because storage is not available", method);
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
    
}