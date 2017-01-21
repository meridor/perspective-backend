package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class ReverseFunction implements Function<String> {

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
        return FunctionName.REVERSE;
    }

    @Override
    public String apply(List<Object> objects) {
        return new StringBuilder(String.valueOf(objects.get(0)))
                .reverse()
                .toString();
    }

    @Override
    public String getSignature() {
        return "REVERSE(S)";
    }

    @Override
    public String getDescription() {
        return "Returns the string S with the order of the characters reversed.";
    }
}
