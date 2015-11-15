package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.repository.SettingsRepository;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class SettingsRepositoryImpl implements SettingsRepository {

    @Autowired
    private FiltersAware filtersAware;

    @Autowired
    private SettingsAware settingsAware;

    @Override public Set<String> set(String data) {
        Set<String> errors = new HashSet<>();
        Map<String, Set<String>> values = TextUtils.parseAssignment(data);
        values.keySet().stream().forEach(
                k -> {
                    Set<String> value = values.get(k);
                    String enumName = k.toUpperCase();

                    if (value.isEmpty()) {
                        errors.add(String.format("Filter or setting with name = %s is empty", k));
                    } else if (Field.contains(enumName)) {
                        Field field = Field.valueOf(enumName);
                        filtersAware.setFilter(field, value);
                    } else if (Setting.contains(enumName)) {
                        Setting setting = Setting.valueOf(enumName);
                        settingsAware.setSetting(setting, value);
                    } else {
                        errors.add(String.format("Filter or setting with name = %s not found", k));
                    }
                }
        );
        return errors;
    }
    
    @Override public Set<String> unset(String data) {
        Set<String> errors = new HashSet<>();
        Map<String, Set<String>> values = TextUtils.parseAssignment(data);
        values.keySet().stream().forEach(
                k -> {
                    String enumName = k.toUpperCase();
                    if (Field.contains(enumName)) {
                        Field field = Field.valueOf(enumName);
                        filtersAware.unsetFilter(field);
                    } else if (Setting.contains(enumName)) {
                        Setting setting = Setting.valueOf(enumName);
                        settingsAware.unsetSetting(setting);
                    } else {
                        errors.add(String.format("Filter or setting with name = %s not found", k));
                    }
                }
        );
        return errors;
    }
    
    @Override public Map<String, String> showSettings() {
        return settingsAware.getSettings();
    }


    @Override public Map<String, String> showFilters() {
        return filtersAware.getFilters();
    }

}
