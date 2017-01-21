package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class BitLengthFunction implements Function<Integer> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1)
        );
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.BIT_LENGTH;
    }

    @Override
    public Integer apply(List<Object> objects) {
        return String.valueOf(objects.get(0)).getBytes().length * 8;
    }

    @Override
    public String getSignature() {
        return "BIT_LENGTH(S)";
    }

    @Override
    public String getDescription() {
        return "Returns the length of the string S in bits.";
    }
}
