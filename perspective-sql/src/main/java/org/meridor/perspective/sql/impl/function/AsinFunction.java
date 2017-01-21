package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class AsinFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0),
                between(0, -1d, 1d)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.ASIN;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.asin(Double.valueOf(String.valueOf(objects.get(0))));
    }

    @Override
    public String getSignature() {
        return "ASIN(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the arc sine of X. X should be between -1 and 1.";
    }

}
