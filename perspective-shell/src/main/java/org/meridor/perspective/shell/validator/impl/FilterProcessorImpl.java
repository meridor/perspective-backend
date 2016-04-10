package org.meridor.perspective.shell.validator.impl;

import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.FilterProcessor;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.ShellException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.enumerateValues;

@Component
public class FilterProcessorImpl implements FilterProcessor {
    
    @Autowired
    private FiltersAware filtersAware;
    
    @Override
    public boolean hasAppliedFilters(Object object) {
        Object o = getRealObject(object);
        for (java.lang.reflect.Field f : o.getClass().getDeclaredFields()) {
            try {
                if (f.isAnnotationPresent(Filter.class)) {
                    Field field = f.getAnnotation(Filter.class).value();
                    f.setAccessible(true);
                    Object fieldValue = f.get(o);
                    if (filtersAware.hasFilter(field)) {
                        Set<String> filterValues = filtersAware.getFilter(field);
                        if (
                                ( isSet(f.getType()) && filterValues.equals(fieldValue) ) ||
                                enumerateValues(filterValues).equals(fieldValue)
                        ) {
                            return true;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new ShellException("Failed to process field " + f.getName(), e);
            }
        }
        return false;
    }

    @Override
    public <T> T applyFilters(T object) {
        T o = getRealObject(object);
        Arrays.stream(o.getClass().getDeclaredFields())
                .forEach(f -> {
                    try {
                        f.setAccessible(true);
                        Object value = f.get(o);
                        if (value == null && f.isAnnotationPresent(Filter.class)) {
                            Field field = f.getAnnotation(Filter.class).value();
                            if (filtersAware.hasFilter(field)) {
                                Set<String> filterValues = filtersAware.getFilter(field);
                                if (isSet(f.getType())) {
                                    value = filterValues;
                                } else if (filterValues.size() > 0) {
                                    value = enumerateValues(filterValues);
                                }
                                if (value != null) {
                                    f.set(o, value);
                                }
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new ShellException("Failed to apply filter for field " + f.getName(), e);
                    }
                });

        return object;
    }

    private static boolean isSet(Class<?> cls) {
        return Set.class.isAssignableFrom(cls);
    }

    @Override
    public <T> T unsetFilters(T object) {
        T o = getRealObject(object);
        Arrays.stream(o.getClass().getDeclaredFields())
                .forEach(f -> {
                    try {
                        f.setAccessible(true);
                        if (
                                f.isAnnotationPresent(Filter.class) &&
                                        f.get(o) != null &&
                                        filtersAware.hasFilter(f.getAnnotation(Filter.class).value())
                                ) {
                            f.set(o, null);
                        }
                    } catch (IllegalAccessException e) {
                        throw new ShellException("Failed to unset field " + f.getName(), e);
                    }
                });
        return o; //We remove validation logic by returning original object (not proxy with aspects)
    }
    
    private <T> T getRealObject(T object) {
        try {
            if (AopUtils.isAopProxy(object) && object instanceof Advised) {
                @SuppressWarnings("unchecked")
                T ret = (T) ((Advised) object).getTargetSource().getTarget();
                return ret;
            }
            return object;
        } catch (Exception e) {
            return object;
        }
    }
}
