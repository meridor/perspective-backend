package org.meridor.perspective.sql.impl.expression;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ExpressionUtilsTest {
    
    @Test
    public void mergeFixedValueConditions() throws Exception {
        Map<String, Set<Object>> left = new HashMap<String, Set<Object>>(){
            {
                put("first", Collections.singleton("one"));
            }
        }; 
        Map<String, Set<Object>> right = new HashMap<String, Set<Object>>(){
            {
                put("first", Collections.singleton("two"));
                put("second", Collections.singleton("three"));
            }
        };
        Map<String, Set<Object>> result = ExpressionUtils.mergeFixedValueConditions(left, right);
        assertThat(result, equalTo(new HashMap<String, Set<Object>>(){
            {
                put("first", new HashSet<>(Arrays.asList("one", "two")));
                put("second", Collections.singleton("three"));
            }
        }));
    }

}