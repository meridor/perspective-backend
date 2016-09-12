package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.meridor.perspective.shell.common.validator.annotation.Required;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RequiredValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new RequiredValidator();
        Object instance = new Object();
        Required annotation = new Required() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

        };
        assertThat(validator.validate(instance, annotation, null), is(false));
        assertThat(validator.validate(instance, annotation, "something"), is(true));
    }
}