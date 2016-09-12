package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.meridor.perspective.shell.common.validator.annotation.SupportedSymbols;

import java.lang.annotation.Annotation;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SupportedSymbolsValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Validator validator = new SupportedSymbolsValidator();
        SupportedSymbols annotation = new SupportedSymbols() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return SupportedSymbols.class;
            }

            @Override
            public String value() {
                return "a-zA-Z0-9-_$.";
            }
        };
        Object instance = new Object();
        assertThat(validator.validate(instance, annotation, "Cor_rect-$Name."), is(true));
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, "%incorrect#name"), is(false));
    }
}