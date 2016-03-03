package org.meridor.perspective.sql;

import java.util.regex.Pattern;

public final class PatternUtils {

    public static boolean isExactMatch(String expression) {
        return expression != null && expression.startsWith("^") && expression.endsWith("$");
    }

    public static boolean isContainsMatch(String expression) {
        return expression != null && expression.startsWith("%") && expression.endsWith("%");
    }

    public static boolean isRegex(String expression) {
        try {
            Pattern.compile(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String removeFirstAndLastChars(String expression) {
        if (expression.length() < 2) {
            return expression;
        }
        return expression.substring(1, expression.length() - 1);
    }

    private PatternUtils() {
        
    }
}
