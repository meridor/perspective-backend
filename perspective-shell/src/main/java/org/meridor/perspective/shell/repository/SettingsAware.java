package org.meridor.perspective.shell.repository;

import org.meridor.perspective.shell.repository.query.validator.Setting;

import java.util.Map;
import java.util.Set;

public interface SettingsAware {
    
    boolean hasSetting(Setting setting);

    void setSetting(Setting setting, Set<String> value);

    void unsetSetting(Setting setting);

    Set<String> getSetting(Setting setting);
    
    Map<String, String> getSettings();

}
