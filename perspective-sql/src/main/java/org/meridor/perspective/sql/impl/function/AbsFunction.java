package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AbsFunction implements Function<Double> {
    
    @Override
    public Set<String> validateInput(List<Object> args) {
        Set<String> errors = new HashSet<>();
        if (args.size() != 1) {
            errors.add("Function accepts one argument only");
        }
        Object firstArg = args.get(0);
        if (!(
                firstArg instanceof Double ||
                firstArg instanceof Float ||
                firstArg instanceof Integer ||
                firstArg instanceof Long 
        )){
            errors.add(String.format("Function argument should be a number but a %s is given", firstArg.getClass().getCanonicalName()));
        }
        return errors;
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.ABS;
    }

    @Override
    public Double apply(List<Object> objects) {
        return Math.abs(Double.valueOf(objects.get(0).toString()));
    }
}
