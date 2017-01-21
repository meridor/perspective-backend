package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class CosFunction implements Function<Double> {

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
        return FunctionName.COS;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.cos(Double.valueOf(String.valueOf(objects.get(0))));
    }

    @Override
    public String getSignature() {
        return "COS(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the cosine of X given in radians.";
    }

}
