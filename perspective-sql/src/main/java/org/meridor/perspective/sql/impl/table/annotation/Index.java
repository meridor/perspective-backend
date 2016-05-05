package org.meridor.perspective.sql.impl.table.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Indexes.class)
public @interface Index {
    
    String[] columnNames();
    
    int length() default 0;
    
}
