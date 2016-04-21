package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.shell.common.validator.annotation.RelativeToNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class NumberRelationValidator implements Validator {

    @Autowired
    private RelationChecker relationChecker;
    
    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) {
            return true;
        }
        RelativeToNumber ann = RelativeToNumber.class.cast(annotation);
        double number = ann.number();
        double doubleValue = Double.valueOf(value.toString());
        return relationChecker.checkDoubleRelation(doubleValue, number, ann.relation());
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RelativeToNumber.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        RelativeToNumber ann = RelativeToNumber.class.cast(annotation);
        String number = String.valueOf(ann.number());
        String relationSign = ann.relation().getText();
        return String.format("%s should be %s %s: %s given", fieldName, relationSign, number, value.toString());
    }

}
