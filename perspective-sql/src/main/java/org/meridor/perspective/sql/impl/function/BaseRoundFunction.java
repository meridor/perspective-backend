package org.meridor.perspective.sql.impl.function;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN_EQUAL;
import static org.meridor.perspective.beans.BooleanRelation.LESS_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

public abstract class BaseRoundFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(GREATER_THAN_EQUAL, 1),
                argsCount(LESS_THAN_EQUAL, 2),
                isNumber(0),
                isInteger(1),
                numberRelation(1, GREATER_THAN_EQUAL, 0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    protected abstract RoundingMode getRoundingMode();

    @Override
    public Double apply(List<Object> objects) {
        DecimalFormat df = new DecimalFormat();
        df.setRoundingMode(getRoundingMode());
        int digits = objects.size() == 2 ? Integer.valueOf(String.valueOf(objects.get(1))) : 0;
        df.setMaximumFractionDigits(digits);
        return Double.valueOf(df.format(Double.valueOf(String.valueOf(objects.get(0)))));
    }


}
