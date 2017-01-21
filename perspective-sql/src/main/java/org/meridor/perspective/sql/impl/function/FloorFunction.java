package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class FloorFunction implements Function<Integer> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0)
        );
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.FLOOR;
    }

    @Override
    public Integer apply(List<Object> objects) {
        return Double.valueOf(Math.floor(Double.valueOf(String.valueOf(objects.get(0))))).intValue();
    }

    @Override
    public String getSignature() {
        return "FLOOR(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the largest integer value not greater than X.";
    }


}
