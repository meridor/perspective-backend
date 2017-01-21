package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class Base64Function implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.BASE64;
    }

    @Override
    public String apply(List<Object> objects) {
        return new String(Base64.getEncoder().encode(String.valueOf(objects.get(0)).getBytes()));
    }

    @Override
    public String getSignature() {
        return "BASE64(S)";
    }

    @Override
    public String getDescription() {
        return "Converts the string S to Base64 encoded form and returns the result as a character string.";
    }
}
