package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HashTableIndex implements Index {
    
    private final Map<Key, Set<String>> index = new ConcurrentHashMap<>();
    private final IndexSignature signature;
    private final int keyLength;

    public HashTableIndex(IndexSignature signature, int keyLength) {
        this.signature = signature;
        this.keyLength = keyLength;
    }
    
    public HashTableIndex(IndexSignature signature) {
        this(signature, 0);
    }

    @Override
    public void put(Key key, String id) {
        checkKeyLength(key);
        index.compute(key, (k, oldIds) -> new LinkedHashSet<String>(){
            {
                if (oldIds != null) {
                    addAll(oldIds);
                }
                add(id);
            }
        });
    }

    private void checkKeyLength(Key key) {
        if (getKeyLength() > 0 && key.length() != getKeyLength()) {
            throw new IllegalArgumentException(String.format("This index accepts keys with length = %d", keyLength));
        }
    }

    @Override
    public void delete(Key key, String id) {
        checkKeyLength(key);
        index.computeIfPresent(key, (k, ids) -> {
            ids.remove(id);
            return ids;
        });
    }

    @Override
    public IndexSignature getSignature() {
        return signature;
    }

    @Override
    public Set<Key> getKeys() {
        return index.keySet();
    }

    @Override
    public Set<String> getIds() {
        return index.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> get(Key key) {
        return index.containsKey(key) ?
                new LinkedHashSet<>(index.get(key)) :
                Collections.emptySet();
    }

    @Override
    public int getKeyLength() {
        return keyLength;
    }
}
