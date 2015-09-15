package org.meridor.perspective.shell.repository.query.validator;

import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public abstract class RangeValidator implements Validator {
    
    @Override
    public boolean validate(Annotation annotation, Object value) {
        List<String> values = getRange(annotation);
        return (value != null) && values.contains(value.toString());
    }
    
    protected abstract List<String> getRange(Annotation annotation);
    
    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        String correctValues = TextUtils.enumerateValues(new HashSet<>(getRange(annotation)));
        return String.format("Invalid %s value: %s. Should be one of [%s]", fieldName, value, correctValues);
    }
    
}
