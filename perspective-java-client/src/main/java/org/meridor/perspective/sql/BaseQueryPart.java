package org.meridor.perspective.sql;

import java.util.*;
import java.util.function.BiFunction;

public class BaseQueryPart implements QueryPart {
    
    private final StringBuilder sb = new StringBuilder();
    private final Map<String, Parameter> parameters = new LinkedHashMap<>();
    
    @Override
    public String getSql() {
        return sb.toString();
    }

    @Override
    public List<Parameter> getParameters() {
        return new ArrayList<>(parameters.values());
    }
    
    protected void addToSql(String str) {
        sb.append(str);
    }
    
    protected Parameter addParameter(String name, String value) {
        String parameterName = name;
        int index = 1;
        while (parameters.containsKey(parameterName)) {
            parameterName = name + index;
            index++;
        }
        Parameter parameter = new Parameter();
        parameter.setName(parameterName);
        parameter.setValue(value);
        parameters.put(parameterName, parameter);
        return parameter;
    }

    protected static <T> T joinWith(Map<String, Collection<String>> columnValues, BiFunction<String, String, T> matchingFunction, Runnable joiningOperation, T returnValue) {
        int position = 0;
        for (String columnName : columnValues.keySet()) {
            Collection<String> values = columnValues.get(columnName);
            for (String value : values) {
                if (position > 0) {
                    joiningOperation.run();
                }
                matchingFunction.apply(columnName, value);
                position++;
            }
        }
        return returnValue;
    }

}
