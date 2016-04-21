package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.shell.common.validator.annotation.NumericRange;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isRange;

@Component
public class NumericRangeValidator implements Validator {

    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        return value == null || isRange(value.toString());
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
