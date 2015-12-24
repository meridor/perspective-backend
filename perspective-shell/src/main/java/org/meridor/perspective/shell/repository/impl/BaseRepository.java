package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.query.InvalidQueryException;
import org.meridor.perspective.shell.query.Query;
import org.meridor.perspective.shell.validator.ObjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public abstract class BaseRepository {
    
    @Autowired
    private ObjectValidator objectValidator;
    
    protected void validateQuery(Query<?> query) {
        Set<String> validationErrors = objectValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            throw new InvalidQueryException(validationErrors);
        }
    }
    
}
