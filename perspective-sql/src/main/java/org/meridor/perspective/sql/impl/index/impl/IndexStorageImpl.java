package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Component
public class IndexStorageImpl implements IndexStorage {

    private final Map<IndexSignature, Index> indexes = new ConcurrentHashMap<>();

    @Override
    public Set<IndexSignature> getSignatures() {
        return indexes.keySet();
    }

    @Override
    public Optional<Index> get(IndexSignature indexSignature) {
        //This emulates storage like Hazelcast returning read-only copies of objects
        Index index = indexes.get(indexSignature);
        return index != null ?
                Optional.of(cloneIndex(index)) :
                Optional.empty();
    }

    private static Index cloneIndex(Index index) {
        HashTableIndex ret = new HashTableIndex(index.getSignature(), index.getKeyLength());
        index.getKeys().forEach(
                key -> index.get(key).forEach(id -> ret.put(key, id))
        );
        return ret;
    }
    
    @Override
    public void update(IndexSignature indexSignature, UnaryOperator<Index> action) {
        indexes.put(indexSignature, action.apply(indexes.get(indexSignature)));
    }

    @Override
    public void create(IndexSignature indexSignature, int keyLength) {
        update(indexSignature, any -> new HashTableIndex(indexSignature, keyLength));
    }
}
