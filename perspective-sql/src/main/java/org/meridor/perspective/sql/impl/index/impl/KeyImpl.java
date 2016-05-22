package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Key;

import java.util.Arrays;
import java.util.stream.Collectors;

public class KeyImpl implements Key {
    
    private final int length;
    
    private final String value;

    public KeyImpl(int length, Object...parts) {
        this.length = length;
        this.value = createValue(parts);
    }
    
    private String createValue(Object...parts) {
        return Arrays.asList(parts).stream()
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
    public String value() {
        return value;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Key && value.equals(((Key) obj).value());
    }
}
