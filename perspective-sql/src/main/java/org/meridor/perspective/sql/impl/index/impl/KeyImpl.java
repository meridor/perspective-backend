package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Key;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeyImpl implements Key {
    
    private final int length;
    
    private final List<Object> values;

    public KeyImpl(int length, Object... values) {
        this.length = length;
        this.values = Arrays.asList(values);
    }
    
    private String toString(Object... values) {
        return Arrays.stream(values)
                .map(
                        p -> {
                            String str = String.valueOf(p);
                            return length > 0 && str.length() >= length ?
                                    str.substring(0, length - 1) :
                                    str;
                        }
                )
                .collect(Collectors.joining());

    }

    @Override
    public List<Object> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return toString(values);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Key && toString().equals(obj.toString());
    }
}
