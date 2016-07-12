package org.meridor.perspective.sql.impl;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.Pair;
import org.meridor.perspective.sql.impl.parser.QueryParser;
import org.meridor.perspective.sql.impl.parser.SelectQueryAware;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.meridor.perspective.sql.impl.task.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.sql.impl.QueryPlannerImpl.OptimizedTask.*;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanExpression.alwaysTrue;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.AND;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.OR;
import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToCondition;
import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToNames;
import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.INDEX_FETCH;
import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.INDEX_SCAN;
import static org.meridor.perspective.sql.impl.parser.DataSourceUtils.*;

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

        SelectTask selectTask = applicationContext.getBean(SelectTask.class, selectQueryAware.getSelectionMap(), selectQueryAware.getTableAliases());
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

        //Assumption: original data source is a chain of data sources 
        // corresponding to a chain of joins in SQL request. This probably will
        // need to be reconsidered while implementing sub-queries.
                    
        final Map<String, Map<String, Set<Object>>> fixedValuesConditions = new HashMap<>();
        final ColumnRelations columnRelations = new ColumnRelations();
        final List<BooleanExpression> restOfExpressions = new ArrayList<>();

        //------------------------
        // This section analyses all where conditions present in datasource and where clause
        
        //Analysing where conditions
        originalWhereConditions.forEach(wc -> {
            getFixedValuesConsumer(fixedValuesConditions).accept(wc);
            getColumnRelationsConsumer(columnRelations).accept(wc);
            getRestOfExpressionsConsumer(restOfExpressions).accept(wc);
        });
        
        //Analyzing data sources
        iterateDataSource(originalDataSource, (pds, ds) -> {
            if (pds.isPresent()) {
                replaceNaturalJoin(ds, pds.get(), tableAliases);
            }
            
            if (ds.getCondition().isPresent()) {
                BooleanExpression booleanExpression = ds.getCondition().get();
                getFixedValuesConsumer(fixedValuesConditions).accept(booleanExpression);
                getColumnRelationsConsumer(columnRelations).accept(booleanExpression);
                getRestOfExpressionsConsumer(restOfExpressions).accept(booleanExpression);
            }

            String tableAlias = ds.getTableAlias().get();
            if (pds.isPresent() && pds.get().getTableAlias().isPresent() && !ds.getColumns().isEmpty()) {
                String previousTableAlias = pds.get().getTableAlias().get();
                Optional<BooleanExpression> columnsBooleanExpression = columnsToCondition(Optional.empty(), previousTableAlias, ds.getColumns(), tableAlias);
                if (columnsBooleanExpression.isPresent()) {
                    getColumnRelationsConsumer(columnRelations).accept(columnsBooleanExpression.get());
                }
            }
        });

        // Deciding which columns should be selected
        final Map<String, Set<String>> columnNamesToSelect = getColumnNamesToSelect(selectionMap, tableAliases, fixedValuesConditions, columnRelations, restOfExpressions);

        //-----------------------
        // This section will create optimized data source
        final DataSource optimizedDataSource = new DataSource();
        final AtomicBoolean firstIndexScanDataSourceAdded = new AtomicBoolean();
        iterateDataSource(originalDataSource, (pds, ds) -> {

            DataSource optimizedChildDataSource = ds.copy();
            
            String tableAlias = ds.getTableAlias().get();
            
            if (isSuitableForIndexFetch(ds, tableAliases, columnNamesToSelect)) {
                optimizedChildDataSource.setType(INDEX_FETCH);
                Set<String> tableColumnNamesToSelect = columnNamesToSelect.get(tableAlias);
                optimizedChildDataSource.getColumns().clear();
                optimizedChildDataSource.getColumns().addAll(tableColumnNamesToSelect);
                addToDataSource(optimizedDataSource, optimizedChildDataSource);
            } else {

                Set<String> indexScanColumns = getIndexedRelationColumns(tableAlias, tableAliases, columnRelations);
                Optional<IndexBooleanExpression> indexScanBooleanExpressionCandidate = getIndexScanBooleanExpression(tableAlias, tableAliases, fixedValuesConditions);
                
                if (!indexScanColumns.isEmpty()) {
                    //Index foreign key join
                    ds.setType(INDEX_SCAN);
                    ds.getColumns().clear();
                    ds.getColumns().addAll(indexScanColumns);
                    ds.setRightDatasource(null);
                    if (indexScanBooleanExpressionCandidate.isPresent()) {
                        ds.setCondition(indexScanBooleanExpressionCandidate.get());
                    }
                    
                    DataSource tailDataSource = getTail(optimizedDataSource);
                    if (
                            tailDataSource.getType() == INDEX_SCAN &&
                            ds.getJoinType().isPresent() &&
                            firstIndexScanDataSourceAdded.get()
                    ) {
                        tailDataSource.setRightDatasource(optimizedChildDataSource);
                        firstIndexScanDataSourceAdded.set(false);
                    } else {
                        firstIndexScanDataSourceAdded.set(true);
                        addToDataSource(optimizedDataSource, optimizedChildDataSource);
                    }
                } else if (indexScanBooleanExpressionCandidate.isPresent()) {
                    //Simple index scan
                    ds.setType(INDEX_SCAN);
                    ds.setCondition(indexScanBooleanExpressionCandidate.get());
                    addToDataSource(optimizedDataSource, optimizedChildDataSource);
                } else {
                    //Add table scan data source
                    if (ds.getJoinType().isPresent()) {
                        Optional<BooleanExpression> joinCondition = ds.getCondition();
                        Optional<BooleanExpression> booleanExpressionCandidate = fixedValuesToBooleanExpression(tableAlias, fixedValuesConditions.remove(tableAlias));
                        Optional<BooleanExpression> updatedJoinCondition = intersectConditions(joinCondition, booleanExpressionCandidate);
                        if (updatedJoinCondition.isPresent()) {
                            ds.setCondition(updatedJoinCondition.get());
                        }
                    }
                    addToDataSource(optimizedDataSource, optimizedChildDataSource);
                }
            }
            
        });

        
        //Not used fixed values conditions should be moved to where clause, e.g. index fetch strategy does not support conditions
        Set<String> fixedValuesTableAliases = new HashSet<>(fixedValuesConditions.keySet());
        fixedValuesTableAliases.forEach(tableAlias -> {
            Optional<BooleanExpression> booleanExpressionCandidate = fixedValuesToBooleanExpression(tableAlias, fixedValuesConditions.remove(tableAlias));
            if (booleanExpressionCandidate.isPresent()) {
                restOfExpressions.add(booleanExpressionCandidate.get());
            }
        });
        
        Assert.isTrue(fixedValuesConditions.isEmpty(), "All fixed values conditions should be used");
        Assert.isTrue(columnRelations.isEmpty(), "All column relations should be used");
        
        Optional<BooleanExpression> optimizedWhereCondition = restOfExpressions.isEmpty() ?
                Optional.empty() :
                Optional.of(
                        restOfExpressions.stream()
                          .reduce(alwaysTrue(), (l, r) -> new BinaryBooleanExpression(l, AND, r))
                );
        
        return new Pair<>(optimizedDataSource, optimizedWhereCondition);
    }

    private DataSource replaceNaturalJoin(DataSource dataSource, DataSource previousDataSource, Map<String, String> tableAliases) {
        if (!dataSource.isNaturalJoin()) {
            return dataSource;
        }
        checkLeftDataSource(dataSource, false);
        checkLeftDataSource(previousDataSource, false);
        Set<String> similarColumns = getSimilarColumns(previousDataSource, dataSource, tableAliases);
        dataSource.setNaturalJoin(false);
        dataSource.getColumns().clear();
        dataSource.getColumns().addAll(similarColumns);
        return dataSource;
    }

    private Set<String> getSimilarColumns(DataSource left, DataSource right, Map<String, String> tableAliases) {
        String leftTableName = tableAliases.get(left.getTableAlias().get());
        String rightTableName = tableAliases.get(right.getTableAlias().get());
        Set<String> leftColumnNames = new HashSet<>(columnsToNames(tablesAware.getColumns(leftTableName)));
        Set<String> rightColumnNames = new HashSet<>(columnsToNames(tablesAware.getColumns(rightTableName)));
        return intersect(leftColumnNames, rightColumnNames);
    }
    
    private boolean isSuitableForIndexFetch(DataSource ds, Map<String, String> tableAliases, Map<String, Set<String>> columnNamesToSelect) {
        boolean simpleChecksPassed =
                !ds.getJoinType().isPresent() && //Does not participate in join
                !ds.getRightDataSource().isPresent(); 
        if (simpleChecksPassed) {
            String tableAlias = ds.getTableAlias().get();
            Set<String> tableColumnNamesToSelect = columnNamesToSelect.get(tableAlias);
            String tableName = tableAliases.get(tableAlias);
            IndexSignature indexSignature = new IndexSignature(Collections.singletonMap(tableName, tableColumnNamesToSelect));
            Optional<Index> indexCandidate = tablesAware.getIndex(indexSignature);
            return indexCandidate.isPresent();
        }
        return false;
    }

    private Optional<IndexBooleanExpression> getIndexScanBooleanExpression(String tableAlias, Map<String, String> tableAliases, Map<String, Map<String, Set<Object>>> fixedValuesConditions) {
        String tableName = tableAliases.get(tableAlias);
        boolean hasFixedValuesConditions = fixedValuesConditions.containsKey(tableAlias);
        if (hasFixedValuesConditions) {
            Map<String, Set<Object>> tableFixedValuesConditions = fixedValuesConditions.get(tableAlias);
            Map<String, Set<Object>> matchingFixedValuesConditions = new HashMap<>();
            for (String columnName : tableFixedValuesConditions.keySet()) {
                Optional<Column> columnCandidate = tablesAware.getColumn(tableName, columnName);
                Assert.isTrue(columnCandidate.isPresent(), String.format("Column %s should be present in table %s", columnName, tableName));
                Column column = columnCandidate.get();
                for (IndexSignature indexSignature : column.getIndexes()) {
                    Set<String> allIndexColumns = indexSignature.getDesiredColumns().get(tableName);
                    allIndexColumns.removeAll(tableFixedValuesConditions.keySet());
                    if (allIndexColumns.isEmpty()) {
                        //I.e. all index columns of at least one index are present in fixed value conditions
                        matchingFixedValuesConditions.put(columnName, tableFixedValuesConditions.remove(columnName));
                    }
                }
            }
            if (!matchingFixedValuesConditions.isEmpty()) {
                return Optional.of(new IndexBooleanExpression(tableAlias, matchingFixedValuesConditions));
            }
        }
        return Optional.empty();
    }

    private Set<String> getIndexedRelationColumns(String tableAlias, Map<String, String> tableAliases, ColumnRelations columnRelations) {
        Map<String, Set<String>> allColumnRelations = columnRelations.getRelations(tableAlias);
        boolean hasColumnRelations = !allColumnRelations.isEmpty();
        if (hasColumnRelations) {

            for (String relationTableAlias : allColumnRelations.keySet()) {
                String relationTableName = tableAliases.get(relationTableAlias);
                for (String columnName : allColumnRelations.get(relationTableAlias)) {
                    Optional<Column> columnCandidate = tablesAware.getColumn(relationTableName, columnName);
                    Assert.isTrue(columnCandidate.isPresent(), String.format("Column %s should be present in table %s", columnName, relationTableName));
                    Column column = columnCandidate.get();
                    if (column.getIndexes().isEmpty()) {
                        return Collections.emptySet();
                    }
                }
            }
            //Currently we require that all columns should be present in indexes.
            //Later this can be relaxed e.g. for inner joins.
            return columnRelations.removeRelations(tableAlias);
        }
        return Collections.emptySet();
    }
    
    private static Consumer<BooleanExpression> getFixedValuesConsumer(Map<String, Map<String, Set<Object>>> fixedValuesConditions) {
        return be -> be.getTableAliases().forEach(ta -> fixedValuesConditions.put(ta, be.getFixedValueConditions(ta)));
    }
    
    private static Consumer<BooleanExpression> getColumnRelationsConsumer(ColumnRelations columnRelations) {
        return be -> columnRelations.add(be.getColumnRelations());
    }

    private static Consumer<BooleanExpression> getRestOfExpressionsConsumer(List<BooleanExpression> restOfExpressions) {
        return be -> {
            Optional<BooleanExpression> restOfExpressionCandidate = be.getRestOfExpression();
            if (restOfExpressionCandidate.isPresent()) {
                restOfExpressions.add(restOfExpressionCandidate.get());
            }
        };
    }
    
    private Map<String, Set<String>> getColumnNamesToSelect(
            Map<String, Object> selectionMap,
            Map<String, String> tableAliases,
            Map<String, Map<String, Set<Object>>> fixedValuesConditions,
            ColumnRelations columnRelations,
            List<BooleanExpression> restOfExpressions
    ) {
        final Map<String, Set<String>> columnNamesToSelect = new HashMap<>();
        tableAliases.keySet().forEach(tableAlias -> {
            columnNamesToSelect.putIfAbsent(tableAlias, new HashSet<>());
            columnNamesToSelect.get(tableAlias).addAll(getColumnNamesToSelect(tableAlias, selectionMap));
            columnNamesToSelect.get(tableAlias).addAll(columnRelations.getColumnNames(tableAlias));
            columnNamesToSelect.get(tableAlias).addAll(getRestOfExpressionsColumnNamesToSelect(tableAlias, restOfExpressions));
            if (fixedValuesConditions.containsKey(tableAlias)) {
                columnNamesToSelect.get(tableAlias).addAll(fixedValuesConditions.get(tableAlias).keySet());
            }
        });
        return columnNamesToSelect;
    }
    
    private Set<String> getColumnNamesToSelect(String tableAlias, Map<String, Object> selectionMap) {
        return selectionMap.values().stream()
                .flatMap(v -> expressionEvaluator.getColumnNames(v).getOrDefault(tableAlias, Collections.emptySet()).stream())
                .collect(Collectors.toSet());
    }

    private Set<String> getRestOfExpressionsColumnNamesToSelect(String tableAlias, List<BooleanExpression> restOfExpressions) {
        return restOfExpressions.stream()
                .flatMap(be -> expressionEvaluator.getColumnNames(be).getOrDefault(tableAlias, Collections.emptySet()).stream())
                .collect(Collectors.toSet());
    }

    private static Optional<BooleanExpression> fixedValuesToBooleanExpression(String tableAlias, Map<String, Set<Object>> fixedValueConditions) {
        if (fixedValueConditions == null || fixedValueConditions.isEmpty()) {
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
    
    private static void iterateDataSource(DataSource dataSource, BiConsumer<Optional<DataSource>, DataSource> dataSourceConsumer) {
        iterateDataSource(Optional.empty(), Optional.ofNullable(dataSource), dataSourceConsumer);
    }
    
    private static void iterateDataSource(
            Optional<DataSource> previousDataSourceCandidate,
            Optional<DataSource> dataSourceCandidate,
            BiConsumer<Optional<DataSource>, DataSource> dataSourceConsumer
    ) {
        if (!dataSourceCandidate.isPresent()) {
            return;
        }
        DataSource dataSource = dataSourceCandidate.get();
        dataSourceConsumer.accept(previousDataSourceCandidate, dataSource);
        Assert.isTrue(dataSource.getTableAlias().isPresent(), "Original query should have table alias");
        Assert.isTrue(!dataSource.getLeftDataSource().isPresent(), "Original data source should not contain nested data sources");
        iterateDataSource(dataSourceCandidate, dataSource.getRightDataSource(), dataSourceConsumer);
    }

    private Task createFilterTask(BooleanExpression booleanExpression) {
        FilterTask filterTask = applicationContext.getBean(FilterTask.class);
        filterTask.setCondition(booleanExpression);
        return filterTask;
    }
    
    private Task createDataSourceTask(DataSource dataSource, Map<String, String> tableAliases) {
        return applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                tableAliases
        );
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
    
    private static class ColumnRelations {

        private final Map<String, Set<String>> columnRelationsColumns = new HashMap<>();
        private final Map<String, Map<String, Set<String>>> allColumnRelations = new HashMap<>();
        
        Set<String> getColumnNames(String tableAlias){
            return columnRelationsColumns.getOrDefault(tableAlias, Collections.emptySet());
        }
        
        Map<String, Set<String>> getRelations(String tableAlias) {
            return allColumnRelations.getOrDefault(tableAlias, Collections.emptyMap());
        }

        Set<String> removeRelations(String tableAlias) {
            allColumnRelations.remove(tableAlias);
            return columnRelationsColumns.remove(tableAlias);
        }

        void add(Map<String, Set<String>> data){
            data.keySet().forEach(tableAlias -> {
                allColumnRelations.put(tableAlias, data);
                columnRelationsColumns.putIfAbsent(tableAlias, new HashSet<>());
                columnRelationsColumns.get(tableAlias).addAll(data.getOrDefault(tableAlias, Collections.emptySet()));
            });
        }
        
        boolean isEmpty(){
            return columnRelationsColumns.isEmpty() && allColumnRelations.isEmpty();
        }
        
    }

}
