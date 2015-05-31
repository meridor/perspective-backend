package org.meridor.perspective.rest.workers;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.meridor.perspective.framework.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@Configurable
public class ProcessingAspects {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingAspects.class);
    
    @Around("@within(org.meridor.perspective.framework.EntryPoint) || execution(@org.meridor.perspective.framework.EntryPoint * *(..))")
    public Object ifNotLocked(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class returnType = signature.getReturnType();
        Method method = signature.getMethod();
        EntryPoint annotation = method.getAnnotation(EntryPoint.class);
        final int maxAttempts = annotation.maxAttempts();
        final long delayBetweenAttempts = annotation.delayBetweenAttempts();
        int attemptNumber = 1;
        
        while (attemptNumber <= maxAttempts) {
            try {
                LOG.debug("Attempt {}: executing method {} of {}", method, joinPoint.getTarget());
                Object ret = joinPoint.proceed();
                if (Boolean.class.isAssignableFrom(returnType) && (Boolean) ret) {
                    return true;
                }
            } catch (Exception e) {
                LOG.error("An exception while executing method " + method, e);
            }
            if (delayBetweenAttempts > 0) {
                Thread.sleep(delayBetweenAttempts);
            }
            attemptNumber++;
        }
        return false;
    }
    
}