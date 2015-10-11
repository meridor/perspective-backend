package org.meridor.perspective.shell.validator;

import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.validator.annotation.Metadata;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MetadataValidator implements Validator {
    
    private static final List<String> ALL_METADATA_KEYS =
            Arrays.asList(MetadataKey.values())
            .stream()
            .map(k -> k.name().toLowerCase())
            .collect(Collectors.toList());
    
    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) { 
            return true;
        }
        if (value instanceof Map) {
            Map<?, ?> valuesMap = (Map) value;
            for (Object k : valuesMap.keySet()) {
                if (!ALL_METADATA_KEYS.contains(k.toString())) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Metadata.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        String correctValues = TextUtils.enumerateValues(ALL_METADATA_KEYS);
        return String.format("%s should be one of %s: %s given", fieldName, correctValues, value.toString());
    }
    
}
