package org.meridor.perspective.sql.impl.index;

import java.io.Serializable;
import java.util.List;

public interface Key extends Serializable {
    
    List<Object> getValues();
    
    int length();
    
}
