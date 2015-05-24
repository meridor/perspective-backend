package org.meridor.perspective.rest.storage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.meridor.perspective.rest.storage.AspectUtils.getAnnotationParameter;
import static org.meridor.perspective.rest.storage.AspectUtils.getMethodAnnotation;

@Component
@Aspect
@Configurable
public class StorageAspects {
    
    private static final Logger LOG = LoggerFactory.getLogger(StorageAspects.class);
    
    private static final String LOCK_NAME = "lockName"; 
    private static final String LOCK_TIMEOUT = "timeout"; 
    
    @Autowired
    private Storage storage;

    @Around("@within(org.meridor.perspective.rest.storage.IfNotLocked) || execution(@org.meridor.perspective.rest.storage.IfNotLocked * *(..))")
    public Object ifNotLocked(ProceedingJoinPoint joinPoint) throws Throwable {
        String lockName = getLockName(joinPoint, IfNotLocked.class);
        long timeout = getLockTimeout(joinPoint, IfNotLocked.class);
        Optional<Lock> lock = storage.getLock(lockName);
        LOG.debug("Trying to obtain lock {} within {} milliseconds", lockName, timeout);
        if (lock.isPresent() && lock.get().tryLock(timeout, TimeUnit.MILLISECONDS)) {
            try {
                return joinPoint.proceed();
            } finally {
                LOG.debug("Releasing the lock {}", lockName);
                lock.get().unlock();
            }
        } else {
            LOG.debug("Failed to obtain lock {}. Will do nothing.", lockName);
            return null;
        }
    }

    private String getLockName(ProceedingJoinPoint joinPoint, Class annotationClass) throws Exception {
        Annotation annotation = getMethodAnnotation(joinPoint, annotationClass);
        return getAnnotationParameter(
                annotation,
                LOCK_NAME,
                t -> !t.isEmpty(),
                joinPoint.getSignature().getDeclaringType().getCanonicalName()
        );
    }
    
    private long getLockTimeout(ProceedingJoinPoint joinPoint, Class annotationClass) throws Exception {
        Annotation annotation = getMethodAnnotation(joinPoint, annotationClass);
        return getAnnotationParameter(
                annotation,
                LOCK_TIMEOUT,
                v -> true,
                0l
        );
   }
    
}