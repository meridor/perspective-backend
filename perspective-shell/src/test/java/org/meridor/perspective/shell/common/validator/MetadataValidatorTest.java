package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.shell.common.validator.annotation.Metadata;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MetadataValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new MetadataValidator();
        Object instance = new Object();
        Metadata annotation = new Metadata() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Metadata.class;
            }

        };
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, "something"), is(false));
        assertThat(validator.validate(instance, annotation, getCorrectMetadataMap()), is(true));
        assertThat(validator.validate(instance, annotation, getIncorrectMetadataMap()), is(false));

    }
    
    private static Map<String, Set<String>> getCorrectMetadataMap() {
        return new HashMap<String, Set<String>>(){
            {
                put(MetadataKey.ID.value(), Collections.singleton("anything"));
            }
        };
    }
    
    private static Map<String, Set<String>> getIncorrectMetadataMap() {
        return new HashMap<String, Set<String>>(){
            {
                put("not-metadata", Collections.singleton("anything"));
            }
        };
    }
    
}