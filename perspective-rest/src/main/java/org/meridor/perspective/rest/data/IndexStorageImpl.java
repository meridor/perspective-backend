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
        String signatureString = indexSignature.getValue();
        return readIndex(signatureString, map -> Optional.ofNullable(map.get(signatureString)));
    }

    private <T> T readIndex(String indexSignature, Function<Map<String, Index>, T> function) {
        return storage.readFromMap(indexes(), indexSignature, function);
    }

    @Override
    public void put(IndexSignature indexSignature, Index index) {
        String signatureString = indexSignature.getValue();
        storage.modifyMap(indexes(), signatureString, map -> map.putIfAbsent(signatureString, index));
    }
}
