package org.meridor.perspective.rest.aspects;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.concurrent.locks.Lock;

import static org.meridor.perspective.rest.aspects.AspectUtils.getAnnotationParameter;
import static org.meridor.perspective.rest.aspects.AspectUtils.getMethodAnnotation;

@Component
@Aspect
@Configurable
public class LockAspect {
    
    private static final Logger LOG = LoggerFactory.getLogger(LockAspect.class);
    
    private static final String LOCK_NAME = "lockName"; 
    private static final String LOCK_TIMEOUT = "timeout"; 
    
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Around("@within(org.meridor.perspective.rest.aspects.IfNotLocked) || execution(@org.meridor.perspective.rest.aspects.IfNotLocked * *(..))")
    public Object ifNotLocked(ProceedingJoinPoint joinPoint) throws Throwable {
        String lockName = getLockName(joinPoint, IfNotLocked.class);
        Lock lock = hazelcastInstance.getLock(lockName);
        LOG.debug("Trying to obtain lock {}", lockName);
        if (lock.tryLock()) {
            try {
                return joinPoint.proceed();
            } finally {
                lock.unlock();
            }
        } else {
            LOG.debug("Failed to obtain lock {}. Will do nothing.", lockName);
            return null;
        }
    }

    @Around("@within(org.meridor.perspective.rest.aspects.WaitForLock) || execution(@org.meridor.perspective.rest.aspects.WaitForLock * *(..))")
    public Object waitForLock(ProceedingJoinPoint joinPoint) throws Throwable {
        String lockName = getLockName(joinPoint, WaitForLock.class);
        ILock lock = hazelcastInstance.getLock(lockName);
        if (lock.isLocked()) {
            long timeout = getLockTimeout(joinPoint, WaitForLock.class);
            LOG.debug("Lock {} is locked. Waiting for {} milliseconds for it to be released.", lockName, timeout);
            if (timeout > 0) {
                lock.wait(timeout);
            } else {
                lock.wait();
            }
        }
        try {
            LOG.debug("Lock {} is now released. Doing the work...", lockName);
            lock.lock();
            return joinPoint.proceed();
        } finally {
            lock.unlock();
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