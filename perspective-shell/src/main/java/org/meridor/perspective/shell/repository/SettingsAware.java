package org.meridor.perspective.shell.repository;

import org.meridor.perspective.shell.validator.Setting;

import java.util.Map;
import java.util.Set;

public interface SettingsAware {
    
    boolean hasSetting(Setting setting);

    void setSetting(Setting setting, Set<String> value);

    void unsetSetting(Setting setting);

    Set<String> getSetting(Setting setting);
    
    <T> T getSettingAs(Setting setting, Class<T> cls);
    
    Map<String, String> getSettings();

}
