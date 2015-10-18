package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class TextUtils {

    private static final String SEMICOLON = ";";
    private static final String EQUALITY = "=";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String ENTER = "\n";
    private static final String YES = "y";
    private static final String NO = "n";
    private static final String NEXT = "n";
    private static final String LESS_PREVIOUS = "w";
    private static final String PREVIOUS = "p";
    private static final String QUIT = "q";
    private static final String NONE = "-";

    public static String replacePlaceholders(final String template, Map<Placeholder, String> values) {
        String ret = template;
        for (Placeholder placeholder : values.keySet()) {
            String placeholderValue = values.get(placeholder);
            if (placeholderValue != null) {
                ret = template.replace(getPlaceholder(placeholder), placeholderValue);
            }
        }
        return ret;
    }

    private static String getPlaceholder(Placeholder placeholder) {
        return String.format("$%s", placeholder.name().toLowerCase());
    }
    
    public static boolean containsPlaceholder(final String template, Placeholder placeholder) {
        return template.contains(getPlaceholder(placeholder));
    }

    public static String enumerateValues(Collection<String> values) {
        return values.stream().collect(Collectors.joining(COMMA + SPACE));
    }
    
    public static String joinLines(Collection<String> values) {
        return values.stream().collect(Collectors.joining("\n"));
    }

    public static String humanizedDuration(ZonedDateTime zonedDateTime) {
        Date currentDate = Date.from(zonedDateTime.toInstant());
        PrettyTime pt = new PrettyTime();
        return pt.format(currentDate);
    }
    
    /**
     * Parses the following strings: key1 = value1, value2, value3; key2 = value4, value5
     * @param data a string of the specified format
     * @return a map with parsed keys and values
     */
    public static Map<String, Set<String>> parseAssignment(String data) {
        Map<String, Set<String>> ret = new HashMap<>();
        if (data != null) {
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
        }
        return ret;
    }

    /**
     * Returns space separated collection of entries as an array
     * @param data space separated values
     * @return an set of values or null if null is passed
     */
    public static Set<String> parseEnumeration(String data) {
        return parseEnumeration(data, SPACE);
    }
    
    public static Set<String> parseEnumeration(String data, String delimiter) {
        if (data == null) {
            return null;
        }
        return new HashSet<>(Arrays.asList(data.split(delimiter)));
    }
    
    private static boolean isKeyDelimiter(String token) {
        return SEMICOLON.equals(token);
    }
    
    private static boolean isValueDelimiter(String token) {
        return EQUALITY.equals(token) || COMMA.equals(token);
    }
    
    public static boolean isNextElementKey(String key) {
        return ENTER.equals(key) || SPACE.equals(key) || NEXT.equals(key);
    }
    
    public static boolean isPrevElementKey(String key) {
        return PREVIOUS.equals(key) || LESS_PREVIOUS.equals(key);
    }
    
    public static boolean isExitKey(String key) {
        return QUIT.equals(key);
    }
    
    public static boolean isYesKey(String key) {
        return YES.equals(key);
    }
    
    public static boolean isNoKey(String key) {
        return NO.equals(key);
    }
    
    public static String[] instanceToRow(Instance instance) {
        return new String[]{
                instance.getName(),
                (instance.getImage() != null) ? instance.getImage().getName() : NONE,
                (instance.getFlavor() != null) ? instance.getFlavor().getName() : NONE,
                (instance.getState() != null) ?  instance.getState().value() : NONE,
                (instance.getTimestamp() != null) ? humanizedDuration(instance.getTimestamp()) : NONE
        };
    }
    
    public static String[] imageToRow(Image image) {
        return new String[]{
                image.getName(),
                (image.getState() != null) ? image.getState().value() : NONE,
                (image.getTimestamp() != null) ? humanizedDuration(image.getTimestamp()) : NONE
        };
    }
    
    public static boolean isNumericKey(String key) {
        if (key == null) {
            return false;
        }
        int length = key.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (key.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = key.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }
    
    private TextUtils() {
        
    }

}
