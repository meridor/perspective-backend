package org.meridor.perspective.shell.validator;

import org.junit.Test;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SupportedCloudValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new SupportedCloudValidator();
        Object instance = new Object();
        SupportedCloud annotation = new SupportedCloud() {

            @Override
            public CloudType[] value() {
                return new CloudType[]{CloudType.MOCK, CloudType.DOCKER};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return SupportedCloud.class;
            }

        };
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, CloudType.MOCK.value()), is(true));
        assertThat(validator.validate(instance, annotation, CloudType.DOCKER.value()), is(true));
        assertThat(validator.validate(instance, annotation, CloudType.OPENSTACK.value()), is(false));

    }
}