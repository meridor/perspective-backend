package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN_EQUAL;
import static org.meridor.perspective.beans.BooleanRelation.LESS_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class SubstrFunction implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(GREATER_THAN_EQUAL, 2),
                argsCount(LESS_THAN_EQUAL, 3),
                isInteger(1),
                isInteger(2)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.SUBSTR;
    }

    @Override
    public String apply(List<Object> objects) {
        String string = String.valueOf(objects.get(0));
        Integer from = Integer.valueOf(String.valueOf(objects.get(1)));
        Optional<Integer> to = objects.size() == 3 ?
                Optional.of(Integer.valueOf(String.valueOf(objects.get(2)))) :
                Optional.empty();
        return to.isPresent() ?
                string.substring(from, to.get()) :
                string.substring(from);
    }

    @Override
    public String getSignature() {
        return "SUBSTR(S, FROM[, TO])";
    }

    @Override
    public String getDescription() {
        return "Return a substring from string S starting at position FROM. If TO is specified end at position TO - 1. Otherwise goes to the end of S.";
    }
}
