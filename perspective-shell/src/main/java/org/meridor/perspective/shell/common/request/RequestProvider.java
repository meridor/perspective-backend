package org.meridor.perspective.shell.common.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public final class RequestProvider {
    
    private final ApplicationContext applicationContext;

    @Autowired
    public RequestProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T extends Request> T get(Class<T> cls) {
        return applicationContext.getBean(cls);
    }
    
}
