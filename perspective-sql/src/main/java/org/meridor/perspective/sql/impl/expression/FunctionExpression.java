package org.meridor.perspective.sql.impl.expression;

import java.util.List;
import java.util.stream.Collectors;

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
    public boolean equals(Object another) {
        return 
                another instanceof FunctionExpression
                && functionName.equals(((FunctionExpression) another).getFunctionName())
                && args.equals(((FunctionExpression) another).getArgs());
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)",
                functionName,
                args.stream().map(FunctionExpression::beautifyArg).collect(Collectors.joining(", "))
        );
    }
    
    private static String beautifyArg(Object arg) {
        String argString = String.valueOf(arg);
        if (
                (arg != null) &&
                ( ExpressionUtils.isString(arg.getClass()) || ExpressionUtils.isDate(arg.getClass()) )
        ) {
            return String.format("'%s'", argString);
        }
        return argString;
    }
}
