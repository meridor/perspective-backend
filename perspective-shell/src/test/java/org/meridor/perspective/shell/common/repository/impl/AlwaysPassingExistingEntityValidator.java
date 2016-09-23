package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.shell.common.validator.Validator;
import org.meridor.perspective.shell.common.validator.annotation.ExistingEntity;

import java.lang.annotation.Annotation;

public class AlwaysPassingExistingEntityValidator implements Validator {

    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        return true;
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return ExistingEntity.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        throw new UnsupportedOperationException("This method should never be called");
    }

}
