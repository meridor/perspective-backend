package org.meridor.perspective.sql.impl.index;

import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.io.Serializable;
import java.util.Set;

public interface Index extends Serializable {

    IndexSignature getSignature();
    
    Set<Key> getKeys();
    
    Set<String> getIds();
    
    Set<String> get(Key key);

    void put(Key key, String id);

    void delete(Key key, String id);
    
    int getKeyLength();
    
}
