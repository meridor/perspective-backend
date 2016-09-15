package org.meridor.perspective.shell.common.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/date-utils-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DateUtilsTest {

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private SettingsAware settingsAware;
    
    @Test
    public void testFormatDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 2, 1, 10, 5, 20, 0, ZoneId.systemDefault());
        assertThat(dateUtils.formatDate(zonedDateTime), equalTo("20160201_100520"));
        settingsAware.setSetting(Setting.DATE_FORMAT, Collections.singleton("YYYYMMdd"));
        assertThat(dateUtils.formatDate(zonedDateTime), equalTo("20160201"));
    }
    
    @Test
    public void testInvalidDateTimePattern() {
        DateTimeFormatter invalidPatternDateTimeFormatter = dateUtils.getDateTimeFormatter("invalid");
        DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss");
        ZonedDateTime dateTime = ZonedDateTime.now();
        assertThat(invalidPatternDateTimeFormatter.format(dateTime), equalTo(defaultDateTimeFormatter.format(dateTime)));
    }

}