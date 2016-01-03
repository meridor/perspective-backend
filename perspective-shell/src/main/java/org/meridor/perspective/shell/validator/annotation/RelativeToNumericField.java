package org.meridor.perspective.shell.validator.annotation;

import org.meridor.perspective.beans.BooleanRelation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RelativeToNumericField {
    
    BooleanRelation relation() default BooleanRelation.EQUAL;
    
    String field();
    
}
