package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.shell.common.validator.annotation.SupportedSymbols;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class SupportedSymbolsValidator implements Validator {

    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        return value == null || value.toString().matches(String.format("[%s]+", getAllowedSymbols(annotation)));
    }

    private String getAllowedSymbols(Annotation annotation) {
        SupportedSymbols ann = SupportedSymbols.class.cast(annotation);
        return ann.value();
    }
    
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return SupportedSymbols.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        return String.format("%s should contain only one of the following symbols: %s", fieldName, getAllowedSymbols(annotation));
    }

}
