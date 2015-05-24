package org.meridor.perspective.rest.storage;

import org.meridor.perspective.beans.DestinationName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Consume {

    DestinationName queueName() default DestinationName.UNDEFINED;
    
    int numConsumers() default 1; 
    
}
