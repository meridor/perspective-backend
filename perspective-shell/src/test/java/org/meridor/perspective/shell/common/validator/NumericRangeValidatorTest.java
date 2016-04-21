package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.meridor.perspective.shell.common.validator.annotation.NumericRange;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NumericRangeValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new NumericRangeValidator();
        Object instance = new Object();
        NumericRange annotation = new NumericRange() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return NumericRange.class;
            }

        };
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, "1,3-5,10,100"), is(true));
        assertThat(validator.validate(instance, annotation, "1"), is(true));
        assertThat(validator.validate(instance, annotation, "#$-3"), is(false));

    }
}