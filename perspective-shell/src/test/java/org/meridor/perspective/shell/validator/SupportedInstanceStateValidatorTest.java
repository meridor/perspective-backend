package org.meridor.perspective.shell.validator;

import org.junit.Test;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.shell.validator.annotation.SupportedInstanceState;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SupportedInstanceStateValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new SupportedInstanceStateValidator();
        Object instance = new Object();
        SupportedInstanceState annotation = new SupportedInstanceState() {

            @Override
            public InstanceState[] value() {
                return new InstanceState[]{InstanceState.LAUNCHED, InstanceState.DELETING};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return SupportedInstanceState.class;
            }

        };
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, InstanceState.LAUNCHED.value()), is(true));
        assertThat(validator.validate(instance, annotation, InstanceState.DELETING.value()), is(true));
        assertThat(validator.validate(instance, annotation, InstanceState.ERROR.value()), is(false));

    }
}