package org.meridor.perspective.sql.impl;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.Pair;
import org.meridor.perspective.sql.impl.parser.QueryParser;
import org.meridor.perspective.sql.impl.parser.SelectQueryAware;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.meridor.perspective.sql.impl.task.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.sql.impl.QueryPlannerImpl.OptimizedTask.*;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanExpression.alwaysTrue;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.AND;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.OR;
import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.INDEX_FETCH;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QueryPlannerImpl implements QueryPlanner {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    
    @Autowired
    private TablesAware tablesAware;
    
    private final Queue<Task> tasksQueue = new LinkedList<>();

    @Override
    public Queue<Task> plan(String sql) throws SQLSyntaxErrorException {
        QueryParser queryParser = applicationContext.getBean(QueryParser.class);
        queryParser.parse(sql);
        switch (queryParser.getQueryType()) {
            case SELECT: {
                SelectQueryAware selectQueryAware = queryParser.getSelectQueryAware();
                processSelectQuery(selectQueryAware);
                break;
            }
            case SHOW_TABLES: {
                tasksQueue.add(applicationContext.getBean(ShowTablesTask.class));
                break;
            }
            case UNKNOWN: throw new SQLSyntaxErrorException("Unknown query type");
        }
        return tasksQueue;
    }

    private void processSelectQuery(SelectQueryAware selectQueryAware) {

        Map<OptimizedTask, Task> optimizedQuery = optimizeSelectQuery(selectQueryAware);

        if (optimizedQuery.containsKey(DATASOURCE)) {
            tasksQueue.add(optimizedQuery.get(DATASOURCE));
        } else {
            tasksQueue.add(new DummyFetchTask());
        }
        
        if (optimizedQuery.containsKey(WHERE)) {
            tasksQueue.add(optimizedQuery.get(WHERE));
        }

        if (!selectQueryAware.getGroupByExpressions().isEmpty()) {
            GroupTask groupTask = applicationContext.getBean(GroupTask.class);
            selectQueryAware.getGroupByExpressions().forEach(groupTask::addExpression);
            tasksQueue.add(groupTask);
        }

        if (optimizedQuery.containsKey(HAVING)) {
            tasksQueue.add(optimizedQuery.get(HAVING));
        }

        if (!selectQueryAware.getOrderByExpressions().isEmpty()) {
            OrderTask orderTask = applicationContext.getBean(OrderTask.class);
            selectQueryAware.getOrderByExpressions().forEach(orderTask::addExpression);
            tasksQueue.add(orderTask);
        }

        SelectTask selectTask = applicationContext.getBean(SelectTask.class, selectQueryAware.getSelectionMap());
        tasksQueue.add(selectTask);

        if (selectQueryAware.getLimitCount().isPresent()) {
            tasksQueue.add(createLimitTask(
                    selectQueryAware.getLimitOffset(),
                    selectQueryAware.getLimitCount().get()
            ));
        }
    }
    
    private Map<OptimizedTask, Task> optimizeSelectQuery(SelectQueryAware selectQueryAware) {
        
        Map<OptimizedTask, Task> ret = new HashMap<>();
        
        Optional<DataSource> originalDataSourceCandidate = selectQueryAware.getDataSource();
        Map<String, String> tableAliases = selectQueryAware.getTableAliases();


        Pair<List<BooleanExpression>, Optional<BooleanExpression>> whereConditions = extractWhereConditions(selectQueryAware);
        Optional<BooleanExpression> havingConditionCandidate = whereConditions.getSecond();
        if (havingConditionCandidate.isPresent()) {
            ret.put(HAVING, createFilterTask(havingConditionCandidate.get()));
        }

        if (originalDataSourceCandidate.isPresent()) {

            Pair<DataSource, Optional<BooleanExpression>> optimizedConditions = optimizeDataSource(
                    originalDataSourceCandidate.get(),
                    whereConditions.getFirst(),
                    selectQueryAware.getSelectionMap(),
                    selectQueryAware.getTableAliases()
            );

            Optional<BooleanExpression> optimizedWhereConditionsCandidate = optimizedConditions.getSecond();
            if (optimizedWhereConditionsCandidate.isPresent()) {
                ret.put(WHERE, createFilterTask(optimizedWhereConditionsCandidate.get()));
            }

            DataSource optimizedDataSource = optimizedConditions.getFirst();
            ret.put(DATASOURCE, createDataSourceTask(optimizedDataSource, tableAliases));

        }
        
        return ret;
    }

    //Deciding whether having clause can be moved to where clause
    private Pair<List<BooleanExpression>, Optional<BooleanExpression>> extractWhereConditions(SelectQueryAware selectQueryAware) {
        
        List<BooleanExpression> whereConditions = new ArrayList<>();
        Optional<BooleanExpression> whereExpressionCandidate = selectQueryAware.getWhereExpression();
        if (whereExpressionCandidate.isPresent()) {
            whereConditions.add(whereExpressionCandidate.get());
        }
        
        boolean noGroupByTaskExists = selectQueryAware.getGroupByExpressions().isEmpty();
        Optional<BooleanExpression> havingExpressionCandidate = selectQueryAware.getHavingExpression();
        if (havingExpressionCandidate.isPresent() && noGroupByTaskExists) {
            whereConditions.add(havingExpressionCandidate.get());
            return new Pair<>(whereConditions, Optional.empty());
        } else {
            return new Pair<>(whereConditions, havingExpressionCandidate);
        }
    }

    //Deciding whether parts of where clause can be moved to data sources
    private Pair<DataSource, Optional<BooleanExpression>> optimizeDataSource(
            DataSource originalDataSource,
            List<BooleanExpression> originalWhereConditions,
            Map<String, Object> selectionMap,
            Map<String, String> tableAliases
    ) {
                    
            /*
                 1) Перенос фиксиров значений в join
                 2) Склеивание OR выражений по одной колонке: where name = '1' or name = '2'
                4) Выбор индексов и создание задач по ним
             */

        Map<String, Set<String>> columnRelationsAsColumnNames = new HashMap<>();
        //TODO: implement it!

        Map<String, Map<String, Set<Object>>> fixedValuesConditions = new HashMap<>();
        List<Map<String, Set<String>>> columnRelations = new ArrayList<>();
        List<BooleanExpression> restOfExpressions = new ArrayList<>();
        originalWhereConditions.forEach(wc -> {
            wc.getTableAliases().forEach(ta -> fixedValuesConditions.put(ta, wc.getFixedValueConditions(ta)));
            columnRelations.add(wc.getColumnRelations());
            Optional<BooleanExpression> restOfExpressionCandidate = wc.getRestOfExpression();
            if (restOfExpressionCandidate.isPresent()) {
                restOfExpressions.add(restOfExpressionCandidate.get());
            }
        });
        
        
        
        List<BooleanExpression> optimizedWhereConditions = new ArrayList<>();
        
        DataSource optimizedDataSource = originalDataSource.copy();
        
        iterateDataSource(Optional.of(optimizedDataSource), ds -> {
            
            if (ds.getTableAlias().isPresent()) {
                String tableAlias = ds.getTableAlias().get();
                
                //Testing whether index fetch can be applied
                Set<String> columnNamesToSelect = getColumnNamesToSelect(tableAlias, selectionMap);
                
                if (fixedValuesConditions.containsKey(tableAlias)) {
                    columnNamesToSelect.addAll(fixedValuesConditions.get(tableAlias).keySet());
                }
                
                //TODO: need to consider column relations and rest of expressions
                
                String tableName = tableAliases.get(tableAlias);
                IndexSignature indexSignature = new IndexSignature(Collections.singletonMap(tableName, columnNamesToSelect));
                Optional<Index> indexCandidate = tablesAware.getIndex(indexSignature);
                if (indexCandidate.isPresent()) {
                    ds.getColumns().addAll(columnNamesToSelect);
                    ds.setType(INDEX_FETCH);
                }
                
            }
            
            
            
            
            
            
//            originalWhereConditions.stream()
//                .forEach(wc -> {
//                    wc.getTableAliases().stream()
//                        .filter(ta -> ds.getTableAlias().isPresent() && ta.equals(ds.getTableAlias().get()))
//                        .forEach(ta -> {
//                            //TODO: think about it!
//                            //TODO: one where condition should be processed only once!!!
//                            List<Optional<BooleanExpression>> conditions = new ArrayList<>();
//                            conditions.add(ds.getCondition());
//
//                            Optional<BooleanExpression> remainingCondition = wc.getRestOfExpression();
//                            conditions.add(remainingCondition);
//                            
//                            Map<String, Set<Object>> fixedValueConditions = wc.getFixedValueConditions(ta);
//                            Optional<BooleanExpression> fixedValuesBooleanExpression = fixedValuesToBooleanExpression(ta, fixedValueConditions);
//                            if (fixedValuesBooleanExpression.isPresent()) {
//                                Optional<BooleanExpression> conditionsIntersection = intersectConditions(ds.getCondition(), fixedValuesBooleanExpression);
//                                if (conditionsIntersection.isPresent()) {
//                                    ds.setCondition(conditionsIntersection.get());
//                                }
//                            }
//                            //TODO: modify ds condition and where conditions
//                            
//                            if (remainingCondition.isPresent()) {
//                                Map<String, Set<String>> columnRelations = wc.getColumnRelations();
//                                if (joinExists(ds, columnRelations)) {
//                                    //TODO: modify ds condition
//                                }
//                            }
//                            
//                            BooleanExpression optimizedWhereCondition = conditions.stream()
//                                    .filter(Optional::isPresent)
//                                    .map(Optional::get)
//                                    .reduce(alwaysTrue(), (l, r) -> new BinaryBooleanExpression(l, AND, r));
//                            
//                            optimizedWhereConditions.add(optimizedWhereCondition);
//                            
//                        });
//                });
            
            
        });
        
        Optional<BooleanExpression> optimizedWhereCondition = optimizedWhereConditions.isEmpty() ?
                Optional.empty() :
                Optional.of(
                        optimizedWhereConditions.stream()
                          .reduce(alwaysTrue(), (l, r) -> new BinaryBooleanExpression(l, AND, r))
                );
        
        return new Pair<>(optimizedDataSource, optimizedWhereCondition);
    }
    
    private Set<String> getColumnNamesToSelect(String tableAlias, Map<String, Object> selectionMap) {
        return selectionMap.values().stream()
                .flatMap(v -> expressionEvaluator.getColumnNames(v).getOrDefault(tableAlias, Collections.emptySet()).stream())
                .collect(Collectors.toSet());
    }
    
    private static boolean joinExists(DataSource ds, Map<String, Set<String>> columnRelations) {
        Optional<String> tableAlias = ds.getTableAlias();
        Optional<DataSource> nextDataSourceCandidate = ds.getRightDataSource();
        Optional<String> nextDataSourceTableAlias = nextDataSourceCandidate.isPresent() ?
                nextDataSourceCandidate.get().getTableAlias() :
                Optional.empty();
        return 
                tableAlias.isPresent() &&
                columnRelations.containsKey(tableAlias.get()) &&
                nextDataSourceTableAlias.isPresent() &&
                columnRelations.containsKey(nextDataSourceTableAlias.get());
    }
    
    private static Optional<BooleanExpression> fixedValuesToBooleanExpression(String tableAlias, Map<String, Set<Object>> fixedValueConditions) {
        if (fixedValueConditions.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(fixedValueConditions.keySet().stream()
                .map(columnName -> {
                    Set<Object> values = fixedValueConditions.get(columnName);
                    return values.stream()
                            .map(v -> new SimpleBooleanExpression(new ColumnExpression(columnName, tableAlias), BooleanRelation.EQUAL, v))
                            .reduce(
                                    alwaysTrue(),
                                    (l, r) -> new BinaryBooleanExpression(l, OR, r),
                                    (l, r) -> new BinaryBooleanExpression(l, OR, r)
                            );
                })
                .reduce(alwaysTrue(), (l, r) -> new BinaryBooleanExpression(l, AND, r)));
    }
    
    private static Optional<BooleanExpression> intersectConditions(Optional<BooleanExpression> left, Optional<BooleanExpression> right) {
        if (!left.isPresent()) {
            return right;
        }
        if (!right.isPresent()) {
            return left;
        }
        return Optional.of(new BinaryBooleanExpression(left.get(), BinaryBooleanOperator.AND, right.get()));
    }
    
    private void iterateDataSource(Optional<DataSource> dataSourceCandidate, Consumer<DataSource> dataSourceConsumer) {
        if (!dataSourceCandidate.isPresent()) {
            return;
        }
        DataSource dataSource = dataSourceCandidate.get();
        dataSourceConsumer.accept(dataSource);
        iterateDataSource(dataSource.getLeftDataSource(), dataSourceConsumer);
        iterateDataSource(dataSource.getRightDataSource(), dataSourceConsumer);
    }

    private Task createFilterTask(BooleanExpression booleanExpression) {
        FilterTask filterTask = applicationContext.getBean(FilterTask.class);
        filterTask.setCondition(dr -> expressionEvaluator.evaluateAs(booleanExpression, dr, Boolean.class));
        return filterTask;
    }
    
    private Task createDataSourceTask(DataSource dataSource, Map<String, String> tableAliases) {
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                tableAliases
        );
        return dataSourceTask;
    }

    private LimitTask createLimitTask(Optional<Integer> limitOffset, Integer limitCount) {
        return limitOffset.isPresent() ?
                new LimitTask(limitOffset.get(), limitCount) :
                new LimitTask(limitCount);
    }

    enum OptimizedTask {
    
        DATASOURCE,
        WHERE,
        HAVING
        
    }
    
}
