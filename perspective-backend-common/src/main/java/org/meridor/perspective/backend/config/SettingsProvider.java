package org.meridor.perspective.backend.config;

import java.util.List;
import java.util.Optional;

public interface SettingsProvider {
    
    Optional<String> get(String settingName);
    
    List<String> getList(String settingName);

    <T> Optional<T> getAs(String settingName, Class<T> cls);
    
}
