package org.meridor.perspective.sql.impl;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.meridor.perspective.sql.Parameter;
import org.meridor.perspective.sql.ParametersLexer;
import org.meridor.perspective.sql.ParametersParser;
import org.meridor.perspective.sql.ParametersParserBaseListener;

import java.sql.SQLDataException;
import java.util.*;

public class PlaceholderConfigurer extends ParametersParserBaseListener {
    
    private class InternalErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            String message = String.format("Parse error: \"%s\" near \"%s\" at %d:%d", msg, String.valueOf(offendingSymbol), line, charPositionInLine);
            SQLDataException ex = new SQLDataException(message, e);
            exception = Optional.of(ex);
        }
    }
    
    private final String sqlWithPlaceholders;
    private Map<String, String> parametersByName = new HashMap<>();
    private Map<Integer, String> parametersByIndex = new HashMap<>();
    private Integer parameterIndex = 1;
    private String currentQuery = "";
    private List<String> preparedQueries = new ArrayList<>();
    private Optional<SQLDataException> exception = Optional.empty(); 

    public PlaceholderConfigurer(String sqlWithPlaceholders, List<Parameter> parameters) {
        this.sqlWithPlaceholders = sqlWithPlaceholders;
        classifyParameters(parameters);
    }

    public List<String> getQueries() throws SQLDataException {
        CharStream input = new ANTLRInputStream(sqlWithPlaceholders);
        ANTLRErrorListener errorListener = new InternalErrorListener();
        ParametersLexer parametersLexer = new ParametersLexer(input);
        parametersLexer.removeErrorListeners();
        parametersLexer.addErrorListener(errorListener);
        CommonTokenStream commonTokenStream = new CommonTokenStream(parametersLexer);
        ParametersParser parametersParser = new ParametersParser(commonTokenStream);
        parametersParser.removeErrorListeners();
        parametersParser.addErrorListener(errorListener);
        ParseTree parseTree = parametersParser.queries();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
        if (exception.isPresent()) {
            throw exception.get();
        }
        return preparedQueries;
    }
    
    private void classifyParameters(List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if (parameter.getName() != null) {
                parametersByName.put(parameter.getName(), parameter.getValue());
            } else if (parameter.getIndex() != null) {
                parametersByIndex.put(parameter.getIndex(), parameter.getValue());
            }
        }
    }
    
    @Override
    public void exitQuery(ParametersParser.QueryContext ctx) {
        if (!currentQuery.isEmpty()) {
            preparedQueries.add(currentQuery);
        }
        currentQuery = "";
    }

    @Override
    public void exitText(ParametersParser.TextContext ctx) {
        currentQuery += ctx.getText();
    }

    @Override
    public void exitPositional_placeholder(ParametersParser.Positional_placeholderContext ctx) {
        if (!parametersByIndex.containsKey(parameterIndex)) {
            String message = String.format("Parameter for placeholder #%d is missing", parameterIndex);
            exception = Optional.of(new SQLDataException(message));
            return;
        }
        currentQuery += processValue(parametersByIndex.get(parameterIndex));
        parameterIndex++;
    }

    @Override
    public void exitNamed_placeholder(ParametersParser.Named_placeholderContext ctx) {
        String parameterName = ctx.ID().getText();
        if (!parametersByName.containsKey(parameterName)) {
            String message = String.format("Parameter for placeholder \"%s\" is missing", parameterName);
            exception = Optional.of(new SQLDataException(message));
            return;
        }
        currentQuery += processValue(parametersByName.get(parameterName));
    }

    private String processValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value to process can't be null");
        }
        return isPositiveInteger(value) ?
                value :
                String.format("'%s'", escapeValue(value));
    }
    
    private static boolean isPositiveInteger(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
    
    private static String escapeValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace(";", "\\;");
    }
}
