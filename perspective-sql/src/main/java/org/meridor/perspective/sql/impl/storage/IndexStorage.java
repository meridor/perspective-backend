package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.util.Optional;
import java.util.Set;

public interface IndexStorage {
    
    Set<IndexSignature> getSignatures();
    
    Optional<Index> get(IndexSignature indexSignature);
    
    //This does not overwrite index if exists 
    void put(IndexSignature indexSignature, Index index);
    
}
