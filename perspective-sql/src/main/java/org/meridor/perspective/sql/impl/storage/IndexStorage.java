package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.util.Optional;

public interface IndexStorage {
    
    Optional<Index> get(IndexSignature indexSignature);
    
    void put(IndexSignature indexSignature, Index index);
    
}
