package org.meridor.perspective.sql.impl.index;

import java.io.Serializable;
import java.util.Set;

public interface Index extends Serializable {

    Set<Serializable> get(Key key);

    void put(Key key, Serializable id);

    void delete(Key key, Serializable id);
    
    int getKeyLength();
    
}
