package org.meridor.perspective.sql.impl.index;

import org.meridor.perspective.sql.impl.index.impl.KeyImpl;

public final class Keys {
    
    public static Key key(int length, Object...parts) {
        return new KeyImpl(length, parts);
    }
    
    public static Key key(Object value) {
        return new KeyImpl(0, value);
    }
    
    private Keys() {}
}
