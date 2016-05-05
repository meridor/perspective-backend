package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Key;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeyImpl implements Key {
    
    private final int length;
    
    private final List<Object> parts;

    public KeyImpl(int length, Object...parts) {
        this.length = length;
        this.parts = Arrays.asList(parts);
    }
    
    @Override
    public String value() {
        return parts.stream()
                .map(
                        p -> {
                            String str = p.toString();
                            return length > 0 && str.length() >= length ?
                                    str.substring(0, length - 1) :
                                    str;
                        }
                )
                .collect(Collectors.joining());
    }

    @Override
    public int length() {
        return length;
    }
}
