package org.meridor.perspective.sql.impl.index;

import org.meridor.perspective.sql.impl.index.impl.KeyImpl;

public final class Keys {
    
    public static Key create(int length, Object...parts) {
        return new KeyImpl(length, parts);
    }
    
    private Keys() {}
}
