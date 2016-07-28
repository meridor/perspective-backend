package org.meridor.perspective.rest.data;

import org.meridor.perspective.framework.storage.Storage;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.meridor.perspective.framework.storage.impl.StorageKey.indexes;

@Component
public class IndexStorageImpl implements IndexStorage {
    
    @Autowired
    private Storage storage;

    @Override
    public Set<IndexSignature> getSignatures() {
        return storage.getMapKeys(indexes());
    }

    @Override
    public Optional<Index> get(IndexSignature indexSignature) {
        return readIndex(indexSignature, map -> Optional.ofNullable(map.get(indexSignature)));
    }

    private <T> T readIndex(IndexSignature indexSignature, Function<Map<IndexSignature, Index>, T> function) {
        return storage.readFromMap(indexes(), indexSignature, function);
    }
    
    @Override
    public void update(IndexSignature indexSignature, UnaryOperator<Index> action) {
        storage.<IndexSignature, Index>modifyMap(indexes(), indexSignature, map -> map.put(
                indexSignature,
                action.apply(map.get(indexSignature))
        ));
    }
}
