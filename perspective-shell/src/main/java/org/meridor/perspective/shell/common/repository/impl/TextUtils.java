package org.meridor.perspective.shell.common.repository.impl;

import com.google.common.base.CaseFormat;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TextUtils {

    private static final String SEMICOLON = ";";
    private static final String EQUALITY = "=";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String ENTER = "\n";
    public static final String YES = "y";
    public static final String NO = "n";
    private static final String FIRST = "g";
    private static final String LAST = "G";
    private static final String NEXT = "n";
    private static final String LESS_PREVIOUS = "w";
    private static final String PREVIOUS = "p";
    private static final String SKIP = "s";
    public static final String QUIT = "q";
    private static final String REPEAT = "r";
    public static final String DASH = "-";

    public static String replacePlaceholders(final String template, Map<Placeholder, String> values) {
        String ret = template;
        for (Placeholder placeholder : values.keySet()) {
            String placeholderValue = values.get(placeholder);
            if (placeholderValue != null) {
                ret = ret.replace(getPlaceholder(placeholder), placeholderValue);
            }
        }
        return ret;
    }

    public static String getPlaceholder(Placeholder placeholder) {
        return String.format("$%s", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, placeholder.name()));
    }
    
    public static boolean containsPlaceholder(final String template, Placeholder placeholder) {
        return template.contains(getPlaceholder(placeholder));
    }

    public static String enumerateValues(Collection<String> values) {
        return enumerateValues(values, COMMA + SPACE);
    }
    
    public static String enumerateValues(Collection<String> values, String delimiter) {
        return values.stream().collect(Collectors.joining(delimiter));
    }
    
    public static String joinLines(Collection<String> values) {
        return values.stream().collect(Collectors.joining("\n"));
    }

    public static String humanizedDuration(ZonedDateTime zonedDateTime) {
        Date currentDate = Date.from(zonedDateTime.toInstant());
        PrettyTime pt = new PrettyTime();
        return pt.format(currentDate);
    }
    
    public static String createAssignment(Map<String, Set<String>> data) {
        return enumerateValues(
                data.keySet().stream()
                .map(k -> String.format("%s%s%s", k, EQUALITY, enumerateValues(data.get(k), COMMA)))
                .collect(Collectors.toList()),
                SEMICOLON
        );
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
                    value = new HashSet<>();
                    continue;
                } else if (counter == 0) {
                    key = token.trim();
                } else {
                    String trimmedValue = token.trim();
                    if (!StringUtils.isEmpty(trimmedValue)) {
                        value.add(trimmedValue);
                    }
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
     * Parses the following string: 1, 3-5, 8-10
     * @param range a string of the range format
     * @return a set of numbers corresponding to given string
     */
    public static Set<Integer> parseRange(String range) {
        if (range == null) {
            return Collections.emptySet();
        }
        Set<Integer> ret = new LinkedHashSet<>();
        StringTokenizer stringTokenizer = new StringTokenizer(range, String.format("%s%s", COMMA, DASH), true);
        Integer from = null;
        Integer to = null;
        boolean rangeStarted = false;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (isValueDelimiter(token)) {
                ret.addAll(fromToAsSet(from, to));
                from = null;
                to = null;
                rangeStarted = false;
            } else if (isRangeDelimiter(token)) {
                rangeStarted = true;
            } else {
                String trimmedValue = token.trim();
                if (!isPositiveInt(trimmedValue)) {
                    throw new IllegalArgumentException("Range values should be positive numbers.");
                }
                Integer parsedInteger = Integer.parseUnsignedInt(trimmedValue);
                if (rangeStarted) {
                    to = parsedInteger;
                } else {
                    from = parsedInteger;
                }
            }
            if (from != null) {
                ret.addAll(fromToAsSet(from, to));
            }
        }

        return ret;
    }
    
    private static Set<Integer> fromToAsSet(Integer from, Integer to) {
        Set<Integer> ret = new LinkedHashSet<>();
        if (to == null) {
            ret.add(from);
        } else {
            for (int i = from; i <= to; i++) {
                ret.add(i);
            }
        }
        return ret;
    }

    /**
     * Returns whether passed value is a range
     * @param value string to process
     * @return true if range, false otherwise
     */
    public static boolean isRange(String value) {
        if (value == null) {
            return false;
        }
        try {
            Set<Integer> values = parseRange(value);
            return values.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Returns command separated collection of entries as an array
     * @param data comma separated values
     * @return an set of values or null if null is passed
     */
    public static Set<String> parseEnumeration(String data) {
        return parseEnumeration(data, COMMA);
    }
    
    public static Set<String> parseEnumeration(String data, String delimiter) {
        if (data == null) {
            return null;
        }
        return new HashSet<>(
                Arrays.stream(data.split(delimiter))
                        .filter(el -> !el.isEmpty())
                        .map(String::trim)
                .collect(Collectors.toList())
        );
    }
    
    private static boolean isKeyDelimiter(String token) {
        return SEMICOLON.equals(token);
    }
    
    private static boolean isValueDelimiter(String token) {
        return EQUALITY.equals(token) || COMMA.equals(token);
    }

    public static boolean isRangeDelimiter(String key) {
        return DASH.equals(key);
    }

    public static boolean isFirstElementKey(String key) {
        return FIRST.equals(key);
    }

    public static boolean isLastElementKey(String key) {
        return LAST.equals(key);
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
    
    public static boolean isSkipKey(String key) {
        return SKIP.equals(key);
    }

    public static boolean isYesKey(String key) {
        return YES.equals(key);
    }
    
    public static boolean isNoKey(String key) {
        return NO.equals(key);
    }
    
    public static boolean isRepeatKey(String key) {
        return REPEAT.equals(key);
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
    
    public static boolean isPositiveInt(String key) {
        try {
            return Integer.parseUnsignedInt(key) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String quoteIfNeeded(String value) {
        if (value == null) {
            return null;
        }
        return value.contains(SPACE) ? String.format("'%s'", value) : value;
    }

    private static boolean isExactMatch(String expression) {
        return expression != null && expression.startsWith("^") && expression.endsWith("$");
    }
    
    private static boolean isContainsMatch(String expression) {
        return expression != null && expression.startsWith("%") && expression.endsWith("%");
    }

    public static String getAsExactMatch(String value) {
        return String.format("^%s$", value);
    }
    
    public static Collection<String> removeSuffixes(Collection<String> input, Collection<String> suffixes) {
        if (input == null) {
            return null;
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }
        if (suffixes == null || suffixes.isEmpty()) {
            return input;
        }
        return input.stream()
            .map(str -> {
                Optional<String> matchingSuffix = suffixes.stream()
                        .filter(s -> 
                                str.endsWith(s) ||
                                isContainsMatch(str) && str.endsWith(s + "%") ||
                                isExactMatch(str) && str.endsWith(s + "$")
                        )
                        .findFirst();
                if (matchingSuffix.isPresent()) {
                    int strLength = str.length();
                    int suffixLength = matchingSuffix.get().length();
                    char lastChar = str.charAt(str.length() - 1);
                    boolean hasSpecialLastChar = lastChar == '$' || lastChar == '%';
                    int end = strLength - suffixLength;
                    if (hasSpecialLastChar) {
                        end--;
                    }
                    String substr = str.substring(0, end);
                    return hasSpecialLastChar ?
                            substr + lastChar :
                            getAsExactMatch(substr);
                }
                return str;
            })
            .collect(Collectors.toList());
    }

    public static <T> Optional<T> routeByKey(Map<Predicate<String>, Function<String, T>> routes) {
        return routeByKey(null, routes, null, null);
    }
    
    public static <T> Optional<T> routeByKey(Map<Predicate<String>, Function<String, T>> routes, Consumer<String> onInvalidKey) {
        return routeByKey(null, routes, onInvalidKey, null);
    }
    
    public static <T> Optional<T> routeByKey(Map<Predicate<String>, Function<String, T>> routes, Consumer<String> onInvalidKey, Consumer<Exception> onException) {
        return routeByKey(null, routes, onInvalidKey, onException);
    }
    
    static <T> Optional<T> routeByKey(ConsoleReader consoleReader, Map<Predicate<String>, Function<String, T>> routes, Consumer<String> onInvalidKey, Consumer<Exception> onException) {
        try {
            if (consoleReader == null) {
                consoleReader = new ConsoleReader();
            }
            while (true) {
                String key = String.valueOf((char) consoleReader.readCharacter());
                for (Predicate<String> keyPredicate : routes.keySet()) {
                    if (keyPredicate.test(key)) {
                        return Optional.ofNullable(routes.get(keyPredicate).apply(key));
                    }
                }
                if (onInvalidKey != null) {
                    onInvalidKey.accept(key);
                } else {
                    return Optional.empty();
                }
            }
        } catch (Exception e) {
            if (onException != null) {
                onException.accept(e);
            }
            return Optional.empty();
        }
    }

    public static String getVersion() {
        Optional<String> version = Optional.ofNullable(TextUtils.class.getPackage().getImplementationVersion());
        return version.isPresent() ? version.get() : "devel";
    }
    
}
