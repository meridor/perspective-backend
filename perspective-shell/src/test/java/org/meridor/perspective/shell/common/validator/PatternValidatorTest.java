package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.shell.common.validator.annotation.Pattern;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class PatternValidatorTest {

    @Parameterized.Parameters(name = "{0} should return {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"test", true},
                {"%test%", true},
                {"^test$", true},
                {"\\d+", true},
                {"test{", false},
        });
    }

    private final String pattern;
    private final boolean result;

    public PatternValidatorTest(String pattern, boolean result) {
        this.pattern = pattern;
        this.result = result;
    }

    @Test
    public void testValidate() {
        PatternValidator patternValidator = new PatternValidator();
        Object instance = new Object();
        Pattern annotation = new Pattern() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Pattern.class;
            }
        };
        assertThat(patternValidator.validate(instance, annotation, pattern), is(result));
    }
}