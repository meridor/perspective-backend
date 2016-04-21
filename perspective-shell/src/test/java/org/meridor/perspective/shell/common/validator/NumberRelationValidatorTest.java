package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.shell.common.validator.annotation.RelativeToNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/validator-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class NumberRelationValidatorTest {

    @Autowired
    private NumberRelationValidator validator;
    
    @Test
    public void testValidate() throws Exception {
        Object instance = new Object();
        RelativeToNumber annotation = new RelativeToNumber() {

            @Override
            public BooleanRelation relation() {
                return BooleanRelation.GREATER_THAN;
            }

            @Override
            public double number() {
                return 0;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RelativeToNumber.class;
            }

        };
        assertThat(validator.validate(instance, annotation, null), is(true));
        assertThat(validator.validate(instance, annotation, 1), is(true));
        assertThat(validator.validate(instance, annotation, -1), is(false));
    }
    
}