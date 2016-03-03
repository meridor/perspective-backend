package org.meridor.perspective.shell.validator;

import org.meridor.perspective.shell.validator.annotation.Pattern;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

import static org.meridor.perspective.sql.PatternUtils.isContainsMatch;
import static org.meridor.perspective.sql.PatternUtils.isExactMatch;
import static org.meridor.perspective.sql.PatternUtils.isRegex;

@Component
public class PatternValidator implements Validator {
    
    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) {
            return true;
        }
        String str = value.toString();
        return isExactMatch(str) || isContainsMatch(str) || isRegex(str);
    }
    
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Pattern.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        return String.format("%s should contain one or more regular expression or %%text%% or ^text$", fieldName);
    }
}
