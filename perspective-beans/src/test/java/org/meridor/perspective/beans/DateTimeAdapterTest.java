package org.meridor.perspective.beans;

import org.junit.Test;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.DateTimeAdapter.parseDate;
import static org.meridor.perspective.beans.DateTimeAdapter.printDate;

public class DateTimeAdapterTest {
    
    @Test
    public void testFormatAndParse() {
        ZonedDateTime now = ZonedDateTime.now();
        String dateString = printDate(now);
        assertThat(parseDate(dateString), equalTo(now));
    }

}