package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.NOT_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class CotFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0),
                numberRelation(0, NOT_EQUAL, 0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.COT;
    }

    @Override
    public Double apply(List<Object> objects) {
        Double doubleValue = Double.valueOf(String.valueOf(objects.get(0)));
        return Math.cos(doubleValue) / Math.sin(doubleValue);
    }

    @Override
    public String getSignature() {
        return "COT(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the cotangent of X given in radians.";
    }

}
