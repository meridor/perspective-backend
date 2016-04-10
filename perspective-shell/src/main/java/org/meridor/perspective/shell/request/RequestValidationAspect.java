package org.meridor.perspective.shell.request;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.meridor.perspective.shell.validator.ObjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Aspect
@Configurable
public class RequestValidationAspect {
    
    @Autowired
    private ObjectValidator objectValidator;

    @Around("execution(public * org.meridor.perspective.shell.request.Request+.getPayload(..))")
    public Object getPayload(ProceedingJoinPoint joinPoint) throws Throwable {
        Object request = joinPoint.getTarget();
        Set<String> validationErrors = objectValidator.validate(request);
        if (!validationErrors.isEmpty()) {
            throw new InvalidRequestException(validationErrors);
        }
        return joinPoint.proceed();
    }


}
