package org.meridor.perspective.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SerializationUtils {
    
    public static String serialize(Object bean) throws IOException {
        ObjectMapper objectMapper = createDefaultMapper();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, bean);
        return new String(outputStream.toByteArray());
    }
    
    public static <T> T unserialize(String text, Class<T> cls) throws IOException {
        ObjectMapper objectMapper = createDefaultMapper();
        JavaType bodyType = objectMapper.getTypeFactory().constructType(cls);
        return objectMapper.readValue(text.getBytes(), bodyType);
    }

    public static ObjectMapper createDefaultMapper() {
        final ObjectMapper result = new ObjectMapper();
        result.configure(SerializationFeature.INDENT_OUTPUT, true);
        result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        result.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        result.setAnnotationIntrospector(AnnotationIntrospector.pair(
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()),
                new JacksonAnnotationIntrospector()
        ));
        return result;
    }
}
