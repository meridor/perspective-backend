package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.shell.common.validator.Field;

import java.util.Map;
import java.util.Set;

public interface FiltersAware {
    
    boolean hasFilter(Field field);

    void setFilter(Field field, Set<String> value);

    void unsetFilter(Field field);
    
    Set<String> getFilter(Field field);

    <T> T getFilterAs(Field field, Class<T> cls);
    
    Map<String, String> getFilters(boolean all);
    
}
