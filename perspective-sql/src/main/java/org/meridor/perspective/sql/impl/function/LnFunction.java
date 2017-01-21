package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class LnFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0),
                numberRelation(0, GREATER_THAN, 0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.LN;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.log(Double.valueOf(String.valueOf(objects.get(0))));
    }

    @Override
    public String getSignature() {
        return "LN(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the natural logarithm of X. X should be greater than 0";
    }

}
