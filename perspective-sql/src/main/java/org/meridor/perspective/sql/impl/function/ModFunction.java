package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class ModFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(2),
                isNumber(0),
                isNumber(1),
                numberRelation(1, GREATER_THAN, 0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.MOD;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Double.valueOf(String.valueOf(objects.get(0))) % Double.valueOf(String.valueOf(objects.get(1)));
    }

    @Override
    public String getSignature() {
        return "MOD(X, Y)";
    }

    @Override
    public String getDescription() {
        return "Returns the remainder of X divided by Y. Y should be positive.";
    }

}
