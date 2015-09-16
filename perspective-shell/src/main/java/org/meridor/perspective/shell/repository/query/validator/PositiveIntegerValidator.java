package org.meridor.perspective.shell.repository.query.validator;

import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class PositiveIntegerValidator implements Validator {
    
    @Override
    public boolean validate(Annotation annotation, Object value) {
        return (value == null) ||
                (Integer.class.isAssignableFrom(value.getClass()) &&
                Integer.valueOf(value.toString()) > 0);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Positive.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        return String.format("%s should be positive integer: %s given", fieldName, value.toString());
    }

}
