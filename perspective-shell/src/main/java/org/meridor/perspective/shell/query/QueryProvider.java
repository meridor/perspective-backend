package org.meridor.perspective.shell.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public final class QueryProvider {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    public <T extends Query> T get(Class<T> cls) {
        return applicationContext.getBean(cls);
    }
    
}
