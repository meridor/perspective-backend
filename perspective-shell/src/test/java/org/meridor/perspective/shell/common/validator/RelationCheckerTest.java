package org.meridor.perspective.shell.common.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.beans.BooleanRelation;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RelationCheckerTest {

    @Parameterized.Parameters(name = "{0} {2} {1} should return {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1, 1, BooleanRelation.EQUAL, true},
                {2, 1, BooleanRelation.EQUAL, false},
                {2, 1, BooleanRelation.GREATER_THAN, true},
                {1, 2, BooleanRelation.GREATER_THAN, false},
                {1, 2, BooleanRelation.LESS_THAN, true},
                {2, 1, BooleanRelation.LESS_THAN, false},
                {1, 1, BooleanRelation.GREATER_THAN_EQUAL, true},
                {2, 1, BooleanRelation.GREATER_THAN_EQUAL, true},
                {1, 2, BooleanRelation.GREATER_THAN_EQUAL, false},
                {1, 1, BooleanRelation.LESS_THAN_EQUAL, true},
                {1, 2, BooleanRelation.LESS_THAN_EQUAL, true},
                {2, 1, BooleanRelation.LESS_THAN_EQUAL, false},
                {1, 2, BooleanRelation.NOT_EQUAL, true},
                {1, 1, BooleanRelation.NOT_EQUAL, false},
        });
    }

    private final double first;
    private final double second;
    private final BooleanRelation relation;
    private final boolean result;

    public RelationCheckerTest(double first, double second, BooleanRelation relation, boolean result) {
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