package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class AbsFunction implements Function<Double> {
    
    @Override
    public Set<String> validateInput(List<Object> args) {
        if (args.size() != 1) {
            return Collections.singleton("Function accepts one argument only");
        }
        Object firstArg = args.get(0);
        if (!(
                firstArg instanceof Double ||
                firstArg instanceof Float ||
                firstArg instanceof Integer ||
                firstArg instanceof Long 
        )){
            return Collections.singleton(String.format(
                    "Function argument should be a number but a %s is given",
                    firstArg != null ? firstArg.getClass().getCanonicalName() : "NULL"
            ));
        }
        return Collections.emptySet();
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
