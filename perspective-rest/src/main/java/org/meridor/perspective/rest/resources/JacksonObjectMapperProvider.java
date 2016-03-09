package org.meridor.perspective.rest.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.meridor.perspective.api.ObjectMapperFactory;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return ObjectMapperFactory.createDefaultMapper();
    }
}