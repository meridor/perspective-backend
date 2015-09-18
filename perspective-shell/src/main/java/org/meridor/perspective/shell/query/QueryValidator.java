package org.meridor.perspective.shell.query;

import java.util.Set;

public interface QueryValidator {
    
    Set<String> validate(Query<?> query);
    
}
