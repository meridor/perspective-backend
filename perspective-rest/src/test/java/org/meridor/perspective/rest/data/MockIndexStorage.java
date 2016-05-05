package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class MockIndexStorage implements IndexStorage {

    private Map<String, Index> indexes = new HashMap<>();
    
    @Override
    public Optional<Index> get(IndexSignature indexSignature) {
        return Optional.ofNullable(indexes.get(indexSignature.getValue()));
    }

    @Override
    public void put(IndexSignature indexSignature, Index index) {
        indexes.put(indexSignature.getValue(), index);
    }


}
