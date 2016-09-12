package org.meridor.perspective.shell.common.validator.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.validator.Field;
import org.meridor.perspective.shell.common.validator.FilterProcessor;
import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.validator.impl.ObjectValidatorImplTest.getFieldValue;

@ContextConfiguration(locations = "/META-INF/spring/validator-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class FilterProcessorImplTest {

    @Autowired
    private FilterProcessor filterProcessor;

    @Test
    public void testAll() throws Exception {
        ObjectWithFilter object = new ObjectWithFilter();
        assertThat(filterProcessor.hasAppliedFilters(object), is(false));
        ObjectWithFilter objectWithFilter = filterProcessor.applyFilters(object);
        assertThat(filterProcessor.hasAppliedFilters(objectWithFilter), is(true));

        String[] fieldNames = {"fieldOne", "fieldTwo"};

        for (String fieldName : fieldNames) {
            Object fieldValue = getFieldValue(objectWithFilter, fieldName);
            assertThat(fieldValue, is(notNullValue()));
            assertThat(fieldValue, is(instanceOf(String.class)));
            assertThat(String.valueOf(fieldValue), equalTo("one, two"));
        }

        ObjectWithFilter objectWithNoFilter = filterProcessor.unsetFilters(objectWithFilter);
        for (String fieldName : fieldNames) {
            Object fieldValue = getFieldValue(objectWithNoFilter, fieldName);
            assertThat(fieldValue, is(nullValue()));
        }
    }

    private static class ObjectWithFilter {

        @Filter(Field.INSTANCE_NAMES)
        private String fieldOne;

        @Filter(Field.IMAGE_NAMES)
        private String fieldTwo;

    }

}