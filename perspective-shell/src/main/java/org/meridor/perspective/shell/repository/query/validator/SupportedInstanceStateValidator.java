package org.meridor.perspective.shell.repository.query.validator;

import org.meridor.perspective.beans.InstanceState;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupportedInstanceStateValidator extends RangeValidator {
    @Override
    protected List<String> getRange(Annotation annotation) {
        SupportedInstanceState ann = SupportedInstanceState.class.cast(annotation);
        InstanceState[] acceptedInstanceStates = ann.value();
        return acceptedInstanceStates.length > 0 ?
                Arrays.stream(acceptedInstanceStates).map(InstanceState::value).collect(Collectors.toList()) :
                Arrays.stream(InstanceState.values()).map(InstanceState::value).collect(Collectors.toList());
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return SupportedInstanceState.class;
    }
}
