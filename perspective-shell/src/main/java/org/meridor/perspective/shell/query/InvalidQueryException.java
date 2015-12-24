package org.meridor.perspective.shell.query;

import java.util.Set;

public class InvalidQueryException extends RuntimeException {
    
    private final Set<String> errors;

    public InvalidQueryException(Set<String> errors) {
        this.errors = errors;
    }

    public Set<String> getErrors() {
        return errors;
    }
}
