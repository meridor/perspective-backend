package org.meridor.perspective.shell.common.validator.impl;

import org.meridor.perspective.shell.common.validator.FilterProcessor;
import org.meridor.perspective.shell.common.validator.ObjectValidator;
import org.meridor.perspective.shell.common.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class ObjectValidatorImpl implements ObjectValidator {

    @Autowired
    private FilterProcessor filterProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    private Collection<Validator> getValidators() {
        return applicationContext.getBeansOfType(Validator.class).values();
    }

    @Override
    public Set<String> validate(Object object) {
        Set<String> errors = new HashSet<>();
        filterProcessor.applyFilters(object);
        Arrays.stream(object.getClass().getDeclaredFields())
                .forEach(f -> {
                            try {
                                f.setAccessible(true);
                                Object value = f.get(object);
                                getValidators().stream()
                                    .filter(v -> f.isAnnotationPresent(v.getAnnotationClass()))
                                    .forEach(v -> {
                                        Set<String> fieldErrors = validateField(v, object, value, f);
                                        errors.addAll(fieldErrors);
                                    });
                            } catch (IllegalAccessException e) {
                                errors.add(String.format(
                                        "Failed to read field \"%s\" value",
                                        f.getName()
                                ));
                            }
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

    private boolean isSet(Class<?> cls) {
        return Set.class.isAssignableFrom(cls);
    }
    
}
