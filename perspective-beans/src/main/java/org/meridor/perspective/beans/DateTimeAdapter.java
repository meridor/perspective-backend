package org.meridor.perspective.beans;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeAdapter {

    public static ZonedDateTime parseDate(String dateTimeString) {
        return ZonedDateTime.parse(dateTimeString);
    }

    public static String printDate(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

}
