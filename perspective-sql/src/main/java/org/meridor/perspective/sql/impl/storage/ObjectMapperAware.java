package org.meridor.perspective.sql.impl.storage;

public interface ObjectMapperAware {
    
    <T> ObjectMapper<T> get(Class<T> cls);
    
}
