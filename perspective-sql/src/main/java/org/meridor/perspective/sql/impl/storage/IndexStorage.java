package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

public interface IndexStorage {
    
    Set<IndexSignature> getSignatures();
    
    Optional<Index> get(IndexSignature indexSignature);
    
    void update(IndexSignature indexSignature, UnaryOperator<Index> action);

    void create(IndexSignature indexSignature, int keyLength);
    
}
