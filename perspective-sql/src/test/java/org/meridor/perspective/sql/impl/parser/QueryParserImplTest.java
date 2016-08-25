package org.meridor.perspective.sql.impl.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.impl.expression.BooleanExpression;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.InExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLSyntaxErrorException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/query-parser-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class QueryParserImplTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testParseInExpression() throws Exception {
        SelectQueryAware selectQueryAware = parse(
                "select * from instances where id in ('test-instance')",
                SelectQueryAware.class
        );
        assertThat(selectQueryAware.getWhereExpression().isPresent(), is(true));
        BooleanExpression whereExpression = selectQueryAware.getWhereExpression().get();
        assertThat(whereExpression, is(instanceOf(InExpression.class)));
        InExpression inExpression = (InExpression) whereExpression;
        assertThat(inExpression.getValue(), is(instanceOf(ColumnExpression.class)));
        assertThat(inExpression.getValue(), equalTo(new ColumnExpression("id")));
        assertThat(inExpression.getCandidates(), contains("test-instance"));
    }
    
    private <T> T parse(String sql, Class<T> cls) throws SQLSyntaxErrorException {
        QueryParser queryParser = applicationContext.getBean(QueryParser.class);
        queryParser.parse(sql);
        assertThat(queryParser, is(instanceOf(cls)));
        return cls.cast(queryParser);
    }
    
}