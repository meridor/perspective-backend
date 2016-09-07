package org.meridor.perspective.sql;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.meridor.perspective.sql.PatternUtils.*;

public class PatternUtilsTest {
    
    @Test
    public void testIsContainsMatch() {
        assertThat(isContainsMatch("%something%"), is(true));
        assertThat(isContainsMatch("anything"), is(false));
    }
    
    @Test
    public void testIsExactMatch() {
        assertThat(isExactMatch("^something$"), is(true));
        assertThat(isExactMatch("anything"), is(false));
    }

    @Test
    public void testIsRegex() {
        assertThat(isRegex("regex.*"), is(true));
        assertThat(isRegex("(wrong"), is(false));
    }
    
    @Test
    public void testRemoveFirstAndLastChars() {
        assertThat(removeFirstAndLastChars("1234"), equalTo("23"));
        assertThat(removeFirstAndLastChars("1"), equalTo("1"));
        assertThat(removeFirstAndLastChars(""), equalTo(""));
        assertThat(removeFirstAndLastChars(null), is(nullValue()));
    }

    
    
}