package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EFunction implements Function<Double> {

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.E;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.E;
    }

    @Override
    public String getDescription() {
        return "Returns Euler's number.";
    }

}
