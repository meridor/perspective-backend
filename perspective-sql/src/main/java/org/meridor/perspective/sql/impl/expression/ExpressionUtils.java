package org.meridor.perspective.sql.impl.expression;

import java.time.ZonedDateTime;

public final class ExpressionUtils {

    public  static boolean isConstant(Class<?> expressionClass) {
        return
                isNumber(expressionClass) ||
                        isString(expressionClass) ||
                        isDate(expressionClass) ||
                        isBoolean(expressionClass) ||
                        expressionClass.isEnum();
    }

    public  static boolean isString(Class<?> expressionClass) {
        return String.class.isAssignableFrom(expressionClass);
    }

    public  static boolean isInteger(Class<?> expressionClass) {
        return Integer.class.isAssignableFrom(expressionClass) || Long.class.isAssignableFrom(expressionClass);
    }

    public  static boolean isDouble(Class<?> expressionClass) {
        return Float.class.isAssignableFrom(expressionClass) || Double.class.isAssignableFrom(expressionClass);
    }

    public  static boolean isNumber(Class<?> expressionClass) {
        return isInteger(expressionClass) || isDouble(expressionClass);
    }

    public  static boolean isBoolean(Class<?> expressionClass) {
        return Boolean.class.isAssignableFrom(expressionClass);
    }

    public  static boolean isDate(Class<?> expressionClass) {
        return ZonedDateTime.class.isAssignableFrom(expressionClass);
    }

    public  static boolean oneOfIsNull(Object left, Object right) {
        return left == null || right == null;
    }

    public  static boolean bothAreNumbers(Class<?> leftClass, Class<?> rightClass) {
        return isNumber(leftClass) && isNumber(rightClass);
    }

    public  static boolean bothAreIntegers(Class<?> leftClass, Class<?> rightClass) {
        return isInteger(leftClass) && isInteger(rightClass);
    }

    public  static boolean bothAreStrings(Class<?> leftClass, Class<?> rightClass) {
        return isString(leftClass) && isString(rightClass);
    }

    public  static boolean bothAreBooleans(Class<?> leftClass, Class<?> rightClass) {
        return isBoolean(leftClass) && isBoolean(rightClass);
    }

    public  static int asInt(Object value) {
        return Integer.valueOf(value.toString());
    }

    public  static double asDouble(Object value) {
        return Double.valueOf(value.toString());
    }

    public  static String asString(Object value) {
        return String.valueOf(value);
    }

    public  static Boolean asBoolean(Object value) {
        return Boolean.valueOf(asString(value));
    }

    private ExpressionUtils() {}
    
}
