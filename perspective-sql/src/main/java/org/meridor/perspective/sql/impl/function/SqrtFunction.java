package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.beans.BooleanRelation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class SqrtFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0),
                numberRelation(0, BooleanRelation.GREATER_THAN_EQUAL, 0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.SQRT;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.sqrt(Double.valueOf(String.valueOf(objects.get(0))));
    }

    @Override
    public String getSignature() {
        return "SQRT(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the square root of X. X should be positive.";
    }


}
