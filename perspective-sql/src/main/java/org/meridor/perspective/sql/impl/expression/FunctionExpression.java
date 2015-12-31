package org.meridor.perspective.sql.impl.expression;

import java.util.List;

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
}
