package org.meridor.perspective.backend.config.impl;

import org.meridor.perspective.backend.config.SettingsProvider;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class PropertiesSettingsProvider implements SettingsProvider {
    
    private static final String COMMA = ",";
    
    private final Properties properties;

    public PropertiesSettingsProvider(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<String> get(String settingName) {
        return Optional.ofNullable(properties.getProperty(settingName));
    }

    @Override
    public List<String> getList(String settingName, List<String> defaultValue) {
        Optional<String> stringCandidate = get(settingName);
        return stringCandidate.isPresent() ?
                Arrays.asList(stringCandidate.get().split(COMMA)) :
                defaultValue;
    }

    @Override
    public <T> Optional<T> getAs(String settingName, Class<T> cls) {
        Optional<String> settingCandidate = get(settingName);
        if (settingCandidate.isPresent()) {
            try {
                Constructor<T> constructor = cls.getConstructor(String.class);
                return Optional.of(constructor.newInstance(settingCandidate.get()));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
