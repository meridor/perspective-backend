package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PiFunction implements Function<Double> {

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.PI;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.PI;
    }

    @Override
    public String getDescription() {
        return "Returns the π (pi) number.";
    }

}
