package org.meridor.perspective.shell.repository.impl;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

public class TextUtilsTest {

    @Test
    public void testReplacePlaceholders(){
        String stringWithPlaceholder = "start-$number-$name-end";
        Map<Placeholder, String> replacementMap = new HashMap<Placeholder, String>(){
            {
                put(Placeholder.NUMBER, "1");
                put(Placeholder.NAME, "test");
            }
        };
        assertThat(replacePlaceholders(stringWithPlaceholder, replacementMap), equalTo("start-1-test-end"));
    }

    @Test
    public void testGetPlaceholder(){
        assertThat(getPlaceholder(Placeholder.NUMBER), equalTo("$number"));
    }

    @Test
    public void testContainsPlaceholder(){
        String template = "start-$name-end";
        assertTrue(containsPlaceholder(template, Placeholder.NAME));
        assertFalse(containsPlaceholder(template, Placeholder.NUMBER));
    }

    @Test
    public void testEnumerateValues(){
        Set<String> values = new LinkedHashSet<>(Arrays.asList("value1", "value2"));
        assertThat(enumerateValues(values), equalTo("value1, value2"));
        assertThat(enumerateValues(values, "#"), equalTo("value1#value2"));
        assertThat(enumerateValues(Collections.singletonList("value")), equalTo("value"));
    }

    @Test
    public void testJoinLines(){
        List<String> lines = Arrays.asList("line1", "line2");
        String joinedLines = joinLines(lines);
        assertNotNull(joinedLines);
        List<String> splittedLines = Arrays.asList(joinedLines.split("\\r?\\n"));
        assertThat(splittedLines, hasSize(2));
        assertThat(splittedLines.get(0), equalTo("line1"));
        assertThat(splittedLines.get(1), equalTo("line2"));
        assertThat(joinLines(Collections.singletonList("line")), equalTo("line"));
    }

    @Test
    public void testCreateAssignment(){
        Map<String, Set<String>> data = new HashMap<>();
        data.put("key1", new HashSet<>(Arrays.asList("value11", "value12")));
        data.put("key2", new HashSet<>(Collections.singletonList("value21")));
        assertThat(createAssignment(data), equalTo("key1=value11,value12;key2=value21"));
    }

    @Test
    public void testParseAssignment(){
        assertThat(parseAssignment(null).keySet(), empty());
        
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
        assertFalse(isRange(null));
        assertFalse(isRange(""));
        assertTrue(isRange("1"));
        assertTrue(isRange("1,3- 5, 7 -9"));
        assertFalse(isRange("#$%#$%#$"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIllegalRange() throws Exception {
        parseRange("#$%-");
    }

    @Test
    public void testParseEnumeration(){
        String value = "value1 value2";
        Set<String> enumeration = parseEnumeration(value);
        assertThat(enumeration, hasSize(2));
        assertThat(enumeration, containsInAnyOrder("value1", "value2"));
    }
    
    @Test
    public void testParseEnumerationCustomDelimiter(){
        String value = "value1#value2";
        Set<String> enumeration = parseEnumeration(value, "#");
        assertThat(enumeration, hasSize(2));
        assertThat(enumeration, containsInAnyOrder("value1", "value2"));
    }

    @Test
    public void testIsNextElementKey(){
        assertTrue(isNextElementKey("n"));
        assertTrue(isNextElementKey(" "));
        assertTrue(isNextElementKey("\n"));
        assertFalse(isNextElementKey("y"));
    }

    @Test
    public void testIsPrevElementKey(){
        assertTrue(isPrevElementKey("p"));
        assertTrue(isPrevElementKey("w"));
        assertFalse(isPrevElementKey("y"));
    }

    @Test
    public void testIsExitKey(){
        assertTrue(isExitKey("q"));
        assertFalse(isExitKey("y"));
    }

    @Test
    public void testIsYesKey(){
        assertTrue(isYesKey("y"));
        assertFalse(isYesKey("n"));
    }

    @Test
    public void testIsNoKey(){
        assertTrue(isNoKey("n"));
        assertFalse(isNoKey("y"));
    }

    @Test
    public void testIsNumericKey(){
        assertTrue(isNumericKey("1"));
        assertFalse(isNumericKey("a"));
        assertFalse(isNumericKey(" "));
    }

    @Test
    public void testIsPositiveInt(){
        assertTrue(isPositiveInt("1"));
        assertFalse(isPositiveInt("0"));
        assertFalse(isPositiveInt("-1"));
        assertFalse(isPositiveInt("a"));
        assertFalse(isPositiveInt(" "));
    }

}