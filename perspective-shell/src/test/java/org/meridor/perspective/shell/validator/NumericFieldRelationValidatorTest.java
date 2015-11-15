package org.meridor.perspective.shell.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.validator.annotation.RelativeToNumericField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/validator-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class NumericFieldRelationValidatorTest {

    @Autowired
    private NumericFieldRelationValidator validator;
    
    @Test
    public void testValidateDoubleField() throws Exception {
        TestObject instance = new TestObject();
        RelativeToNumericField doubleFieldAnnotation = new RelativeToNumericField() {

            @Override
            public NumberRelation relation() {
                return NumberRelation.GREATER_THAN;
            }

            @Override
            public String field() {
                return "doubleField";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RelativeToNumericField.class;
            }

        };
        assertThat(validator.validate(instance, doubleFieldAnnotation, null), is(true));
        assertThat(validator.validate(instance, doubleFieldAnnotation, 1), is(true));
        assertThat(validator.validate(instance, doubleFieldAnnotation, -1), is(false));

    }
    
    @Test
    public void testValidateStringField() {
        TestObject instance = new TestObject();
        RelativeToNumericField stringFieldAnnotation = new RelativeToNumericField() {

            @Override
            public NumberRelation relation() {
                return NumberRelation.GREATER_THAN;
            }

            @Override
            public String field() {
                return "stringField";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RelativeToNumericField.class;
            }

        };
        assertThat(validator.validate(instance, stringFieldAnnotation, "anything"), is(false));
    }
    
    @Test
    public void testValidateMissingField() {
        TestObject instance = new TestObject();
        RelativeToNumericField missingFieldAnnotation = new RelativeToNumericField() {

            @Override
            public NumberRelation relation() {
                return NumberRelation.GREATER_THAN;
            }

            @Override
            public String field() {
                return "missingField";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RelativeToNumericField.class;
            }

        };
        assertThat(validator.validate(instance, missingFieldAnnotation, "anything"), is(false));
    }
    
    private static class TestObject {
        
        private String stringField = "anything";
        
        private double doubleField = 0;
        
    }
}