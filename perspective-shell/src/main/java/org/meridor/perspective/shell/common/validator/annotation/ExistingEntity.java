package org.meridor.perspective.shell.common.validator.annotation;

import org.meridor.perspective.shell.common.validator.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExistingEntity {
    
    Entity value();
    
    int maxCount() default 1;
    
    int minCount() default 1;
    
    String projectField() default "";
    
}
