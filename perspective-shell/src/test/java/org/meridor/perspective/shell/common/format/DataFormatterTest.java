package org.meridor.perspective.shell.common.format;

import org.junit.Test;
import org.meridor.perspective.shell.common.format.impl.BlockFormatter;
import org.meridor.perspective.shell.common.format.impl.EnumerationFormatter;
import org.meridor.perspective.shell.common.format.impl.ListFormatter;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Row;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DataFormatterTest {

    @Test
    public void testListFormatter() {
        ListFormatter formatter = new ListFormatter();
        assertThat(formatter.format(getData()), equalTo("11\n12\nnull\n22"));
    }

    @Test
    public void testEnumerationFormatter() {
        EnumerationFormatter formatter = new EnumerationFormatter();
        assertThat(formatter.format(getData()), equalTo("11,null\n12,22"));
    }

    @Test
    public void testBlockFormatter() {
        BlockFormatter formatter = new BlockFormatter();
        assertThat(formatter.format(getData()), equalTo(
                "********************\n" +
                        "One: 11\n" +
                        "Two: 12\n" +
                        "********************\n" +
                        "One: null\n" +
                        "Two: 22\n"
        ));
    }

    private static Data getData() {
        Data data = new Data();
        data.setColumnNames(Arrays.asList("one", "two"));
        data.setRows(Arrays.asList(
                new Row() {
                    {
                        getValues().addAll(Arrays.asList("11", "12"));
                    }
                },
                new Row() {
                    {
                        getValues().addAll(Arrays.asList(null, "22"));
                    }
                }
        ));
        return data;
    }

}