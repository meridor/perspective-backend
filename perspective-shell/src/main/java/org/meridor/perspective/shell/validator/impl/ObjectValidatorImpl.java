package org.meridor.perspective.shell.validator.impl;

import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.ObjectValidator;
import org.meridor.perspective.shell.validator.Validator;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.enumerateValues;

@Component
public class ObjectValidatorImpl implements ObjectValidator {

    @Autowired
    private FiltersAware filtersAware;

    @Autowired
    private ApplicationContext applicationContext;

    private Collection<Validator> getValidators() {
        return applicationContext.getBeansOfType(Validator.class).values();
    }

    @Override
    public Set<String> validate(Object object) {
        Set<String> errors = new HashSet<>();

        Arrays.stream(object.getClass().getDeclaredFields())
                .forEach(f -> {
                        f.setAccessible(true);
                        Object value = getValueFromFieldOrFilters(object, f, errors);
                        getValidators().stream()
                            .filter(v -> f.isAnnotationPresent(v.getAnnotationClass()))
                            .forEach(v -> {
                                Set<String> fieldErrors = validateField(v, object, value, f);
                                errors.addAll(fieldErrors);
                            });
                    }
                );
        
        return errors;
    }
    
    private Set<String> validateField(Validator v, Object object, Object value, java.lang.reflect.Field f) {
        Set<String> errors = new HashSet<>();
        String filterName = f.getName();
        Annotation annotation = f.getAnnotation(v.getAnnotationClass());
        if (value != null && isSet(value.getClass())) {
            Set<?> set = Set.class.cast(value);
            set.stream().forEach(val -> {
                if (!v.validate(object, annotation, val)) {
                    errors.add(v.getMessage(annotation, filterName, value));
                }
            });
        } else {
            if (!v.validate(object, annotation, value)) {
                errors.add(v.getMessage(annotation, filterName, value));
            }
        }
        return errors;
    }
    
    private Object getValueFromFieldOrFilters(Object object, java.lang.reflect.Field f, Set<String> errors) {
        Object value = null;
        try {
            value = f.get(object);
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
                        f.set(object, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            errors.add(String.format(
                    "Failed to read field \"%s\" value",
                    f.getName()
            ));

        }
        return value;
    }
    
    private boolean isSet(Class<?> cls) {
        return Set.class.isAssignableFrom(cls);
    }
    
}
