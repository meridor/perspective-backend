package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class LogFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(2),
                isInteger(0),
                isNumber(1),
                numberRelation(0, GREATER_THAN, 1),
                numberRelation(1, GREATER_THAN, 0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.LOG;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.log(Double.valueOf(String.valueOf(objects.get(1)))) / Math.log(Double.valueOf(String.valueOf(objects.get(0))));
    }

    @Override
    public String getSignature() {
        return "LOG(B, X)";
    }

    @Override
    public String getDescription() {
        return "Returns the base B logarithm of X. X should be greater than 0, B should be an integer greater than 1.";
    }

}
