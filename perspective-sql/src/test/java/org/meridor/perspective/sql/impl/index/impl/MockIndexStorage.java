package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class MockIndexStorage implements IndexStorage {

    private Map<IndexSignature, Index> indexes = new HashMap<>();

    @Override
    public Set<IndexSignature> getSignatures() {
        return indexes.keySet();
    }

    @Override
    public Optional<Index> get(IndexSignature indexSignature) {
        return Optional.ofNullable(indexes.get(indexSignature));
    }

    @Override
    public void put(IndexSignature indexSignature, Index index) {
        indexes.put(indexSignature, index);
    }


}
