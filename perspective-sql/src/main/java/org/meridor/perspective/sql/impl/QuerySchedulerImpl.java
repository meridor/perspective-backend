package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.SQLParserBaseListener;
import org.meridor.perspective.sql.impl.expression.ExpressionEvaluator;
import org.meridor.perspective.sql.impl.parser.QueryParser;
import org.meridor.perspective.sql.impl.parser.SelectQueryAware;
import org.meridor.perspective.sql.impl.task.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLSyntaxErrorException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QuerySchedulerImpl extends SQLParserBaseListener implements QueryScheduler {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    
    private final Queue<Task> tasksQueue = new LinkedList<>();

    @Override
    public Queue<Task> schedule(String sql) throws SQLSyntaxErrorException {
        QueryParser queryParser = applicationContext.getBean(QueryParser.class);
        queryParser.parse(sql);
        switch (queryParser.getQueryType()) {
            case SELECT: {
                SelectQueryAware selectQueryAware = queryParser.getSelectQueryAware();
                //TODO: add tasks for select, from and where
                if (!selectQueryAware.getGroupByExpressions().isEmpty()) {
                    GroupTask groupTask = applicationContext.getBean(GroupTask.class);
                    selectQueryAware.getGroupByExpressions().forEach(groupTask::addExpression);
                    tasksQueue.add(groupTask);
                }
                if (selectQueryAware.getHavingExpression().isPresent()) {
                    FilterTask filterTask = applicationContext.getBean(FilterTask.class);
                    Object havingExpression = selectQueryAware.getHavingExpression().get();
                    filterTask.setCondition(dr -> expressionEvaluator.evaluateAs(havingExpression, dr, Boolean.class));
                    tasksQueue.add(filterTask);
                }
                if (!selectQueryAware.getOrderByExpressions().isEmpty()) {
                    OrderTask orderTask = applicationContext.getBean(OrderTask.class);
                    selectQueryAware.getOrderByExpressions().forEach(orderTask::addExpression);
                }
                if (selectQueryAware.getLimitCount().isPresent()) {
                    tasksQueue.add(createLimitTask(
                            selectQueryAware.getLimitOffset(),
                            selectQueryAware.getLimitCount().get()
                    ));
                }
                break;
            }
            case SHOW_TABLES: {
                tasksQueue.add(new ShowTablesTask());
                break;
            }
            case UNKNOWN: throw new SQLSyntaxErrorException("Unknown query type");
        }
        return tasksQueue;
    }

    private LimitTask createLimitTask(Optional<Integer> limitOffset, Integer limitCount) {
        return limitOffset.isPresent() ?
                new LimitTask(limitOffset.get(), limitCount) :
                new LimitTask(limitCount);
    }
    
}
