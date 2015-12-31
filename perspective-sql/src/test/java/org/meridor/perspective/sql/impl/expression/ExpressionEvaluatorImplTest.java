package org.meridor.perspective.sql.impl.expression;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ExpressionEvaluatorImplTest {
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    
    private static final String STRING_COLUMN_NAME = "str";
    private static final String STRING_COLUMN_VALUE = "test";
    private static final String NUMERIC_COLUMN_NAME = "num";
    private static final Float NUMERIC_COLUMN_VALUE = -3f;
    private static final String NUMERIC_COLUMN_NAME_WITH_DEFAULT_VALUE = "numWithDefaultValue";
    private static final String COLUMN_WITH_MISSING_DEFAULT_VALUE = "missingDefaultValue";
    private static final String TABLE_NAME = "mock";
    
    private static final DataRow EMPTY_ROW = new DataRow();
    private static final DataRow ROW_WITH_VALUES = new DataRow(){
        {
            put(STRING_COLUMN_NAME, STRING_COLUMN_VALUE);
            put(NUMERIC_COLUMN_NAME, NUMERIC_COLUMN_VALUE);
        }
    };
    
    @Test
    public void testEvaluateColumnExpression() {
        ColumnExpression columnExpression = new ColumnExpression(STRING_COLUMN_NAME, TABLE_NAME);
        Object value = expressionEvaluator.evaluate(columnExpression, ROW_WITH_VALUES);
        assertThat(value, is(instanceOf(String.class)));
        assertThat(value, equalTo(STRING_COLUMN_VALUE));
    }
    
    @Test
    public void testEvaluateColumnExpressionWithDefaultValue() {
        ColumnExpression columnExpression = new ColumnExpression(NUMERIC_COLUMN_NAME_WITH_DEFAULT_VALUE, TABLE_NAME);
        Object value = expressionEvaluator.evaluate(columnExpression, ROW_WITH_VALUES);
        assertThat(value,is(instanceOf(Integer.class)));
        assertThat(value, equalTo(42));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateColumnExpressionWithMissingValue() {
        ColumnExpression columnExpression = new ColumnExpression(COLUMN_WITH_MISSING_DEFAULT_VALUE, TABLE_NAME);
        expressionEvaluator.evaluate(columnExpression, ROW_WITH_VALUES);
    }
    
    @Test
    public void testEvaluateFunctionExpressionWithSimpleArgs() {
        final String FUNCTION_NAME = "abs";
        FunctionExpression functionExpression = new FunctionExpression(FUNCTION_NAME, Collections.singletonList(-1));
        Object value = expressionEvaluator.evaluate(functionExpression, EMPTY_ROW);
        assertThat(value, is(instanceOf(Double.class)));
        assertThat(value, equalTo(1d));
    }
    
    @Test
    public void testEvaluateFunctionExpressionWithExpressionArgs() {
        final String FUNCTION_NAME = "abs";
        FunctionExpression functionExpression = new FunctionExpression(
                FUNCTION_NAME,
                Collections.singletonList(new ColumnExpression(NUMERIC_COLUMN_NAME, TABLE_NAME))
        );
        Object value = expressionEvaluator.evaluate(functionExpression, ROW_WITH_VALUES);
        assertThat(value, is(instanceOf(Double.class)));
        assertThat(value, equalTo(3d));
    }
    
    @Test
    public void testEvaluateInteger() {
        final int VALUE = 1;
        assertThat(expressionEvaluator.evaluate(VALUE, EMPTY_ROW), equalTo(VALUE));
    }
    
    @Test
    public void testEvaluateString() {
        final String VALUE = "value";
        assertThat(expressionEvaluator.evaluate(VALUE, EMPTY_ROW), equalTo(VALUE));
    }
    
    @Test
    public void testEvaluateFloat() {
        final float VALUE = 1.0f;
        assertThat(expressionEvaluator.evaluate(VALUE, EMPTY_ROW), equalTo(VALUE));
    }
    

}