package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class TrimFunction implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.TRIM;
    }

    @Override
    public String apply(List<Object> objects) {
        return String.valueOf(objects.get(0)).trim();
    }

    @Override
    public String getSignature() {
        return "TRIM(S)";
    }

    @Override
    public String getDescription() {
        return "Returns the string S with leading and trailing spaces removed.";
    }
}
