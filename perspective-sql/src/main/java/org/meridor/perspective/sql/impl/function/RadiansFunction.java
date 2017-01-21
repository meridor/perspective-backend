package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class RadiansFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.RADIANS;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.toRadians(Double.valueOf(String.valueOf(objects.get(0))));
    }

    @Override
    public String getSignature() {
        return "RADIANS(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the argument X, converted from degrees to radians.";
    }

}
