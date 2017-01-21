package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class CeilFunction implements Function<Integer> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1),
                isNumber(0)
        );
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.CEIL;
    }

    @Override
    public Integer apply(List<Object> objects) {
        return Double.valueOf(Math.ceil(Double.valueOf(String.valueOf(objects.get(0))))).intValue();
    }

    @Override
    public String getSignature() {
        return "CEIL(X)";
    }

    @Override
    public String getDescription() {
        return "Returns the smallest integer value not less than X.";
    }

}
