package org.meridor.perspective.shell.common.validator.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.shell.common.validator.Field;
import org.meridor.perspective.shell.common.validator.ObjectValidator;
import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Name;
import org.meridor.perspective.shell.common.validator.annotation.RelativeToNumber;
import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
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
        List<String> errors = new ArrayList<>(objectValidator.validate(validObject));
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0).contains("customName"), is(true));
    }

    @Test
    public void testValidObjectWithFilter() throws Exception {
        ObjectToValidateWithFilter validObjectWithFilter = new ObjectToValidateWithFilter();
        assertThat(objectValidator.validate(validObjectWithFilter), is(empty()));
    }

    @Test
    public void testFilterButNoValidator() throws Exception {
        ObjectWithFilterOnly validObjectWithFilterOnly = new ObjectWithFilterOnly();
        assertThat(objectValidator.validate(validObjectWithFilterOnly), is(empty()));
        Object fieldValue = getFieldValue(validObjectWithFilterOnly, "fieldWithFilter");
        assertThat(fieldValue, notNullValue());
        assertThat(fieldValue, is(instanceOf(String.class)));
        assertThat(String.valueOf(fieldValue), equalTo("one, two"));
    }

    @Test
    public void testObjectWithSetField() throws Exception {
        ObjectToValidateWithSet invalidObject = new ObjectToValidateWithSet();
        assertThat(objectValidator.validate(invalidObject), hasSize(1));
    }

    public static Object getFieldValue(Object instance, String fieldName) throws Exception {
        java.lang.reflect.Field field = instance.getClass()
                .getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static class ObjectToValidate {

        @Name("customName")
        @Required
        private String requiredField;

        public void setRequiredField(String requiredField) {
            this.requiredField = requiredField;
        }

    }

    private static class ObjectToValidateWithFilter {

        @Required
        @Filter(Field.INSTANCE_NAMES)
        private Set<String> requiredField; //Injected from TestRepository class

    }

    private static class ObjectWithFilterOnly {

        @Filter(Field.INSTANCE_NAMES)
        private String fieldWithFilter; //No validation annotation here but value should be assigned

    }

    private static class ObjectToValidateWithSet {

        @RelativeToNumber(number = 0, relation = BooleanRelation.GREATER_THAN_EQUAL)
        private Set<Integer> fieldContainingSet = Stream.of(1, 0, -1).collect(Collectors.toSet());

    }

}