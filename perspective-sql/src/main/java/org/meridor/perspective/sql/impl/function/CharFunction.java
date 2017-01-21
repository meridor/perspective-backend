package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.meridor.perspective.beans.BooleanRelation.GREATER_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.*;

@Component
public class CharFunction implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        List<java.util.function.Function<List<Object>, Set<String>>> conditions = new ArrayList<>();
        conditions.add(argsCount(GREATER_THAN_EQUAL, 1));
        for (int i = 0; i <= args.size() - 1; i++) {
            conditions.add(isInteger(i));
        }
        @SuppressWarnings("unchecked")
        java.util.function.Function<List<Object>, Set<String>>[] conditionsArr = conditions.toArray(new java.util.function.Function[args.size()]);
        return oneOf(
                args,
                conditionsArr
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.CHAR;
    }

    @Override
    public String apply(List<Object> objects) {
        StringBuilder stringBuilder = new StringBuilder();
        objects.stream()
                .map(c -> (char) Integer.valueOf(String.valueOf(c)).intValue())
                .forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @Override
    public String getSignature() {
        return "CHAR(C1, C2, ... CN)";
    }

    @Override
    public String getDescription() {
        return "Interprets each argument C as an integer and returns a string consisting of the characters given by the code values of those integers.";
    }
}
