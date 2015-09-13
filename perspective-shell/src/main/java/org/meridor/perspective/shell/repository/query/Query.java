package org.meridor.perspective.shell.repository.query;

import java.util.Set;

public interface Query<T> {
    
    T getPayload();
    
    Set<String> validate();
    
}
