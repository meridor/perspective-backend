package org.meridor.perspective.shell.common.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/date-utils-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DateUtilsTest {

    @Autowired
    private DateUtils dateUtils;

    @Test
    public void testFormatDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 2, 1, 10, 5, 20, 0, ZoneId.systemDefault());
        String formattedDate = dateUtils.formatDate(zonedDateTime);
        assertThat(formattedDate, equalTo("20160201_100520"));
    }

}