package org.meridor.perspective.sql.impl.expression;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.table.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.BooleanRelation.*;
import static org.meridor.perspective.sql.impl.expression.BinaryArithmeticOperator.*;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.*;
import static org.meridor.perspective.sql.impl.expression.UnaryArithmeticOperator.BIT_NOT;
import static org.meridor.perspective.sql.impl.expression.UnaryBooleanOperator.NOT;

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
    
    private static final Map<String, List<String>> columnsMap = new HashMap<String, List<String>>() {
        {
            put(
                    TABLE_NAME,
                    Arrays.asList(
                            STRING_COLUMN_NAME,
                            NUMERIC_COLUMN_NAME,
                            NUMERIC_COLUMN_NAME_WITH_DEFAULT_VALUE,
                            COLUMN_WITH_MISSING_DEFAULT_VALUE
                    )
            );
        }
    };
    private static final DataContainer DATA_CONTAINER = new DataContainer(columnsMap);
    private static final DataRow EMPTY_ROW = new DataRow(DATA_CONTAINER, Collections.emptyList());
    private static final DataRow ROW_WITH_VALUES = new DataRow(DATA_CONTAINER, Arrays.asList(STRING_COLUMN_VALUE, NUMERIC_COLUMN_VALUE));
    
    @Test
    public void testEvaluateNull() {
        assertThat(expressionEvaluator.evaluateAs(new Null(), EMPTY_ROW, Integer.class), is(nullValue()));
    }
    
    @Test
    public void testEvaluateConstant() {
        assertThat(expressionEvaluator.evaluate(1, EMPTY_ROW), equalTo(1));
        assertThat(expressionEvaluator.evaluate("string", EMPTY_ROW), equalTo("string"));
        assertThat(expressionEvaluator.evaluate(true, EMPTY_ROW), equalTo(true));
        assertThat(expressionEvaluator.evaluate(false, EMPTY_ROW), equalTo(false));
        assertThat(expressionEvaluator.evaluate(DataType.NULL, EMPTY_ROW), equalTo(DataType.NULL));

        ZonedDateTime now = ZonedDateTime.now();
        assertThat(expressionEvaluator.evaluate(now, EMPTY_ROW), equalTo(now));
    }
    
    @Test
    public void testEvaluateColumnExpression() {
        Object value = expressionEvaluator.evaluate(column(STRING_COLUMN_NAME, TABLE_NAME), ROW_WITH_VALUES);
        assertThat(value, is(instanceOf(String.class)));
        assertThat(value, equalTo(STRING_COLUMN_VALUE));
    }
    
    @Test
    public void testEvaluateMultipleAliasesExpression() {
        final String FIRST_ALIAS = "first";
        final String SECOND_ALIAS = "second";
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>() {
            {
                put(
                        FIRST_ALIAS,
                        Collections.singletonList(NUMERIC_COLUMN_NAME)
                );
                put(
                        SECOND_ALIAS,
                        Collections.singletonList(NUMERIC_COLUMN_NAME)
                );
            }
        };
        final DataContainer DATA_CONTAINER = new DataContainer(columnsMap);
        final DataRow DATA_ROW = new DataRow(DATA_CONTAINER, Arrays.asList(1, 2));
        Object firstValue = expressionEvaluator.evaluate(column(NUMERIC_COLUMN_NAME, FIRST_ALIAS), DATA_ROW);
        assertThat(firstValue, equalTo(1));
        Object secondValue = expressionEvaluator.evaluate(column(NUMERIC_COLUMN_NAME, SECOND_ALIAS), DATA_ROW);
        assertThat(secondValue, equalTo(2));
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
    public void testEvaluateLiteralBooleanExpression() {
        assertThat(bool(true), is(true));
        assertThat(bool(false), is(false));
    }
    
    @Test
    public void testEvaluateSimpleBooleanExpression() {
        assertThat(bool(null, EQUAL, 1), is(false));
        assertThat(bool(1, EQUAL, null), is(false));
        assertThat(bool(1, EQUAL, 1), is(true));
        assertThat(bool(1L, EQUAL, 1d), is(true));
        assertThat(bool(2, GREATER_THAN, 1), is(true));
        assertThat(bool(2f, GREATER_THAN_EQUAL, 1), is(true));
        assertThat(bool(absFunction(), GREATER_THAN_EQUAL, 1, ROW_WITH_VALUES), is(true));
        assertThat(bool(1, LESS_THAN, absFunction(), ROW_WITH_VALUES), is(true));
        assertThat(bool(1d, LESS_THAN_EQUAL, 2), is(true));
        assertThat(bool(1, NOT_EQUAL, 1), is(false));
        assertThat(bool(1L, NOT_EQUAL, 2), is(true));
        assertThat(bool("123", EQUAL, 123), is(true));
        assertThat(bool(123, EQUAL, "123"), is(true));
        assertThat(bool("123", EQUAL, "123"), is(true));
        assertThat(bool("123", NOT_EQUAL, "456"), is(true));
        assertThat(bool("01234", LIKE, "123"), is(true));
        assertThat(bool("123", LIKE, "1_3"), is(true));
        assertThat(bool("123", LIKE, "1\\_3"), is(false));
        assertThat(bool("123", LIKE, "%3%"), is(true));
        assertThat(bool("123", LIKE, "\\%3"), is(false));
        assertThat(bool("123", REGEXP, "..3"), is(true));
        assertThat(bool("123", REGEXP, ".\\.3"), is(false));
        assertThat(bool(true, EQUAL, false), is(false));
        assertThat(bool(true, NOT_EQUAL, false), is(true));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateIncorrectSimpleBooleanExpression() {
        bool(true, GREATER_THAN, false);
    }
    
    @Test
    public void testEvaluateInExpression() {
        assertThat(bool(2, Stream.of("1", "2", "3").collect(Collectors.toSet()), EMPTY_ROW), is(true));
        assertThat(bool(STRING_COLUMN_VALUE, Stream.of("stuff", column(STRING_COLUMN_NAME, TABLE_NAME)).collect(Collectors.toSet()), ROW_WITH_VALUES), is(true));
        assertThat(bool("3", Collections.emptySet(), EMPTY_ROW), is(false));
        assertThat(bool(null, Collections.emptySet(), EMPTY_ROW), is(false));
    }
    
    @Test
    public void testEvaluateIsNullExpression() {
        assertThat(isNull(new Null(), EMPTY_ROW), is(true));
        assertThat(isNull(2, EMPTY_ROW), is(false));
    }
    
    @Test
    public void testEvaluateBinaryBooleanExpression() {
        final SimpleBooleanExpression TRUTHY = new SimpleBooleanExpression(1, EQUAL, 1);
        final SimpleBooleanExpression FALSY = new SimpleBooleanExpression(1, NOT_EQUAL, 1);
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
    
    @Test
    public void testEvaluateUnaryBooleanExpression() {
        final SimpleBooleanExpression TRUTHY = new SimpleBooleanExpression(1, EQUAL, 1);
        final SimpleBooleanExpression FALSY = new SimpleBooleanExpression(1, NOT_EQUAL, 1);
        assertThat(bool(TRUTHY, NOT), is(false));
        assertThat(bool(FALSY, NOT), is(true));
    }

    @Test
    public void testEvaluateBinaryArithmeticExpression() {
        assertThat(arithmetic(2, BinaryArithmeticOperator.PLUS, 1, Integer.class), equalTo(3));
        assertThat(arithmetic(3d, BinaryArithmeticOperator.PLUS, absFunction(), ROW_WITH_VALUES, Double.class), equalTo(6d));
        assertThat(arithmetic(2, BinaryArithmeticOperator.MINUS, 1, Integer.class), equalTo(1));
        assertThat(arithmetic(3d, BinaryArithmeticOperator.MINUS, 1.5d, Double.class), equalTo(1.5));
        assertThat(arithmetic(2, MULTIPLY, 1, Integer.class), equalTo(2));
        assertThat(arithmetic(2d, MULTIPLY, 1.5d, Double.class), equalTo(3d));
        assertThat(arithmetic(2, DIVIDE, 1, Integer.class), equalTo(2));
        assertThat(arithmetic(3d, DIVIDE, 1.5d, Double.class), equalTo(2d));
        assertThat(arithmetic(4, MOD, 3, Integer.class), equalTo(1));
        assertThat(arithmetic(5d, MOD, 3d, Double.class), equalTo(2d));
        assertThat(arithmetic(0b101, BIT_AND, 0b100, Integer.class), equalTo(0b100));
        assertThat(arithmetic(0b101, BIT_OR, 0b100, Integer.class), equalTo(0b101));
        assertThat(arithmetic(0b101, BIT_XOR, 0b100, Integer.class), equalTo(0b001));
        assertThat(arithmetic(0b001, SHIFT_LEFT, 2, Integer.class), equalTo(0b100));
        assertThat(arithmetic(0b100, SHIFT_RIGHT, 2, Integer.class), equalTo(0b001));
    }

    @Test
    public void testEvaluateUnaryArithmeticExpression() {
        assertThat(arithmetic(1, UnaryArithmeticOperator.PLUS, Integer.class), equalTo(1));
        assertThat(arithmetic(2d, UnaryArithmeticOperator.PLUS, Double.class), equalTo(2d));
        assertThat(arithmetic(1, UnaryArithmeticOperator.MINUS, Integer.class), equalTo(-1));
        assertThat(arithmetic(column(NUMERIC_COLUMN_NAME, TABLE_NAME), UnaryArithmeticOperator.MINUS, ROW_WITH_VALUES, Double.class), equalTo(3d));
        assertThat(arithmetic(1, BIT_NOT, Integer.class), equalTo(-2)); //See http://stackoverflow.com/questions/2513525/bitwise-not-operator
    }
    
    private boolean bool(boolean literal) {
        return expressionEvaluator.evaluateAs(new LiteralBooleanExpression(literal), EMPTY_ROW, Boolean.class);
    }
    
    private boolean bool(Object left, BooleanRelation booleanRelation, Object right, DataRow dataRow) {
        return expressionEvaluator.evaluateAs(new SimpleBooleanExpression(left, booleanRelation, right), dataRow, Boolean.class);
    }
    
    private boolean bool(Object left, BooleanRelation booleanRelation, Object right) {
        return bool(left, booleanRelation, right, EMPTY_ROW);
    }
    
    private boolean bool(Object left, BinaryBooleanOperator binaryBooleanOperator, Object right, DataRow dataRow) {
        return expressionEvaluator.evaluateAs(new BinaryBooleanExpression(left, binaryBooleanOperator, right), dataRow, Boolean.class);
    }

    private boolean bool(Object left, BinaryBooleanOperator binaryBooleanOperator, Object right) {
        return bool(left, binaryBooleanOperator, right, EMPTY_ROW);
    }

    private boolean bool(BooleanExpression value, UnaryBooleanOperator unaryBooleanOperator) {
        return expressionEvaluator.evaluateAs(new UnaryBooleanExpression(value, unaryBooleanOperator), EMPTY_ROW, Boolean.class);
    }

    private boolean bool(Object value, Set<Object> candidates, DataRow dataRow) {
        return expressionEvaluator.evaluateAs(new InExpression(value, candidates), dataRow, Boolean.class);
    }
    
    private boolean isNull(Object value, DataRow dataRow) {
        return expressionEvaluator.evaluateAs(new IsNullExpression(value), dataRow, Boolean.class);
    }

    private <T extends Comparable<? super T>> T arithmetic(Object left, BinaryArithmeticOperator binaryArithmeticOperator, Object right, DataRow dataRow, Class<T> cls) {
        return expressionEvaluator.evaluateAs(new BinaryArithmeticExpression(left, binaryArithmeticOperator, right), dataRow, cls);
    }

    private <T extends Comparable<? super T>> T arithmetic(Object left, BinaryArithmeticOperator binaryArithmeticOperator, Object right, Class<T> cls) {
        return arithmetic(left, binaryArithmeticOperator, right, EMPTY_ROW, cls);
    }

    private <T extends Comparable<? super T>> T arithmetic(Object value, UnaryArithmeticOperator unaryArithmeticOperator, Class<T> cls) {
        return arithmetic(value, unaryArithmeticOperator, EMPTY_ROW, cls);
    }
    
    private <T extends Comparable<? super T>> T arithmetic(Object value, UnaryArithmeticOperator unaryArithmeticOperator, DataRow dataRow, Class<T> cls) {
        return expressionEvaluator.evaluateAs(new UnaryArithmeticExpression(value, unaryArithmeticOperator), dataRow, cls);
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
        ColumnExpression columnExpression = new ColumnExpression("id", "mock");
        BinaryArithmeticExpression binaryArithmeticExpression = new BinaryArithmeticExpression(columnExpression, BinaryArithmeticOperator.PLUS, 1);
        UnaryArithmeticExpression unaryArithmeticExpression = new UnaryArithmeticExpression(binaryArithmeticExpression, UnaryArithmeticOperator.MINUS);
        FunctionExpression functionExpression = new FunctionExpression("abs", new ArrayList<Object>() {
            {
                add(unaryArithmeticExpression);
            }
        });
        Map<String, Set<String>> columns = expressionEvaluator.getColumnNames(functionExpression);
        assertThat(columns.keySet(), hasSize(1));
        assertThat(columns.keySet(), contains("mock"));
        assertThat(columns.get("mock"), hasSize(1));
        assertThat(columns.get("mock"), contains("id"));
    }

}