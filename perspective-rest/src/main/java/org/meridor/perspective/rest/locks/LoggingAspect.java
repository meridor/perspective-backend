package org.meridor.perspective.rest.locks;

import com.hazelcast.core.HazelcastInstance;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

@Component
@Aspect
@Configurable
public class LoggingAspect {
    
    private static final Logger LOG = LoggerFactory.getLogger(LoggingAspect.class);
    
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Around("@within(org.meridor.perspective.rest.locks.Locked) || @annotation(org.meridor.perspective.rest.locks.Locked)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<String> lockNameCandidate = getLockName(joinPoint);
        String lockName = lockNameCandidate.isPresent() && !lockNameCandidate.get().isEmpty() ? 
                lockNameCandidate.get() 
                : joinPoint.getSignature().getDeclaringType().getCanonicalName();
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
    
    private Optional<String> getLockName(ProceedingJoinPoint joinPoint) throws Exception {
        Class cls = joinPoint.getSignature().getDeclaringType();
        Annotation annotation = cls.getAnnotation(Locked.class);
        Class<? extends Annotation> annotationType = annotation.annotationType();
        for (Method method : annotationType.getDeclaredMethods()) {
            if (method.getName().equals("name")) {
                return Optional.ofNullable(method.invoke(annotation).toString());
            }
        }
        return Optional.empty();
    }
}