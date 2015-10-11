package org.meridor.perspective.shell.validator;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupportedCloudValidator extends EnumerationValidator {
    @Override
    protected List<String> getValues(Annotation annotation) {
        SupportedCloud ann = SupportedCloud.class.cast(annotation);
        CloudType[] acceptedCloudTypes = ann.value();
        return acceptedCloudTypes.length > 0 ?
                Arrays.stream(acceptedCloudTypes).map(CloudType::value).collect(Collectors.toList()) :
                Arrays.stream(CloudType.values()).map(CloudType::value).collect(Collectors.toList());
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return SupportedCloud.class;
    }
}
