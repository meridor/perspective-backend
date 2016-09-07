package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.shell.common.events.EventBus;
import org.meridor.perspective.shell.common.events.PromptChangedEvent;
import org.meridor.perspective.shell.common.repository.FiltersAware;
import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.repository.SettingsRepository;
import org.meridor.perspective.shell.common.validator.Field;
import org.meridor.perspective.shell.common.validator.Setting;
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
    
    @Autowired
    private EventBus eventBus;

    @Override public Set<String> set(String data) {
        Set<String> errors = new HashSet<>();
        Map<String, Set<String>> values = TextUtils.parseAssignment(data);
        values.keySet().forEach(
                k -> {
                    Set<String> value = values.get(k);
                    String enumName = k.toUpperCase();

                    if (value.isEmpty()) {
                        errors.add(String.format("Filter or setting with name = %s is empty", k));
                    } else if (Field.contains(enumName)) {
                        Field field = Field.valueOf(enumName);
                        filtersAware.setFilter(field, value);
                        changePromptIfNeeded();
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
                        changePromptIfNeeded();
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
    
    private void changePromptIfNeeded() {
        if (filtersAware.getFilters(false).isEmpty()) {
            eventBus.fire(new PromptChangedEvent());
        } else {
            eventBus.fire(new PromptChangedEvent("perspective*>"));
        }
    }
    
    @Override public Map<String, String> showSettings(boolean all) {
        return settingsAware.getSettings(all);
    }


    @Override public Map<String, String> showFilters(boolean all) {
        return filtersAware.getFilters(all);
    }

}
