package org.meridor.perspective.shell.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RelationCheckerTest {

    @Parameterized.Parameters(name = "{0} {2} {1} should return {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { 1, 1, NumberRelation.EQUAL, true },
                { 2, 1, NumberRelation.EQUAL, false },
                { 2, 1, NumberRelation.GREATER_THAN, true },
                { 1, 2, NumberRelation.GREATER_THAN, false },
                { 1, 2, NumberRelation.LESS_THAN, true },
                { 2, 1, NumberRelation.LESS_THAN, false },
                { 1, 1, NumberRelation.GREATER_THAN_EQUAL, true },
                { 2, 1, NumberRelation.GREATER_THAN_EQUAL, true },
                { 1, 2, NumberRelation.GREATER_THAN_EQUAL, false },
                { 1, 1, NumberRelation.LESS_THAN_EQUAL, true },
                { 1, 2, NumberRelation.LESS_THAN_EQUAL, true },
                { 2, 1, NumberRelation.LESS_THAN_EQUAL, false },
                { 1, 2, NumberRelation.NOT_EQUAL, true },
                { 1, 1, NumberRelation.NOT_EQUAL, false },
        });
    }
    
    private final double first;
    private final double second;
    private final NumberRelation relation;
    private final boolean result;

    public RelationCheckerTest(double first, double second, NumberRelation relation, boolean result) {
        this.first = first;
        this.second = second;
        this.relation = relation;
        this.result = result;
    }

    @Test
    public void testCheckDoubleRelation() throws Exception {
        RelationChecker relationChecker = new RelationChecker();
        assertThat(relationChecker.checkDoubleRelation(first, second, relation), is(result));
    }
    
}