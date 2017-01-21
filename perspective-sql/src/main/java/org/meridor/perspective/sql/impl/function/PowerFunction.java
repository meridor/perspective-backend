package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class PowerFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(2),
                isNumber(0),
                isNumber(1)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.POWER;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.pow(Double.valueOf(String.valueOf(objects.get(0))), Double.valueOf(String.valueOf(objects.get(1))));
    }

    @Override
    public String getSignature() {
        return "POWER(X, Y)";
    }

    @Override
    public String getDescription() {
        return "Returns the value of X raised to the power of Y.";
    }

}
