package org.meridor.perspective.sql.impl.index;

import java.io.Serializable;

public interface Key extends Serializable {
    
    String value();
    
    int length();
    
}
