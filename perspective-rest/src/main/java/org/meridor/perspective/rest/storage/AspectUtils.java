package org.meridor.perspective.rest.storage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public final class AspectUtils {

    public static Annotation getMethodAnnotation(ProceedingJoinPoint joinPoint, Class annotationClass) throws Exception {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return AnnotationUtils.getAnnotation(method, annotationClass);
    }

    public static <T> T getAnnotationParameter(Annotation annotation, String parameterName, Predicate<T> predicate, T defaultValue) throws Exception {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Optional<Method> annotationMethod = Arrays.stream(annotationType.getDeclaredMethods())
                .filter(m -> m.getName().equals(parameterName))
                .findFirst();
        if (annotationMethod.isPresent()){
            @SuppressWarnings("unchecked")
            T parameterValueCandidate =  (T) annotationMethod.get().invoke(annotation);
            if (predicate.test(parameterValueCandidate)) {
                return parameterValueCandidate;
            }
        }
        return defaultValue;
    }
    
    private AspectUtils() {
    }
}
