package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;

import java.util.*;

public class HashTableIndex implements Index {
    
    private final Map<Key, Set<String>> index = new HashMap<>();
    private final int keyLength;

    public HashTableIndex(int keyLength) {
        this.keyLength = keyLength;
    }

    @Override
    public void put(Key key, String id) {
        checkKeyLength(key);
        index.putIfAbsent(key, new LinkedHashSet<>());
        index.get(key).add(id);
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
    public Set<Key> getKeys() {
        return index.keySet();
    }

    @Override
    public Set<String> get(Key key) {
        return index.containsKey(key) ?
                index.get(key) :
                Collections.emptySet();
    }

    @Override
    public int getKeyLength() {
        return keyLength;
    }
}
