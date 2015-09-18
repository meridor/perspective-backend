package org.meridor.perspective.shell.repository.impl;

import java.util.*;
import java.util.stream.Collectors;

public final class TextUtils {

    private static final String SEMICOLON = ";";
    private static final String EQUALITY = "=";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    
    public static String enumerateValues(Set<String> values) {
        return values.stream().collect(Collectors.joining(COMMA + SPACE));
    }
    
    public static String joinLines(Set<String> values) {
        return values.stream().collect(Collectors.joining("\n"));
    }

    /**
     * Parses the following strings: key1 = value1, value2, value3; key2 = value4, value5
     * @param data a string of the specified format
     * @return a map with parsed keys and values
     */
    public static Map<String, Set<String>> parseAssignment(String data) {
        Map<String, Set<String>> ret = new HashMap<>();
        StringTokenizer stringTokenizer = new StringTokenizer(data, String.format("%s%s%s", EQUALITY, COMMA, SEMICOLON), true);
        String key = null;
        Set<String> value = new HashSet<>();
        int counter = 0;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (isValueDelimiter(token)) {
                continue;
            } else if (isKeyDelimiter(token) && key != null) {
                ret.put(key, value);
                counter = 0;
                key = null;
                continue;
            } else if (counter == 0) {
                key = token.trim();
            } else {
                value.add(token.trim());
            }
            counter++;
        }
        if (key != null) {
            ret.put(key, value);
        }
        return ret;
    }

    /**
     * Returns space separated collection of entries as an array
     * @param data space separated values
     * @return an set of values or null if null is passed
     */
    public static Set<String> parseEnumeration(String data) {
        if (data == null) {
            return null;
        }
        return new HashSet<>(Arrays.asList(data.split(SPACE)));
    }
    
    private static boolean isKeyDelimiter(String token) {
        return SEMICOLON.equals(token);
    }
    
    private static boolean isValueDelimiter(String token) {
        return EQUALITY.equals(token) || COMMA.equals(token);
    }
    
    private TextUtils() {
        
    }

}
