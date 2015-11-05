package org.meridor.perspective.shell.validator;

import org.meridor.perspective.shell.validator.annotation.NumericRange;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseRange;

@Component
public class NumericRangeValidator implements Validator {

    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) {
            return true;
        }
        try {
            Set<Integer> values = parseRange(value.toString());
            return values.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return NumericRange.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        return String.format("%s should be a numeric range: %s given", fieldName, value.toString());
    }

}
