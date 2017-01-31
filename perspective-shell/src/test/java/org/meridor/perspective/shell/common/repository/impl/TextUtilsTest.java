package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.*;

public class TextUtilsTest {

    @Test
    public void testReplacePlaceholders() {
        String stringWithPlaceholder = "start-$defaultAnswer-$name-end";
        Map<Placeholder, String> replacementMap = new HashMap<Placeholder, String>() {
            {
                put(Placeholder.DEFAULT_ANSWER, "1");
                put(Placeholder.NAME, "test");
            }
        };
        assertThat(replacePlaceholders(stringWithPlaceholder, replacementMap), equalTo("start-1-test-end"));
    }

    @Test
    public void testGetPlaceholder() {
        assertThat(getPlaceholder(Placeholder.NUMBER), equalTo("$number"));
        assertThat(getPlaceholder(Placeholder.DEFAULT_ANSWER), equalTo("$defaultAnswer"));
    }

    @Test
    public void testUnderscoreToLowerCamel() {
        assertThat(underscoreToLowerCamel("under_score"), equalTo("underScore"));
    }

    @Test
    public void testUnderscoreToUpperCamel() {
        assertThat(underscoreToUpperCamel("under_score"), equalTo("UnderScore"));
    }

    @Test
    public void testContainsPlaceholder() {
        String template = "start-$name-end";
        assertThat(containsPlaceholder(template, Placeholder.NAME), is(true));
        assertThat(containsPlaceholder(template, Placeholder.NUMBER), is(false));
    }

    @Test
    public void testEnumerateValues() {
        Set<String> values = new LinkedHashSet<>(Arrays.asList("value1", "value2"));
        assertThat(enumerateValues(values), equalTo("value1, value2"));
        assertThat(enumerateValues(values, "#"), equalTo("value1#value2"));
        assertThat(enumerateValues(Collections.singletonList("value")), equalTo("value"));
    }

    @Test
    public void testJoinLines() {
        List<String> lines = Arrays.asList("line1", "line2");
        String joinedLines = joinLines(lines);
        assertThat(joinedLines, is(notNullValue()));
        List<String> splitLines = Arrays.asList(joinedLines.split("\\r?\\n"));
        assertThat(splitLines, hasSize(2));
        assertThat(splitLines.get(0), equalTo("line1"));
        assertThat(splitLines.get(1), equalTo("line2"));
        assertThat(joinLines(Collections.singletonList("line")), equalTo("line"));
    }

    @Test
    public void testCreateAssignment() {
        Map<String, Set<String>> data = new HashMap<>();
        data.put("key1", new HashSet<>(Arrays.asList("value11", "value12")));
        data.put("key2", new HashSet<>(Collections.singletonList("value21")));
        assertThat(createAssignment(data), equalTo("key1=value11,value12;key2=value21"));
    }

    @Test
    public void testParseAssignment() {
        assertThat(parseAssignment(null).keySet(), empty());

        Map<String, Set<String>> parsedEmptyValueAssignment = parseAssignment("key1 = ");
        assertThat(parsedEmptyValueAssignment.keySet(), contains("key1"));
        assertThat(parsedEmptyValueAssignment.get("key1"), empty());

        String assignmentString = "key1 = value11, value12; key2=value21,value22";
        Map<String, Set<String>> assignmentData = parseAssignment(assignmentString);
        assertThat(assignmentData.keySet(), hasSize(2));
        assertThat(assignmentData.keySet(), containsInAnyOrder("key1", "key2"));
        assertThat(assignmentData.get("key1"), containsInAnyOrder("value11", "value12"));
        assertThat(assignmentData.get("key2"), containsInAnyOrder("value21", "value22"));
    }

    @Test
    public void testParseRange() {
        assertThat(parseRange(null), empty());
        assertThat(parseRange(""), empty());

        String range = "1,3- 5, 7 -9";
        assertThat(parseRange(range), contains(1, 3, 4, 5, 7, 8, 9));
    }

    @Test
    public void testIsRange() {
        assertThat(isRange(null), is(false));
        assertThat(isRange(""), is(false));
        assertThat(isRange("1"), is(true));
        assertThat(isRange("1,3- 5, 7 -9"), is(true));
        assertThat(isRange("#$%#$%#$"), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIllegalRange() throws Exception {
        parseRange("#$%-");
    }

    @Test
    public void testParseEnumeration() {
        String value = "value1, value2";
        Set<String> enumeration = parseEnumeration(value);
        assertThat(enumeration, hasSize(2));
        assertThat(enumeration, containsInAnyOrder("value1", "value2"));
    }

    @Test
    public void testParseEnumerationCustomDelimiter() {
        String value = "value1##value2";
        Set<String> enumeration = parseEnumeration(value, "#");
        assertThat(enumeration, hasSize(2));
        assertThat(enumeration, containsInAnyOrder("value1", "value2"));
    }

    @Test
    public void testIsFirstElementKey() {
        assertThat(isFirstElementKey("g"), is(true));
        assertThat(isFirstElementKey("y"), is(false));
    }

    @Test
    public void testIsLastElementKey() {
        assertThat(isLastElementKey("G"), is(true));
        assertThat(isLastElementKey("y"), is(false));
    }

    @Test
    public void testIsNextElementKey() {
        assertThat(isNextElementKey("n"), is(true));
        assertThat(isNextElementKey(" "), is(true));
        assertThat(isNextElementKey("\n"), is(true));
        assertThat(isNextElementKey("y"), is(false));
    }

    @Test
    public void testIsPrevElementKey() {
        assertThat(isPrevElementKey("p"), is(true));
        assertThat(isPrevElementKey("w"), is(true));
        assertThat(isPrevElementKey("y"), is(false));
    }

    @Test
    public void testIsExitKey() {
        assertThat(isExitKey("q"), is(true));
        assertThat(isExitKey("y"), is(false));
    }

    @Test
    public void testIsSkipKey() {
        assertThat(isSkipKey("s"), is(true));
        assertThat(isSkipKey("y"), is(false));
    }

    @Test
    public void testIsYesKey() {
        assertThat(isYesKey("y"), is(true));
        assertThat(isYesKey("n"), is(false));
    }

    @Test
    public void testIsNoKey() {
        assertThat(isNoKey("n"), is(true));
        assertThat(isNoKey("y"), is(false));
    }

    @Test
    public void testIsRepeatKey() {
        assertThat(isRepeatKey("r"), is(true));
        assertThat(isRepeatKey("y"), is(false));
    }

    @Test
    public void testIsNumericKey() {
        assertThat(isNumericKey("1"), is(true));
        assertThat(isNumericKey("a"), is(false));
        assertThat(isNumericKey(" "), is(false));
        assertThat(isNumericKey(""), is(false));
        assertThat(isNumericKey("-"), is(false));
    }

    @Test
    public void testIsPositiveInt() {
        assertThat(isPositiveInt("1"), is(true));
        assertThat(isPositiveInt("0"), is(false));
        assertThat(isPositiveInt("-1"), is(false));
        assertThat(isPositiveInt("a"), is(false));
        assertThat(isPositiveInt(" "), is(false));
    }

    @Test
    public void testQuoteIfNeeded() {
        assertThat(quoteIfNeeded(null), is(nullValue()));
        assertThat(quoteIfNeeded("word"), equalTo("word"));
        assertThat(quoteIfNeeded("two words"), equalTo("'two words'"));
    }

    @Test
    public void testGetAsExactMatch() {
        assertThat(getAsExactMatch("text"), equalTo("^text$"));
    }

    @Test
    public void testRemoveSuffixes() {
        assertThat(removeSuffixes(null, Collections.emptySet()), is(nullValue()));
        assertThat(removeSuffixes(Collections.emptySet(), null), is(empty()));
        assertThat(removeSuffixes(Collections.emptySet(), Collections.emptySet()), is(empty()));

        List<String> seed = Arrays.asList("%example.com%", "^test.net$", "touch", "click.net", "three", "%a%", "^b$");
        List<String> suffixes = Arrays.asList(".com", ".net", "a", "b");
        Collection<String> output = removeSuffixes(seed, suffixes);

        assertThat(output, hasSize(7));
        assertThat(output, contains("%example%", "^test$", "touch", "^click$", "three", "%%", "^$"));
    }

    @Test
    public void testRouteByKey() throws IOException {
        List<Exception> exceptions = new ArrayList<>();
        List<String> invalidKeys = new ArrayList<>();
        Consumer<Exception> onException = exceptions::add;
        Consumer<String> onInvalidKey = invalidKeys::add;
        Map<Predicate<String>, Function<String, Boolean>> routes = new LinkedHashMap<Predicate<String>, Function<String, Boolean>>() {
            {
                put(k -> k.equals("y"), k -> true);
                put(k -> k.equals("n"), k -> false);
                put(k -> k.equals("e"), k -> Boolean.TRUE.compareTo(null) > 0); //Always throws NPE
            }
        };

        Optional<Boolean> routeWithYes = routeByKey(mockConsoleReader("y"), routes, onInvalidKey, onException);
        assertTrue(routeWithYes.isPresent());
        assertThat(routeWithYes.get(), equalTo(true));

        Optional<Boolean> routeWithNo = routeByKey(mockConsoleReader("n"), routes, onInvalidKey, onException);
        assertTrue(routeWithNo.isPresent());
        assertThat(routeWithNo.get(), equalTo(false));

        routeByKey(mockConsoleReader("iy"), routes, onInvalidKey, onException);
        assertThat(invalidKeys, contains("i"));

        routeByKey(mockConsoleReader("e"), routes, onInvalidKey, onException);
        assertThat(exceptions, hasSize(1));

    }

}