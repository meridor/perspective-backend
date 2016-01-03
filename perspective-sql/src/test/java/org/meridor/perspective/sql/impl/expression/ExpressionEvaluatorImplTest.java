package org.meridor.perspective.sql.impl.expression;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.BooleanRelation.*;
import static org.meridor.perspective.sql.impl.expression.BooleanOperation.*;

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
    private static final String FUNCTION_NAME = "abs";
    
    private static final DataRow EMPTY_ROW = new DataRow();
    private static final DataRow ROW_WITH_VALUES = new DataRow(){
        {
            put(STRING_COLUMN_NAME, STRING_COLUMN_VALUE);
            put(NUMERIC_COLUMN_NAME, NUMERIC_COLUMN_VALUE);
        }
    };
    
    @Test
    public void testEvaluateColumnExpression() {
        Object value = expressionEvaluator.evaluate(column(STRING_COLUMN_NAME, TABLE_NAME), ROW_WITH_VALUES);
        assertThat(value, is(instanceOf(String.class)));
        assertThat(value, equalTo(STRING_COLUMN_VALUE));
    }
    
    @Test
    public void testEvaluateColumnExpressionWithDefaultValue() {
        Object value = expressionEvaluator.evaluate(column(NUMERIC_COLUMN_NAME_WITH_DEFAULT_VALUE, TABLE_NAME), ROW_WITH_VALUES);
        assertThat(value,is(instanceOf(Integer.class)));
        assertThat(value, equalTo(42));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateColumnExpressionWithMissingValue() {
        expressionEvaluator.evaluate(column(COLUMN_WITH_MISSING_DEFAULT_VALUE, TABLE_NAME), ROW_WITH_VALUES);
    }
    
    @Test
    public void testEvaluateFunctionExpressionWithSimpleArgs() {
        Object value = expressionEvaluator.evaluate(function(FUNCTION_NAME, Collections.singletonList(-1)), EMPTY_ROW);
        assertThat(value, is(instanceOf(Double.class)));
        assertThat(value, equalTo(1d));
    }
    
    private ColumnExpression column(String columnName, String tableName) {
        return new ColumnExpression(columnName, tableName);
    }
    
    private FunctionExpression function(String functionName, List<Object> args) {
        return new FunctionExpression(functionName, args);
    }
    
    private FunctionExpression absFunction() {
        return function(FUNCTION_NAME, Collections.singletonList(column(NUMERIC_COLUMN_NAME, TABLE_NAME)));
    }
    
    @Test
    public void testEvaluateFunctionExpressionWithExpressionArgs() {
        Object value = expressionEvaluator.evaluate(absFunction(), ROW_WITH_VALUES);
        assertThat(value, is(instanceOf(Double.class)));
        assertThat(value, equalTo(3d));
    }
    
    @Test
    public void testEvaluateSimpleBooleanExpression() {
        assertThat(bool(1, EQUAL, 1), is(true));
        assertThat(bool(1L, EQUAL, 1d), is(true));
        assertThat(bool(2, GREATER_THAN, 1), is(true));
        assertThat(bool(2f, GREATER_THAN_EQUAL, 1), is(true));
        assertThat(bool(absFunction(), GREATER_THAN_EQUAL, 1, ROW_WITH_VALUES), is(true));
        assertThat(bool(1, LESS_THAN, absFunction(), ROW_WITH_VALUES), is(true));
        assertThat(bool(1d, LESS_THAN_EQUAL, 2), is(true));
        assertThat(bool(1, NOT_EQUAL, 1), is(false));
        assertThat(bool(1L, NOT_EQUAL, 2), is(true));
        assertThat(bool("123", EQUAL, "123"), is(true));
        assertThat(bool("123", NOT_EQUAL, "456"), is(true));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateIncorrectSimpleBooleanExpression() {
        bool(1, EQUAL, "123");
    }
    
    @Test
    public void testEvaluateComplexBooleanExpression() {
        final SimpleBooleanExpression TRUTHY = new SimpleBooleanExpression(1, EQUAL, 1);
        final SimpleBooleanExpression FALSY = new SimpleBooleanExpression(1, NOT_EQUAL, 1);
        assertThat(bool(TRUTHY, NOT, null), is(false));
        assertThat(bool(FALSY, NOT, null), is(true));
        assertThat(bool(TRUTHY, AND, TRUTHY), is(true));
        assertThat(bool(TRUTHY, AND, FALSY), is(false));
        assertThat(bool(FALSY, AND, TRUTHY), is(false));
        assertThat(bool(FALSY, AND, FALSY), is(false));
        assertThat(bool(TRUTHY, OR, TRUTHY), is(true));
        assertThat(bool(TRUTHY, OR, FALSY), is(true));
        assertThat(bool(FALSY, OR, TRUTHY), is(true));
        assertThat(bool(FALSY, OR, FALSY), is(false));
        assertThat(bool(TRUTHY, XOR, TRUTHY), is(false));
        assertThat(bool(TRUTHY, XOR, FALSY), is(true));
        assertThat(bool(FALSY, XOR, TRUTHY), is(true));
        assertThat(bool(FALSY, XOR, FALSY), is(false));
    }
    
    private boolean bool(Object left, BooleanRelation booleanRelation, Object right, DataRow dataRow) {
        return expressionEvaluator.evaluateAs(new SimpleBooleanExpression(left, booleanRelation, right), dataRow, Boolean.class);
    }
    
    private boolean bool(Object left, BooleanRelation booleanRelation, Object right) {
        return bool(left, booleanRelation, right, EMPTY_ROW);
    }
    
    private boolean bool(SimpleBooleanExpression left, BooleanOperation booleanOperation, SimpleBooleanExpression right, DataRow dataRow) {
        return expressionEvaluator.evaluateAs(new ComplexBooleanExpression(left, booleanOperation, right), dataRow, Boolean.class);
    }
    
    private boolean bool(SimpleBooleanExpression left, BooleanOperation booleanOperation, SimpleBooleanExpression right) {
        return bool(left, booleanOperation, right, EMPTY_ROW);
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
    
    @Test
    public void testGetColumnNames() {
        FunctionExpression functionExpression = new FunctionExpression("abs", new ArrayList<Object>() {
            {
                add(new ColumnExpression("id", "mock"));
            }
        });
        Map<String, List<String>> columns = expressionEvaluator.getColumnNames(functionExpression);
        assertThat(columns.keySet(), hasSize(1));
        assertThat(columns.keySet(), contains("mock"));
        assertThat(columns.get("mock"), hasSize(1));
        assertThat(columns.get("mock").get(0), equalTo("id"));
    }

}