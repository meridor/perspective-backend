package org.meridor.perspective.shell.validator;

import org.junit.Test;
import org.meridor.perspective.shell.validator.annotation.SupportedName;

import java.lang.annotation.Annotation;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SupportedNameValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new SupportedNameValidator();
        SupportedName annotation = new SupportedName(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return SupportedName.class;
            }
        };
        Object instance = new Object();
        assertThat(validator.validate(instance, annotation, "Cor_rect-$Name"), is(true));
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, "%incorrect#name"), is(false));
    }
}