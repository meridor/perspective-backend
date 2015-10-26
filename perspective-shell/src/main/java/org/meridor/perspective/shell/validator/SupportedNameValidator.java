package org.meridor.perspective.shell.validator;

import org.meridor.perspective.shell.validator.annotation.SupportedName;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class SupportedNameValidator implements Validator {

    private static final String ALLOWED_SYMBOLS = "a-zA-Z0-9-_$";
    
    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        return value != null && value.toString().matches(String.format("[%s]+", ALLOWED_SYMBOLS));
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return SupportedName.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        return String.format("%s should contain only one of the following symbols: %s", fieldName, ALLOWED_SYMBOLS);
    }

}
