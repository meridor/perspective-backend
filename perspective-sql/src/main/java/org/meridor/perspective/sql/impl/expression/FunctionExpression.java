package org.meridor.perspective.sql.impl.expression;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionExpression {
    
    private final String functionName;
    
    private final List<Object> args;

    public FunctionExpression(String functionName, List<Object> args) {
        this.functionName = functionName;
        this.args = args;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Object> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)",
                functionName,
                Stream.of(args).map(String::valueOf).collect(Collectors.joining(", "))
        );
    }
}
