package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;

import java.io.Serializable;
import java.util.*;

public class HashTableIndex implements Index, Serializable {
    
    private final Map<String, Set<Integer>> index = new HashMap<>();
    private final int keyLength;

    public HashTableIndex(int keyLength) {
        this.keyLength = keyLength;
    }

    @Override
    public void put(Key key, int rowNumber) {
        if (getKeyLength() > 0 && key.length() != getKeyLength()) {
            throw new IllegalArgumentException(String.format("This index accepts keys with length = %d", keyLength));
        }
        String keyValue = key.value();
        index.putIfAbsent(keyValue, new HashSet<>());
        index.get(keyValue).add(rowNumber);
    }

    @Override
    public Set<Integer> get(Key key) {
        String keyValue = key.value();
        return index.containsKey(keyValue) ?
                index.get(keyValue) :
                Collections.emptySet();
    }

    @Override
    public int getKeyLength() {
        return keyLength;
    }
}
