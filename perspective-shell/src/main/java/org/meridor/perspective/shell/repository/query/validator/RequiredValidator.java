package org.meridor.perspective.shell.repository.query.validator;

import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class RequiredValidator implements Validator {
    
    @Override
    public boolean validate(Object value) {
        return value != null;
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return Required.class;
    }

    @Override
    public String getMessage(String fieldName, Object value) {
        return String.format("Field %s is required", fieldName);
    }
}
