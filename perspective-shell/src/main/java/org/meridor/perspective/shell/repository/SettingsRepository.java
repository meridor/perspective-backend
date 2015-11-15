package org.meridor.perspective.shell.repository;

import java.util.Map;
import java.util.Set;

public interface SettingsRepository {
    Set<String> set(String data);

    Set<String> unset(String data);

    Map<String, String> showSettings();

    Map<String, String> showFilters();
}
