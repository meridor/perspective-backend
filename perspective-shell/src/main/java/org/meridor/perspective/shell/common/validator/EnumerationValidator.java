package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.shell.common.repository.impl.TextUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;

public abstract class EnumerationValidator implements Validator {
    
    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        List<String> values = getValues(annotation);
        return (value == null) || values.contains(value.toString());
    }
    
    protected abstract List<String> getValues(Annotation annotation);
    
    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        String correctValues = TextUtils.enumerateValues(new HashSet<>(getValues(annotation)));
        return String.format("Invalid \"%s\" value: %s. Should be one of: [%s].", fieldName, value, correctValues);
    }
    
}
