package org.meridor.perspective.shell.common.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SupportedSymbols {
    
    String value() default "a-zA-Z0-9-_$.";
    
}
