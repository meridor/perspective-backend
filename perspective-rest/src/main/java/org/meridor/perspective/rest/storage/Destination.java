package org.meridor.perspective.rest.storage;

import org.meridor.perspective.beans.DestinationName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Destination {
    
    DestinationName name();
    
}
