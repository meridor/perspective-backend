package org.meridor.perspective.shell.repository.query.validator;

import java.lang.annotation.Annotation;

public interface Validator {
    
    boolean validate(Object value);
    
    Class<? extends Annotation> getAnnotation();
    
    String getMessage(String fieldName, Object value);
    
}
