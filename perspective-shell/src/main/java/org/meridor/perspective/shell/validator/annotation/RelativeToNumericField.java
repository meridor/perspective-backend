package org.meridor.perspective.shell.validator.annotation;

import org.meridor.perspective.shell.validator.NumberRelation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RelativeToNumericField {
    
    NumberRelation relation() default NumberRelation.EQUAL;
    
    String field();
    
}
