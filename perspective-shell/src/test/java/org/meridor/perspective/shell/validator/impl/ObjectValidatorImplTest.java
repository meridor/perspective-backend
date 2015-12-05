package org.meridor.perspective.shell.validator.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.NumberRelation;
import org.meridor.perspective.shell.validator.ObjectValidator;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.RelativeToNumber;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/validator-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ObjectValidatorImplTest {

    @Autowired
    private ObjectValidator objectValidator;
    
    @Test
    public void testValidObject() throws Exception {
        ObjectToValidate validObject = new ObjectToValidate();
        validObject.setRequiredField("something"); //Required field is explicitly set
        assertThat(objectValidator.validate(validObject), is(empty()));
    }
    
    @Test
    public void testInvalidObject() throws Exception {
        ObjectToValidate validObject = new ObjectToValidate();
        assertThat(objectValidator.validate(validObject), hasSize(1));
    }
    @Test
    public void testValidObjectWithFilter() throws Exception {
        ObjectToValidateWithFilter validObjectWithFilter = new ObjectToValidateWithFilter();
        assertThat(objectValidator.validate(validObjectWithFilter), is(empty()));
    }
    
    @Test
    public void testFilterButNoValidator() throws Exception {
        ObjectWithFilterOnly validObjectWithFilterOnly = new ObjectWithFilterOnly();
        java.lang.reflect.Field fieldWithFilter = validObjectWithFilterOnly.getClass()
                .getDeclaredField("fieldWithFilter");
        fieldWithFilter.setAccessible(true);
        assertThat(objectValidator.validate(validObjectWithFilterOnly), is(empty()));
        assertThat(fieldWithFilter.get(validObjectWithFilterOnly), notNullValue());
    }

    @Test
    public void testObjectWithSetField() throws Exception {
        ObjectToValidateWithSet invalidObject = new ObjectToValidateWithSet();
        assertThat(objectValidator.validate(invalidObject), hasSize(1));
    }
    
    private static class ObjectToValidate {
        
        @Required
        private String requiredField;

        public void setRequiredField(String requiredField) {
            this.requiredField = requiredField;
        }
        
    }
    
    private static class ObjectToValidateWithFilter {
        
        @Required
        @Filter(Field.INSTANCE_NAMES)
        private String requiredField; //Injected from TestRepository class
        
    }
    
    private static class ObjectWithFilterOnly {
        
        @Filter(Field.INSTANCE_NAMES)
        private String fieldWithFilter; //No validation annotation here but value should be assigned
        
    }
    
    private static class ObjectToValidateWithSet {
        
        @RelativeToNumber(number = 0, relation = NumberRelation.GREATER_THAN_EQUAL)
        private Set<Integer> fieldContainingSet = Stream.of(1, 0, -1).collect(Collectors.toSet());
        
    }
    
}