package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.shell.repository.FiltersAware;
import org.meridor.perspective.shell.repository.query.validator.Field;
import org.meridor.perspective.shell.repository.query.validator.Filter;
import org.meridor.perspective.shell.repository.query.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.*;

@Component
public abstract class BaseQuery<T> implements Query<T> {

    @Autowired
    private FiltersAware filtersAware;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private Collection<Validator> getValidators() {
        return applicationContext.getBeansOfType(Validator.class).values();
    }
    
    @Override
    public Set<String> validate() {
        Set<String> errors = new HashSet<>();
        getValidators().forEach(v -> Arrays.stream(getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(v.getAnnotationClass()))
                .forEach(f -> {
                    String filterName = f.getName();
                    Annotation annotation = f.getAnnotation(v.getAnnotationClass());
                    try {
                        Object value;
                        
                        if (f.isAnnotationPresent(Filter.class)) {
                            Field field = f.getAnnotation(Filter.class).value();
                            if (filtersAware.hasFilter(field)) {
                                value = filtersAware.getFilter(field);
                            } else {
                                value = f.get(this);
                            }
                        } else {
                            value = f.get(this);
                        }
                        if (!v.validate(annotation, value)) {
                            errors.add(v.getMessage(annotation, filterName, value));
                        }
                    } catch (IllegalAccessException e) {
                        errors.add(String.format(
                                "Failed to read field %s value",
                                filterName
                        ));
                    }
                }));
        return errors;
    }
}
