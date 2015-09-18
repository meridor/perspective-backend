package org.meridor.perspective.shell.query.impl;

import org.meridor.perspective.shell.query.Query;
import org.meridor.perspective.shell.query.QueryValidator;
import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class QueryValidatorImpl implements QueryValidator {

    @Autowired
    private FiltersAware filtersAware;

    @Autowired
    private ApplicationContext applicationContext;

    private Collection<Validator> getValidators() {
        return applicationContext.getBeansOfType(Validator.class).values();
    }

    @Override
    public Set<String> validate(Query<?> query) {
        Set<String> errors = new HashSet<>();
        getValidators().forEach(v -> Arrays.stream(query.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(v.getAnnotationClass()))
                .forEach(f -> {
                    Set<String> fieldErrors = validateField(v, query, f);
                    errors.addAll(fieldErrors);
                }));
        return errors;
    }
    
    private Set<String> validateField(Validator v, Query<?> query, java.lang.reflect.Field f) {
        Set<String> errors = new HashSet<>();
        String filterName = f.getName();
        Annotation annotation = f.getAnnotation(v.getAnnotationClass());
        try {
            Object value = getValue(query, f);
            if (value != null && isSet(value.getClass())) {
                Set<?> set = Set.class.cast(value);
                set.stream().forEach(val -> {
                    if (!v.validate(annotation, val)) {
                        errors.add(v.getMessage(annotation, filterName, value));
                    }
                });
            } else {
                if (!v.validate(annotation, value)) {
                    errors.add(v.getMessage(annotation, filterName, value));
                }
            }
        } catch (IllegalAccessException e) {
            errors.add(String.format(
                    "Failed to read field \"%s\" value",
                    filterName
            ));
        }
        return errors;
    }
    
    private Object getValue(Query<?> query, java.lang.reflect.Field f) throws IllegalAccessException {
        f.setAccessible(true);
        Object value = f.get(query);
        if (value == null && f.isAnnotationPresent(Filter.class)) {
            Field field = f.getAnnotation(Filter.class).value();
            if (filtersAware.hasFilter(field)) {
                Set<String> filterValues = filtersAware.getFilter(field);
                if (isSet(f.getType())) {
                    f.set(query, filterValues);
                } else if (filterValues.size() > 0) {
                    f.set(query, filterValues.iterator().next());
                }
            }
        }
        return value;
    }
    
    private boolean isSet(Class<?> cls) {
        return Set.class.isAssignableFrom(cls);
    }
    
}
