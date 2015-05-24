package org.meridor.perspective.framework;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Operation {

    CloudType cloud();
    
    OperationType[] type();
    
}
