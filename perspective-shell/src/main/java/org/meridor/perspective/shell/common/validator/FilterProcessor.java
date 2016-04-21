package org.meridor.perspective.shell.common.validator;

public interface FilterProcessor {

    boolean hasAppliedFilters(Object object);
    
    <T> T applyFilters(T object);
    
    <T> T unsetFilters(T object);

}
