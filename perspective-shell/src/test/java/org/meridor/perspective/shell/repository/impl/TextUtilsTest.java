package org.meridor.perspective.shell.repository.impl;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
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
        assertThat(containsPlaceholder(template, Placeholder.NAME), is(true));
        assertThat(containsPlaceholder(template, Placeholder.NUMBER), is(false));
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
        assertThat(joinedLines, is(notNullValue()));
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
    public void testParseEnumeration(){
        String value = "value1, value2";
        Set<String> enumeration = parseEnumeration(value);
        assertThat(enumeration, hasSize(2));
        assertThat(enumeration, containsInAnyOrder("value1", "value2"));
    }
    
    @Test
    public void testParseEnumerationCustomDelimiter(){
        String value = "value1##value2";
        Set<String> enumeration = parseEnumeration(value, "#");
        assertThat(enumeration, hasSize(2));
        assertThat(enumeration, containsInAnyOrder("value1", "value2"));
    }

    @Test
    public void testIsFirstElementKey(){
        assertThat(isFirstElementKey("g"), is(true));
        assertThat(isFirstElementKey("y"), is(false));
    }
    
    @Test
    public void testIsLastElementKey(){
        assertThat(isLastElementKey("G"), is(true));
        assertThat(isLastElementKey("y"), is(false));
    }
    
    @Test
    public void testIsNextElementKey(){
        assertThat(isNextElementKey("n"), is(true));
        assertThat(isNextElementKey(" "), is(true));
        assertThat(isNextElementKey("\n"), is(true));
        assertThat(isNextElementKey("y"), is(false));
    }

    @Test
    public void testIsPrevElementKey(){
        assertThat(isPrevElementKey("p"), is(true));
        assertThat(isPrevElementKey("w"), is(true));
        assertThat(isPrevElementKey("y"), is(false));
    }

    @Test
    public void testIsExitKey(){
        assertThat(isExitKey("q"), is(true));
        assertThat(isExitKey("y"), is(false));
    }
    
    @Test
    public void testIsSkipKey(){
        assertThat(isSkipKey("s"), is(true));
        assertThat(isSkipKey("y"), is(false));
    }

    @Test
    public void testIsYesKey(){
        assertThat(isYesKey("y"), is(true));
        assertThat(isYesKey("n"), is(false));
    }

    @Test
    public void testIsNoKey(){
        assertThat(isNoKey("n"), is(true));
        assertThat(isNoKey("y"), is(false));
    }
    
    @Test
    public void testIsRepeatKey(){
        assertThat(isRepeatKey("r"), is(true));
        assertThat(isRepeatKey("y"), is(false));
    }

    @Test
    public void testIsNumericKey(){
        assertThat(isNumericKey("1"), is(true));
        assertThat(isNumericKey("a"), is(false));
        assertThat(isNumericKey(" "), is(false));
    }

    @Test
    public void testIsPositiveInt(){
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
    public void testOneOfMatches() throws Exception {
        final String TEXT = "test-string";
        assertThat(oneOfMatches(null, Collections.emptySet()), is(false));
        assertThat(oneOfMatches(TEXT, Collections.singletonList("test-string")), is(true));
        assertThat(oneOfMatches(TEXT, Arrays.asList("missing", "test")), is(true));
        assertThat(oneOfMatches(TEXT, Collections.singletonList("missing")), is(false));
    }
    
    @Test
    public void testMatches() throws Exception {
        final String CANDIDATE = "candidate";
        
        assertThat(matches(null, "something"), is(false));
        assertThat(matches(CANDIDATE, null), is(false));
        assertThat(matches(CANDIDATE, "missing"), is(false));
        
        //Contains match
        assertThat(matches(CANDIDATE, "%cand%"), is(true));
        assertThat(matches(CANDIDATE, "%date%"), is(true));
        
        //Exact match
        assertThat(matches(CANDIDATE, "^candidate$"), is(true));
        assertThat(matches(CANDIDATE, "^cand$"), is(false));
        
        //Regex match
        assertThat(matches(CANDIDATE, "^candidate$"), is(true));
        assertThat(matches(CANDIDATE, "candidate"), is(true));
        assertThat(matches(CANDIDATE, "cand.*"), is(true));
        assertThat(matches(CANDIDATE, ".*did.*"), is(true));
        assertThat(matches(CANDIDATE, "ded"), is(false));
    }
    
    @Test
    public void testGetAsExactMatch() {
        assertThat(getAsExactMatch("text"), equalTo("^text$"));
    }
    
}