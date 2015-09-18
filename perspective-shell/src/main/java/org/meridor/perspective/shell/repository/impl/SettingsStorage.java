package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class SettingsStorage implements FiltersAware, SettingsAware {
    
    private Map<String, Set<String>> storage = new HashMap<>();
    
    @Override
    public boolean hasFilter(Field field) {
        return storage.containsKey(field.name());
    }
    
    @Override
    public void setFilter(Field field, Set<String> value){
        storage.put(field.name(), value);
    }
    
    @Override
    public void unsetFilter(Field field){
        storage.remove(field.name());
    }

    @Override
    public Map<String, String> getFilters() {
        Map<String, String> filters = new HashMap<>();
        Arrays.stream(Field.values()).forEach(
                f -> {
                    if (hasFilter(f)) {
                        String key = f.name().toLowerCase();
                        String value = TextUtils.enumerateValues(getFilter(f));
                        filters.put(key, value);
                    }
                }
        );
        return filters;
    }

    @Override
    public Set<String> getFilter(Field field) {
        return storage.get(field.name());
    }

    @Override
    public boolean hasSetting(Setting setting) {
        return storage.containsKey(setting.name());
    }

    @Override
    public void setSetting(Setting setting, Set<String> value) {
        storage.put(setting.name(), value);
    }

    @Override
    public void unsetSetting(Setting setting){
        storage.remove(setting.name());
    }

    @Override
    public Set<String> getSetting(Setting setting) {
        return this.storage.get(setting.name());
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = new HashMap<>();
        Arrays.stream(Setting.values()).forEach(
                s -> {
                    if (hasSetting(s)) {
                        String key = s.name().toLowerCase();
                        String value = TextUtils.enumerateValues(getSetting(s));
                        settings.put(key, value);
                    }
                }
        );
        return settings;

    }

}
