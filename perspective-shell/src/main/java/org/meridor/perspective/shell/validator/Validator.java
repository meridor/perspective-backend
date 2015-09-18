package org.meridor.perspective.shell.validator;

import java.lang.annotation.Annotation;

public interface Validator {
    
    boolean validate(Annotation annotation, Object value);

    Class<? extends Annotation> getAnnotationClass();
    
    String getMessage(Annotation annotation, String fieldName, Object value);
    
}
