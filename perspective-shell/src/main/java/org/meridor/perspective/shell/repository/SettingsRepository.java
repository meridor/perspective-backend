package org.meridor.perspective.shell.repository;

import java.util.Map;
import java.util.Set;

public interface SettingsRepository {
    Set<String> set(String data);

    Set<String> unset(String data);

    Map<String, String> showSettings(boolean all);

    Map<String, String> showFilters(boolean all);
}
