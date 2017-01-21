package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class ConvFunction implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(3),
                isInteger(0, Character.MAX_RADIX),
                between(1, Character.MIN_RADIX, Character.MAX_RADIX),
                between(2, Character.MIN_RADIX, Character.MAX_RADIX)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.CONV;
    }

    @Override
    public String apply(List<Object> objects) {
        Integer base1 = Integer.parseInt(String.valueOf(objects.get(1)));
        Integer base2 = Integer.parseInt(String.valueOf(objects.get(2)));
        return Integer.toString(Integer.parseInt(String.valueOf(objects.get(0)), base1), base2);
    }

    @Override
    public String getSignature() {
        return "CONV(N, B1, B2)";
    }

    @Override
    public String getDescription() {
        return String.format(
                "Converts number N from number base B1 to B2. B1 and B2 should be between %d and %d.",
                Character.MIN_RADIX,
                Character.MAX_RADIX
        );
    }


}
