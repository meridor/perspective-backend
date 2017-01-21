package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class ConcatFunction implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(GREATER_THAN_EQUAL, 1)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.CONCAT;
    }

    @Override
    public String apply(List<Object> objects) {
        return objects.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    @Override
    public String getSignature() {
        return "CONCAT(S1, S2, ... SN)";
    }

    @Override
    public String getDescription() {
        return "Returns the string that results from concatenating the arguments.";
    }
}
