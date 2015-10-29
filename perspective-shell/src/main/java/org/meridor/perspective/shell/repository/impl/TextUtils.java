package org.meridor.perspective.shell.repository.impl;

import com.google.common.collect.Lists;
import jline.console.ConsoleReader;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.commands.BaseCommands;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.validator.Setting;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.misc.LoggingUtils.error;

public final class TextUtils {

    private static final String SEMICOLON = ";";
    private static final String EQUALITY = "=";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String ENTER = "\n";
    private static final String YES = "y";
    public static final String NO = "n";
    private static final String NEXT = "n";
    private static final String LESS_PREVIOUS = "w";
    private static final String PREVIOUS = "p";
    private static final String QUIT = "q";
    private static final String NONE = "-";
    public static final Integer DEFAULT_PAGE_SIZE = 20;

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
        return String.format("$%s", placeholder.name().toLowerCase());
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
    
    public static boolean isPositiveInt(String key) {
        try {
            return Integer.parseUnsignedInt(key) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private TextUtils() {
        
    }

    /**
     * Shows pages one by one allowing to navigate back and forth
     * @param pages each page contents
     */
    public static void page(List<String> pages) {
        try {
            ConsoleReader consoleReader = new ConsoleReader();
            final int NUM_PAGES = pages.size();
            int currentPage = 1;
            showPage(currentPage, NUM_PAGES, pages); //Always showing first page
            while (currentPage <= NUM_PAGES) {
                boolean pageNumberChanged = false;
                String key = String.valueOf((char) consoleReader.readCharacter());
                if (isExitKey(key)) {
                    break;
                } else if (isNextElementKey(key)) {
                    if (currentPage == NUM_PAGES) {
                        break;
                    }
                    currentPage++;
                    pageNumberChanged = true;
                } else if (isPrevElementKey(key) && currentPage > 1) {
                    currentPage--;
                    pageNumberChanged = true;
                } else if (isNumericKey(key)) {
                    Integer pageNumber = Integer.valueOf(key);
                    if (pageNumber < 1 || pageNumber > NUM_PAGES) {
                        BaseCommands.warn(String.format("Wrong page number: %d. Should be one of 1..%d.", pageNumber, NUM_PAGES));
                        continue;
                    } else if (pageNumber != currentPage) {
                        currentPage = pageNumber;
                        pageNumberChanged = true;
                    }
                }
                if (pageNumberChanged) {
                    showPage(currentPage, NUM_PAGES, pages);
                }
            }
        } catch (IOException e) {
            error(String.format("Failed to show pages: %s", e.getMessage()));
        }
    }

    private static void showPage(final int pageNumber, final int numPages, List<String> entries) {
        BaseCommands.ok(String.format("Showing page %d of %d:", pageNumber, numPages));
        BaseCommands.ok(entries.get(pageNumber - 1));
    }

    public static Integer getPageSize(SettingsStorage settingsStorage) {
        return (settingsStorage.hasSetting(Setting.PAGE_SIZE)) ?
                settingsStorage.getSettingAs(Setting.PAGE_SIZE, Integer.class) :
                DEFAULT_PAGE_SIZE;
    }

    public static List<String> preparePages(TableRenderer tableRenderer, Integer pageSize, String[] columns, List<String[]> rows) {
        return Lists.partition(rows, pageSize).stream().
                map(b -> tableRenderer.render(columns, b))
                .collect(Collectors.toList());
    }
}
