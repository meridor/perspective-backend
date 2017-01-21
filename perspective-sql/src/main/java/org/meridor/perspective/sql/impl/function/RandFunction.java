package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.LESS_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class RandFunction implements Function<Double> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(LESS_THAN_EQUAL, 1),
                isInteger(0)
        );
    }

    @Override
    public Class<Double> getReturnType() {
        return Double.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.RAND;
    }

    @Override
    public Double apply(List<Object> objects) {
        Optional<Long> seed = (objects.size() == 1) ?
                Optional.of(Long.valueOf(String.valueOf(objects.get(0)))) : Optional.empty();
        Random random = (seed.isPresent()) ?
                new Random(seed.get()) :
                new Random();
        return random.nextDouble();
    }

    @Override
    public String getSignature() {
        return "RAND([S])";
    }

    @Override
    public String getDescription() {
        return "Returns a random floating-point value v in the range [0; 1.0) optionally using seed value S.";
    }
}
