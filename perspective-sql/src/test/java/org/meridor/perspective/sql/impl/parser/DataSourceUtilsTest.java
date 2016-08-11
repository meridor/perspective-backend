package org.meridor.perspective.sql.impl.parser;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.parser.DataSourceUtils.crossProduct;

public class DataSourceUtilsTest {
    
    @Test
    public void testCrossProductEmpty() {
        assertThat(crossProduct(null), is(empty()));
        assertThat(crossProduct(Collections.emptyList()), is(empty()));
    }
    
    @Test
    public void testCrossProductSingle() {
        List<Collection<String>> input = Collections.singletonList(Arrays.asList("1", "2"));
        List<List<String>> output = crossProduct(input);
        assertThat(output, equalTo(Arrays.asList(Collections.singletonList("1"), Collections.singletonList("2"))));
    }
    
    @Test
    public void testCrossProductMultiple() {
        List<List<String>> output = crossProduct(Arrays.asList(
                Arrays.asList("1", "2"),
                Arrays.asList("3", "4"),
                Arrays.asList("5", "6")
        ));
        assertThat(output, hasSize(8));
        assertThat(output, contains(
                Arrays.asList("1", "3", "5"),
                Arrays.asList("1", "4", "5"),
                Arrays.asList("1", "3", "6"),
                Arrays.asList("1", "4", "6"),
                Arrays.asList("2", "3", "5"),
                Arrays.asList("2", "4", "5"),
                Arrays.asList("2", "3", "6"),
                Arrays.asList("2", "4", "6")
        ));
    }

    @Test
    public void testCrossProductWithConsumer() {

        List<List<String>> output = new ArrayList<>();
        crossProduct(
                Arrays.asList("1", "2"),
                Arrays.asList("1", "4"),
                Collections::singletonList,
                (i, row) -> output.add(row)
        );
        assertThat(output, hasSize(4));
        assertThat(output, contains(
                Arrays.asList("1", "1"),
                Arrays.asList("2", "1"),
                Arrays.asList("1", "4"),
                Arrays.asList("2", "4")
        ));
    }

}