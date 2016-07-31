package org.meridor.perspective.rest.data;

import org.meridor.perspective.framework.storage.Storage;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.HashTableIndex;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import static org.meridor.perspective.framework.storage.impl.StorageKey.indexes;

@Component
public class IndexStorageImpl implements IndexStorage {
    
    @Autowired
    private Storage storage;

    private Set<IndexSignature> signatures = new LinkedHashSet<>();
    
    @Override
    public Set<IndexSignature> getSignatures() {
        return signatures;
    }

    @Override
    public Optional<Index> get(IndexSignature indexSignature) {
        String signatureString = indexSignature.toString();
        return storage.<String, Index, Optional<Index>>readFromMap(
                indexes(),
                signatureString,
                map -> Optional.ofNullable(map.get(signatureString))
        );
    }

    @Override
    public void update(IndexSignature indexSignature, UnaryOperator<Index> action) {
        String signatureString = indexSignature.toString();
        storage.<String, Index>modifyMap(indexes(), signatureString, map -> map.put(
                signatureString,
                action.apply(map.get(signatureString))
        ));
    }

    @Override
    public void create(IndexSignature indexSignature, int keyLength) {
        if (!get(indexSignature).isPresent()) {
            update(indexSignature, any -> new HashTableIndex(indexSignature, keyLength));
        }
        signatures.add(indexSignature);
    }
}
