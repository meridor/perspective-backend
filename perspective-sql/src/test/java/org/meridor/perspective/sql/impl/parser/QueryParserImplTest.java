package org.meridor.perspective.sql.impl.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLSyntaxErrorException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/query-parser-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class QueryParserImplTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testWeirdStuff() throws Exception {
        parse("some weird stuff");
    }
    
    @Test
    public void testExplainQuery() throws Exception {
        QueryParser queryParser = parse(
                "explain select * from instances",
                QueryParser.class
        );
        assertThat(queryParser.getQueryType(), equalTo(QueryType.EXPLAIN));
    }
    
    @Test
    public void testTableAliases() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances as i, projects as p",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getTableAliases(), equalTo(new HashMap<String, String>(){
            {
                put("i", "instances");
                put("p", "projects");
            }
        }));
    }
    
    @Test
    public void testGroupByClause() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances group by id",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getGroupByExpressions(), contains(new ColumnExpression("id")));
    }
    
    @Test
    public void testOrderByClause() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances order by id desc",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getOrderByExpressions(), contains(
                new OrderExpression(new ColumnExpression("id"), OrderDirection.DESC)
        ));
    }
    
    @Test
    public void testHavingClause() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances having instances.id = 'test'",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getHavingExpression(), equalTo(Optional.of(
                new SimpleBooleanExpression(
                        new ColumnExpression("id", "instances"),
                        BooleanRelation.EQUAL,
                        "test"
                )
        )));
    }
    
    @Test
    public void testLimitClause() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances limit 5, 10",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getLimitOffset(), equalTo(Optional.of(5)));
        assertThat(selectQueryAware.getLimitCount(), equalTo(Optional.of(10)));
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testWrongLimit() throws Exception {
        //This is because of a parse error
        parse("select * from instances limit -1");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testWrongOffset() throws Exception {
        //This is because of a parse error
        parse("select * from instances limit -1, 5");
    }
    
    @Test
    public void testSelectAllTableColumns() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select instances.* from instances",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getSelectionMap().keySet(), contains("instances.*"));
        assertThat(selectQueryAware.getSelectionMap().values(), contains(
                new ColumnExpression(Column.ANY_COLUMN, "instances"))
        );
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testMissingTableAliasInColumnName() throws Exception {
        parse("select missing.id from instances");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testDuplicateTableAlias() throws Exception {
        parse("select * from instances as i, projects as i");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testMissingColumn() throws Exception {
        parse("select missing_column from instances");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testMissingTable() throws Exception {
        parse("select name from missing_table");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testMissingAliasesColumn() throws Exception {
        parse("select i.missing_column from instances as i");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testAmbiguousColumn() throws Exception {
        parse("select id from instances, projects");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testInvalidSelectAllFromTable() throws Exception {
        parse("select id from instances where instances.* = 'test'");
    }
    
    @Test(expected = SQLSyntaxErrorException.class)
    public void testInvalidSelectAll() throws Exception {
        parse("select id from instances where * = 'test'");
    }
    
    @Test
    public void testSelectConstants() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select false, true, 1.2, 1, 'bla', ''",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getSelectionMap().keySet(), contains("FALSE", "TRUE", "1.2", "1", "bla", ""));
        assertThat(selectQueryAware.getSelectionMap().values(), contains(false, true, 1.2F, 1, "bla", ""));
    }

    @Test
    public void testSelectNull() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select null",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getSelectionMap().keySet(), contains("NULL"));
        assertThat(selectQueryAware.getSelectionMap().get("NULL"), is(instanceOf(Null.class)));
    }

    @Test
    public void testSelectUnaryArithmeticExpression() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select +2, -1.2, ~3",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getSelectionMap().keySet(), contains("+2", "-1.2", "~3"));
        assertThat(selectQueryAware.getSelectionMap().values(), contains(
                new UnaryArithmeticExpression(2, UnaryArithmeticOperator.PLUS),
                new UnaryArithmeticExpression(1.2F, UnaryArithmeticOperator.MINUS),
                new UnaryArithmeticExpression(3, UnaryArithmeticOperator.BIT_NOT)
        ));
    }
    
    @Test
    public void testSelectFunctionExpression() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select abs(-1)",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getSelectionMap().keySet(), contains("abs(-1)"));
        assertThat(selectQueryAware.getSelectionMap().values(), contains(
                new FunctionExpression(
                        "abs",
                        Collections.singletonList(
                                new UnaryArithmeticExpression(1, UnaryArithmeticOperator.MINUS)
                        )
                )
        ));
    }
    
    @Test
    public void testBinaryArithmeticExpression() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select 1 + 1, 3 - 2, 2 * 4, 6 / 2, 5 % 2, 3 & 2, 4 | 3, 5 ^ 6, 2 << 2, 100 >> 2",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getSelectionMap().keySet(), contains("1 + 1", "3 - 2", "2 * 4", "6 / 2", "5 % 2", "3 & 2", "4 | 3", "5 ^ 6", "2 << 2", "100 >> 2"));
        assertThat(selectQueryAware.getSelectionMap().values(), contains(
                new BinaryArithmeticExpression(1, BinaryArithmeticOperator.PLUS, 1),
                new BinaryArithmeticExpression(3, BinaryArithmeticOperator.MINUS, 2),
                new BinaryArithmeticExpression(2, BinaryArithmeticOperator.MULTIPLY, 4),
                new BinaryArithmeticExpression(6, BinaryArithmeticOperator.DIVIDE, 2),
                new BinaryArithmeticExpression(5, BinaryArithmeticOperator.MOD, 2),
                new BinaryArithmeticExpression(3, BinaryArithmeticOperator.BIT_AND, 2),
                new BinaryArithmeticExpression(4, BinaryArithmeticOperator.BIT_OR, 3),
                new BinaryArithmeticExpression(5, BinaryArithmeticOperator.BIT_XOR, 6),
                new BinaryArithmeticExpression(2, BinaryArithmeticOperator.SHIFT_LEFT, 2),
                new BinaryArithmeticExpression(100, BinaryArithmeticOperator.SHIFT_RIGHT, 2)
        ));
    }

    @Test
    public void testWhereEqual() throws Exception {
        testBooleanRelation(
                "select * from instances where id = 1",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.EQUAL, 1)
        );
    }

    @Test
    public void testWhereLessThan() throws Exception {
        testBooleanRelation(
                "select * from instances where id < 100",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.LESS_THAN, 100)
        );
    }

    @Test
    public void testWhereGreaterThan() throws Exception {
        testBooleanRelation(
                "select * from instances where id > 12",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.GREATER_THAN, 12)
        );
    }

    @Test
    public void testWhereLessThanEqual() throws Exception {
        testBooleanRelation(
                "select * from instances where id <= 19",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.LESS_THAN_EQUAL, 19)
        );
    }

    @Test
    public void testWhereGreaterThanEqual() throws Exception {
        testBooleanRelation(
                "select * from instances where id >= 42",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.GREATER_THAN_EQUAL, 42)
        );
    }

    @Test
    public void testWhereNotEqual() throws Exception {
        testBooleanRelation(
                "select * from instances where id != 23",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.NOT_EQUAL, 23)
        );
    }

    @Test
    public void testProcessUnaryBooleanExpression() throws Exception {
        testBooleanRelation(
                "select * from instances where ! false",
                new UnaryBooleanExpression(new LiteralBooleanExpression(false), UnaryBooleanOperator.NOT)
        );
    }
    
    @Test
    public void testBinaryBooleanExpression() throws Exception {
        BooleanExpression idEqualOne = new SimpleBooleanExpression(
                new ColumnExpression("id"),
                BooleanRelation.EQUAL,
                "one"
        );
        testBooleanRelation(
                "select * from instances where id = 'one' or true and id = 'one' xor false",
                new BinaryBooleanExpression(
                        idEqualOne,
                        BinaryBooleanOperator.OR,
                        new BinaryBooleanExpression(
                                new LiteralBooleanExpression(true),
                                BinaryBooleanOperator.AND,
                                new BinaryBooleanExpression(
                                        idEqualOne,
                                        BinaryBooleanOperator.XOR,
                                        new LiteralBooleanExpression(false)
                                )
                        )
                )
        );
    }

    @Test
    public void testProcessBinaryBooleanExpressionsJoin() throws Exception {
        testBooleanRelation(
                "select * from instances where id = 'one' or id = 'two'",
                new BinaryBooleanExpression(
                        new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.EQUAL, "one"),
                        BinaryBooleanOperator.OR,
                        new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.EQUAL, "two")
                )
        );
    }
    
    @Test
    public void testBooleanExpressionInBrackets() throws Exception {
        testBooleanRelation(
                "select * from instances where (id = 'one')",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.EQUAL, "one")
        );
    }

    @Test
    public void testBetweenExpression() throws Exception {
        testBooleanRelation(
                "select * from instances where id between 1 and 2",
                new BinaryBooleanExpression(
                        new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.GREATER_THAN_EQUAL, 1),
                        BinaryBooleanOperator.AND,
                        new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.LESS_THAN_EQUAL, 2)
                )
        );
    }

    @Test
    public void testIsNullExpression() throws Exception {
        testBooleanRelation(
                "select * from instances where id is null",
                new IsNullExpression(new ColumnExpression("id"))
        );
    }

    @Test
    public void testIsNotNullExpression() throws Exception {
        testNotBooleanRelation(
                "select * from instances where id is not null",
                new IsNullExpression(new ColumnExpression("id"))
        );
    }

    @Test
    public void testInExpression() throws Exception {
        testBooleanRelation(
                "select * from instances where id in ('test-instance')",
                new InExpression(new ColumnExpression("id"), Collections.singleton("test-instance"))
        );
    }

    @Test
    public void testNotInExpression() throws Exception {
        testNotBooleanRelation(
                "select * from instances where id not in ('test-instance')",
                new InExpression(new ColumnExpression("id"), Collections.singleton("test-instance"))
        );
    }

    @Test
    public void testLikeExpression() throws Exception {
        testBooleanRelation(
                "select * from instances where id like 'test'",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.LIKE, "test")
        );
    }

    @Test
    public void testNotLikeExpression() throws Exception {
        testNotBooleanRelation(
                "select * from instances where id not like 'test'",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.LIKE, "test")
        );
    }
    
    @Test
    public void testRegexpExpression() throws Exception {
        testBooleanRelation(
                "select * from instances where id regexp 'test'",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.REGEXP, "test")
        );
    }

    @Test
    public void testNotRegexpExpression() throws Exception {
        testNotBooleanRelation(
                "select * from instances where id not regexp 'test'",
                new SimpleBooleanExpression(new ColumnExpression("id"), BooleanRelation.REGEXP, "test")
        );
    }

    @Test
    public void testInnerJoin() throws Exception {
        testInnerJoin("select * from instances inner join projects");
    }

    @Test
    public void testTableReferences() throws Exception {
        testInnerJoin("select * from (instances, projects)");
    }
    
    private void testInnerJoin(String sql) throws Exception {
        SelectQueryAware selectQueryAware = parse(
                sql,
                SelectQueryAware.class
        );
        DataSource instancesDataSource = new DataSource("instances");
        DataSource projectsDataSource = new DataSource("projects");
        projectsDataSource.setJoinType(JoinType.INNER);
        instancesDataSource.setRightDataSource(projectsDataSource);
        assertThat(selectQueryAware.getDataSource(), equalTo(Optional.of(instancesDataSource)));
    }

    @Test
    public void testLeftJoin() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances left join projects on instances.id = projects.id",
                SelectQueryAware.class
        );
        DataSource instancesDataSource = new DataSource("instances");
        DataSource projectsDataSource = new DataSource("projects");
        projectsDataSource.setJoinType(JoinType.LEFT);
        BooleanExpression joinCondition = new SimpleBooleanExpression(
                new ColumnExpression("id", "instances"), 
                BooleanRelation.EQUAL,
                new ColumnExpression("id", "projects")
        );
        projectsDataSource.setCondition(joinCondition);
        instancesDataSource.setRightDataSource(projectsDataSource);
        assertThat(selectQueryAware.getDataSource(), equalTo(Optional.of(instancesDataSource)));
    }

    @Test
    public void testRightJoin() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances right join projects using (id)",
                SelectQueryAware.class
        );
        DataSource instancesDataSource = new DataSource("instances");
        DataSource projectsDataSource = new DataSource("projects");
        projectsDataSource.setJoinType(JoinType.RIGHT);
        projectsDataSource.getColumns().add("id");
        instancesDataSource.setRightDataSource(projectsDataSource);
        assertThat(selectQueryAware.getDataSource(), equalTo(Optional.of(instancesDataSource)));
    }

    @Test
    public void testNaturalInnerJoin() throws Exception {
        testNaturalJoin(
                "select * from instances natural join projects",
                JoinType.INNER
        );
    }

    @Test
    public void testNaturalLeftJoin() throws Exception {
        testNaturalJoin(
                "select * from instances natural left join projects",
                JoinType.LEFT
        );
    }

    @Test
    public void testNaturalRightJoin() throws Exception {
        testNaturalJoin(
                "select * from instances natural right join projects",
                JoinType.RIGHT
        );
    }
    
    private void testNaturalJoin(String sql, JoinType joinType) throws Exception {
        SelectQueryAware selectQueryAware = parse(
                sql,
                SelectQueryAware.class
        );
        DataSource instancesDataSource = new DataSource("instances");
        DataSource projectsDataSource = new DataSource("projects");
        projectsDataSource.setJoinType(joinType);
        projectsDataSource.setNaturalJoin(true);
        instancesDataSource.setRightDataSource(projectsDataSource);
        assertThat(selectQueryAware.getDataSource(), equalTo(Optional.of(instancesDataSource)));
    }
    
    private void testNotBooleanRelation(String sql, BooleanExpression booleanExpression) throws Exception {
        testBooleanRelation(sql, new UnaryBooleanExpression(booleanExpression, UnaryBooleanOperator.NOT));
    }
    
    private void testBooleanRelation(String sql, BooleanExpression booleanExpression) throws Exception {
        //Some relations can be semantically wrong because id is a string but parser is not aware about it
        SelectQueryAware selectQueryAware = parse(sql, SelectQueryAware.class);
        assertThat(selectQueryAware.getWhereExpression(), equalTo(Optional.of(booleanExpression)));
    }

    private void parse(String sql) throws SQLSyntaxErrorException {
        parse(sql, QueryParser.class);
    }
    
    private <T> T parse(String sql, Class<T> cls) throws SQLSyntaxErrorException {
        QueryParser queryParser = applicationContext.getBean(QueryParser.class);
        queryParser.parse(sql);
        assertThat(queryParser, is(instanceOf(cls)));
        return cls.cast(queryParser);
    }
    
}