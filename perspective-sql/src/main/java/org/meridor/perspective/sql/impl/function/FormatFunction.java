package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class FormatFunction implements Function<String> {

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
        return FunctionName.FORMAT;
    }

    @Override
    public String apply(List<Object> objects) {
        Object[] formatArgs = objects.subList(1, objects.size()).toArray();
        return String.format(String.valueOf(objects.get(0)), formatArgs);
    }

    @Override
    public String getSignature() {
        return "FORMAT(F[, V1, V2, ...])";
    }

    @Override
    public String getDescription() {
        return "Returns a formatted string with format F and one or more values V. Standard C placeholders like %s or %d should be used.";
    }

}
