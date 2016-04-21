package org.meridor.perspective.shell.common.misc;

import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtils {
    
    @Autowired
    private SettingsAware settingsAware;
    
    public String formatDate(ZonedDateTime date) {
        return getDateTimeFormatter(getFormatPattern()).format(date);
    }
    
    public DateTimeFormatter getDateTimeFormatter(String pattern) {
        try {
            return DateTimeFormatter.ofPattern(pattern);
        } catch (Exception e) {
            return getDateTimeFormatter(getDefaultFormatPattern());
        }
    }

    private String getFormatPattern() {
        if (settingsAware.hasSetting(Setting.DATE_FORMAT)) {
            return settingsAware.getSettingAs(Setting.DATE_FORMAT, String.class);
        }
        return getDefaultFormatPattern();
    }
    
    private String getDefaultFormatPattern() {
        return "YYYYMMdd_HHmmss";
    }
    
}
