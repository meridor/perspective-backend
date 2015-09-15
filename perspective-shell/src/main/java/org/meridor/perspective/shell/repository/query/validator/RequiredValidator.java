package org.meridor.perspective.shell.repository.query.validator;

import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class RequiredValidator implements Validator {
    
    @Override
    public boolean validate(Annotation annotation, Object value) {
        return value != null;
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Required.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        return String.format("Field %s is required", fieldName);
    }
}
