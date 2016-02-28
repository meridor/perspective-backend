package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.Parameter;

import java.util.ArrayList;
import java.util.List;

public class BaseQueryPart implements QueryPart {
    
    private final StringBuilder sb = new StringBuilder();
    private final List<Parameter> parameters = new ArrayList<>();
    
    @Override
    public String getSql() {
        return sb.toString();
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    protected void addToSql(String str) {
        sb.append(str);
    }
    
    protected void addParameter(String name, String value) {
        Parameter parameter = new Parameter();
        parameter.setName(name);
        parameter.setValue(value);
        parameters.add(parameter);
    }
}
