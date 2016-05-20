package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.ObjectMapperAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class ObjectMapperAwareImpl implements ObjectMapperAware {
    
    private final Map<Class, ObjectMapper> storage = new HashMap<>();
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        for (ObjectMapper objectMapper : applicationContext.getBeansOfType(ObjectMapper.class).values()) {
            storage.put(objectMapper.getInputClass(), objectMapper);
        }
    }
    
    @Override
    public <T> ObjectMapper<T> get(Class<T> cls) {
        if (!storage.containsKey(cls)) {
            throw new IllegalArgumentException(String.format("Object mapper for bean class \"%s\" not found", cls.getCanonicalName()));
        }
        @SuppressWarnings("unchecked")
        ObjectMapper<T> objectMapper = (ObjectMapper<T>) storage.get(cls);
        return objectMapper;
    }
    
}
