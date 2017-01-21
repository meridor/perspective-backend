package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class ReplaceFunction implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(3)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.REPLACE;
    }

    @Override
    public String apply(List<Object> objects) {
        return String.valueOf(objects.get(0)).replace(
                String.valueOf(objects.get(1)),
                String.valueOf(objects.get(2))
        );
    }

    @Override
    public String getSignature() {
        return "REPLACE(S, SUBSTR, REPLACEMENT)";
    }

    @Override
    public String getDescription() {
        return "Returns the string S with all SUBSTR changed to REPLACEMENT.";
    }
}
