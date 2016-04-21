package org.meridor.perspective.shell.common.request;

import java.util.Set;

public class InvalidRequestException extends RuntimeException {
    
    private final Set<String> errors;

    public InvalidRequestException(Set<String> errors) {
        this.errors = errors;
    }

    public Set<String> getErrors() {
        return errors;
    }
}
