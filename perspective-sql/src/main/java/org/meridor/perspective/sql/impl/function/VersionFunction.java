package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class VersionFunction implements Function<String> {
    
    @Override
    public Set<String> validateInput(List<Object> args) {
        return Collections.emptySet();
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.VERSION;
    }

    @Override
    public String apply(List<Object> objects) {
        Optional<String> versionCandidate = Optional.ofNullable(getClass().getPackage().getImplementationVersion());
        return versionCandidate.isPresent() ? versionCandidate.get() : "unknown";
    }
}
