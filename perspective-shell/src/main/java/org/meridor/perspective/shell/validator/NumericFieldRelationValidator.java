package org.meridor.perspective.shell.validator;

import org.meridor.perspective.shell.validator.annotation.RelativeToNumericField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class NumericFieldRelationValidator implements Validator {

    @Autowired
    private RelationChecker relationChecker;

    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) {
            return true;
        }
        RelativeToNumericField ann = RelativeToNumericField.class.cast(annotation);
        String fieldName = ann.field();
        double doubleValue = Double.valueOf(value.toString());
        try {
            java.lang.reflect.Field field = instance.getClass().getField(fieldName);
            double fieldValue = Double.valueOf(field.get(instance).toString());
            return relationChecker.checkDoubleRelation(doubleValue, fieldValue, ann.relation());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RelativeToNumericField.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        RelativeToNumericField ann = RelativeToNumericField.class.cast(annotation);
        String numericFieldName = ann.field();
        String relationSign = ann.relation().getSign();
        return String.format("%s should be %s %s value: %s given", fieldName, relationSign, numericFieldName, value.toString());
    }
}
