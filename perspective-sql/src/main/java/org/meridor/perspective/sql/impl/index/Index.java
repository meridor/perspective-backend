package org.meridor.perspective.sql.impl.index;

import java.io.Serializable;
import java.util.Set;

public interface Index extends Serializable {

    Set<String> get(Key key);

    void put(Key key, String id);

    void remove(Key key, String id);
    
    int getKeyLength();
    
}
