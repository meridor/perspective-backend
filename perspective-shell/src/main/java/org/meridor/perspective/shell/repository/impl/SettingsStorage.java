package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.meridor.perspective.shell.repository.impl.TextUtils.enumerateValues;

@Component
public class SettingsStorage implements FiltersAware, SettingsAware {
    
    private Map<String, Set<String>> storage = new HashMap<>();
    
    private Set<Converter> converters = new HashSet<>();
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        Map<String, Converter> availableConverters = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, Converter.class);
        converters.addAll(availableConverters.values());
    }
    
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
        Arrays
            .stream(Field.values())
            .sorted((f1, f2) -> Comparator.<String>naturalOrder().compare(f1.name(), f2.name()))
            .forEach(
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
    public <T> T getFilterAs(Field field, Class<T> cls) {
        return getValueAs(field.name().toLowerCase(), getFilter(field), cls);
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
    public <T> T getSettingAs(Setting setting, Class<T> cls) {
        return getValueAs(setting.name().toLowerCase(), getSetting(setting), cls);
    }

    private <T> T getValueAs(String name, Set<String> settingValue, Class<T> cls) {
        String rawValue = (settingValue.size() == 1) ?
                settingValue.toArray(new String[1])[0] :
                enumerateValues(settingValue);
        for (Converter<?> converter : converters) {
            if (converter.supports(cls, "")) {
                @SuppressWarnings("unchecked")
                T ret = (T) converter.convertFromText(rawValue, cls, "");
                return ret;
            }
        }
        throw new IllegalArgumentException(String.format("Can't get %s as %s", name, cls.getCanonicalName()));
    }
    
    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = new HashMap<>();
        Arrays
            .stream(Setting.values())
            .sorted((s1, s2) -> Comparator.<String>naturalOrder().compare(s1.name(), s2.name()))
            .forEach(
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
