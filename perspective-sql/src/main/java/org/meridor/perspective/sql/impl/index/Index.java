package org.meridor.perspective.sql.impl.index;

import java.io.Serializable;
import java.util.Set;

public interface Index extends Serializable {
    
    void put(Key key, String id);
    
    Set<String> get(Key key);
    
    int getKeyLength();
    
}
