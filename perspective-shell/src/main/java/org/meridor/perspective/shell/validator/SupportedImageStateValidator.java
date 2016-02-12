package org.meridor.perspective.shell.validator;

import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.shell.validator.annotation.SupportedImageState;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupportedImageStateValidator extends EnumerationValidator {
    @Override
    protected List<String> getValues(Annotation annotation) {
        SupportedImageState ann = SupportedImageState.class.cast(annotation);
        ImageState[] acceptedImageStates = ann.value();
        return acceptedImageStates.length > 0 ?
                Arrays.stream(acceptedImageStates).map(ImageState::value).collect(Collectors.toList()) :
                Arrays.stream(ImageState.values()).map(ImageState::value).collect(Collectors.toList());
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return SupportedImageState.class;
    }
}
