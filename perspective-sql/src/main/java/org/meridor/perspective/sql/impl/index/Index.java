package org.meridor.perspective.sql.impl.index;

import java.util.Set;

public interface Index {
    
    void put(Key key, int rowNumber);
    
    Set<Integer> get(Key key);
    
    int getKeyLength();
    
}
