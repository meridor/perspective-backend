package org.meridor.perspective.sql.impl.table.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ForeignKeys.class)
public @interface ForeignKey {
    

    /**
     * Current table column names included in key
     */
    String[] columns();

    /**
     * Foreign table this key references to
     */
    String table();

    /**
     * Columns of the foreign table
     */
    String[] tableColumns();
    
    int length() default 0;
    
}
