package org.meridor.perspective.sql.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.*;
import org.meridor.perspective.sql.impl.parser.QueryType;
import org.meridor.perspective.sql.impl.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/query-processor-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class QueryProcessorImplTest {
    
    private static final String QUERY = "select * from instances";
    
    @Autowired
    private QueryProcessor queryProcessor;
    
    @Autowired
    private MockQueryPlanner mockQueryPlanner;

    @Test
    public void testMissingParameters() {
        List<QueryResult> results = queryProcessor.process(createQuery(
                //No value for :id: is provided
                "select * from instances where id = :id:"
        ));
        assertThat(results, hasSize(1));
        assertThat(results.get(0).getStatus(), equalTo(QueryStatus.MISSING_PARAMETERS));
    }

    @Test
    public void testSyntaxError() {
        String errorMessage = "Some syntax error";
        mockQueryPlanner.setException(new SQLSyntaxErrorException(errorMessage));
        List<QueryResult> results = queryProcessor.process(createQuery(QUERY));
        QueryResult syntaxErrorResult = new QueryResult();
        syntaxErrorResult.setStatus(QueryStatus.SYNTAX_ERROR);
        syntaxErrorResult.setData(DataContainer.empty().toData());
        syntaxErrorResult.setMessage(errorMessage);
        assertThat(results, contains(syntaxErrorResult));
    }

    @Test
    public void testEvaluationError() {
        String errorMessage = "something failed";
        mockQueryPlanner.setQueryPlan(createQueryPlan(
                previousTaskResult -> {
                    throw new SQLException(errorMessage);
                },
                QueryType.SELECT
        ));
        List<QueryResult> results = queryProcessor.process(createQuery(QUERY));
        QueryResult evaluationErrorResult = new QueryResult();
        evaluationErrorResult.setStatus(QueryStatus.EVALUATION_ERROR);
        evaluationErrorResult.setData(DataContainer.empty().toData());
        evaluationErrorResult.setMessage(errorMessage);
        assertThat(results, contains(evaluationErrorResult));

    }
    
    @Test
    public void testSuccess() {
        DataContainer dataContainer = new DataContainer(Arrays.asList("col_one", "col_two"));
        dataContainer.addRow(Arrays.asList("one", "two"));
        mockQueryPlanner.setQueryPlan(createQueryPlan(
                previousTaskResult -> {
                    ExecutionResult executionResult = new ExecutionResult();
                    executionResult.setCount(1);
                    executionResult.setData(dataContainer);
                    return executionResult;
                },
                QueryType.SELECT
        ));
        List<QueryResult> results = queryProcessor.process(createQuery(QUERY));
        QueryResult successResult = new QueryResult();
        successResult.setStatus(QueryStatus.SUCCESS);
        successResult.setData(dataContainer.toData());
        successResult.setCount(1);
        successResult.setMessage("");
        assertThat(results, contains(successResult));
    }
    
    @Test
    public void testExplain() {
        final String TASK_DESCRIPTION = "task_description";
        Task explainTask = new Task() {
            @Override
            public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
                throw new UnsupportedOperationException("Does nothing!");
            }

            @Override
            public String toString() {
                return TASK_DESCRIPTION;
            }
        };
        mockQueryPlanner.setQueryPlan(createQueryPlan(explainTask, QueryType.EXPLAIN));
        DataContainer explainDataContainer = new DataContainer(Collections.singleton("task"));
        explainDataContainer.addRow(Collections.singletonList(TASK_DESCRIPTION));
        List<QueryResult> results = queryProcessor.process(createQuery(QUERY));
        QueryResult explainResult = new QueryResult();
        explainResult.setStatus(QueryStatus.SUCCESS);
        explainResult.setData(explainDataContainer.toData());
        explainResult.setCount(1);
        explainResult.setMessage("");
        assertThat(results, contains(explainResult));

    }
    
    private QueryPlan createQueryPlan(Task task, org.meridor.perspective.sql.impl.parser.QueryType queryType) {
        return new QueryPlanImpl(new LinkedList<>(Collections.singleton(task)), queryType);
    }
    
    private static Query createQuery(String sql) {
        Query query = new Query();
        query.setSql(sql);
        return query;
    }
    
}