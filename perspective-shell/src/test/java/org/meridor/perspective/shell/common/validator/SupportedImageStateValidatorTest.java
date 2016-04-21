package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.shell.common.validator.annotation.SupportedImageState;
import org.meridor.perspective.shell.common.validator.annotation.SupportedInstanceState;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SupportedImageStateValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new SupportedImageStateValidator();
        Object image = new Object();
        SupportedImageState annotation = new SupportedImageState() {

            @Override
            public ImageState[] value() {
                return new ImageState[]{ImageState.SAVED, ImageState.DELETING};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return SupportedInstanceState.class;
            }

        };
        assertThat(validator.validate(image, annotation, null), is(true));
        assertThat(validator.validate(image, annotation, ImageState.SAVED.value()), is(true));
        assertThat(validator.validate(image, annotation, ImageState.DELETING.value()), is(true));
        assertThat(validator.validate(image, annotation, ImageState.ERROR.value()), is(false));

    }
}