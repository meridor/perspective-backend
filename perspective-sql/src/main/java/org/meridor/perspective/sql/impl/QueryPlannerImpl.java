package org.meridor.perspective.sql.impl;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.*;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
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

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.QueryPlannerImpl.OptimizedTask.*;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.AND;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.OR;
import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToCondition;
import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToNames;
import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.INDEX_FETCH;
import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.INDEX_SCAN;
import static org.meridor.perspective.sql.impl.parser.DataSourceUtils.*;
import static org.meridor.perspective.sql.impl.table.Column.ANY_TABLE;

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
    
    @Autowired
    private IndexStorage indexStorage;
    
    private final Queue<Task> tasksQueue = new LinkedList<>();

    @Override
    public QueryPlan plan(String sql) throws SQLException {
        QueryParser queryParser = applicationContext.getBean(QueryParser.class);
        queryParser.parse(sql);
        QueryType queryType = queryParser.getQueryType();
        switch (queryType) {
            case EXPLAIN:
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
        return new QueryPlanImpl(tasksQueue, queryType);
    }

    private void processSelectQuery(SelectQueryAware selectQueryAware) throws SQLException {
        try {
            
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
            
        } catch (Exception e) {
            throw new SQLException(e);
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

        OptimizationContext optimizationContext = analyzeOriginalData(originalDataSource, originalWhereConditions, tableAliases);

        DataSource optimizedDataSource = createOptimizedDataSource(originalDataSource, optimizationContext, selectionMap, tableAliases);

        Optional<BooleanExpression> optimizedWhereCondition = createOptimizedWhereCondition(optimizationContext);

        return new Pair<>(optimizedDataSource, optimizedWhereCondition);
    }

    private OptimizationContext analyzeOriginalData(DataSource originalDataSource, List<BooleanExpression> originalWhereConditions, Map<String, String> tableAliases) {
        //Assumption: original data source is a chain of data sources 
        // corresponding to a chain of joins in SQL request. This probably will
        // need to be reconsidered while implementing sub-queries.
        
        OptimizationContext optimizationContext = new OptimizationContext();
        originalWhereConditions.forEach(optimizationContext::addExpression);

        //Analyzing data sources
        iterateDataSource(originalDataSource, (pds, ds, nds) -> {
            if (pds.isPresent()) {
                replaceNaturalJoin(ds, pds.get(), tableAliases);
            }

            if (ds.getCondition().isPresent()) {
                BooleanExpression booleanExpression = ds.getCondition().get();
                optimizationContext.addExpression(booleanExpression);
            }

            String tableAlias = ds.getTableAlias().get();
            if (pds.isPresent() && pds.get().getTableAlias().isPresent() && !ds.getColumns().isEmpty()) {
                String previousTableAlias = pds.get().getTableAlias().get();
                Optional<BooleanExpression> columnsBooleanExpression = columnsToCondition(Optional.empty(), previousTableAlias, ds.getColumns(), tableAlias);
                if (columnsBooleanExpression.isPresent()) {
                    optimizationContext.addExpression(columnsBooleanExpression.get());
                }
            }
        });
        
        return optimizationContext;
    }
    
    private void replaceNaturalJoin(DataSource dataSource, DataSource previousDataSource, Map<String, String> tableAliases) {
        if (!dataSource.isNaturalJoin()) {
            return;
        }
        checkLeftDataSource(dataSource, false);
        checkLeftDataSource(previousDataSource, false);
        Set<String> similarColumns = getSimilarColumns(previousDataSource, dataSource, tableAliases);
        dataSource.setNaturalJoin(false);
        dataSource.getColumns().clear();
        //This will be later replaced by condition and transformed to column relations
        dataSource.getColumns().addAll(similarColumns);
    }

    private DataSource createOptimizedDataSource(DataSource originalDataSource, OptimizationContext optimizationContext, Map<String, Object> selectionMap, Map<String, String> tableAliases) {

        final Map<String, Set<String>> columnNamesToSelect = getColumnNamesToSelect(selectionMap, tableAliases, optimizationContext);

        final DataSource optimizedDataSource = new DataSource();
        
        //Flags used to group scan data sources with joins by pairs
        final AtomicBoolean firstIndexScanDataSourceAdded = new AtomicBoolean();
        
        /*
            Here we create optimized data source as a tree:
            
            root
                parent
                    parent
                        item1
                        item2
                    item3
                item4
            
            An item can be either an index fetch datasource or index scan datasource
            or an index scan foreign key join or a table scan data source. Such form
            guarantees that joins are processed in correct order so that data container
            on each iteration has all columns for table scan condition checking.

         */
        
        iterateDataSource(originalDataSource, (pds, ds, nds) -> {

            DataSource optimizedChildDataSource = ds.copy();

            String tableAlias = ds.getTableAlias().get();

            if (isSuitableForIndexFetch(optimizedChildDataSource, tableAliases, columnNamesToSelect)) {
                optimizedChildDataSource.setType(INDEX_FETCH);
                Set<String> tableColumnNamesToSelect = columnNamesToSelect.get(tableAlias);
                optimizedChildDataSource.getColumns().clear();
                optimizedChildDataSource.getColumns().addAll(tableColumnNamesToSelect);
                firstIndexScanDataSourceAdded.set(false);
                addToDataSource(optimizedDataSource, optimizedChildDataSource);
            } else {

                ColumnRelationsStorage columnRelations = optimizationContext.getColumnRelations();
                
                Optional<String> foreignTableAliasCandidate = firstIndexScanDataSourceAdded.get() ?
                        ( pds.isPresent() ? pds.get().getTableAlias() : Optional.empty() ):
                        ( nds.isPresent() ? nds.get().getTableAlias() : Optional.empty() );
                List<ColumnRelation> indexedColumnRelations = foreignTableAliasCandidate.isPresent() ?
                        getIndexedColumnRelations(tableAlias, foreignTableAliasCandidate.get(), tableAliases, columnRelations):
                        Collections.emptyList();
                Map<String, Map<String, Set<Object>>> fixedValuesConditions = optimizationContext.getFixedValuesConditions();
                
                Optional<IndexBooleanExpression> indexScanBooleanExpressionCandidate = getIndexScanBooleanExpression(tableAlias, tableAliases, fixedValuesConditions);
                DataSource tailDataSource = getTail(optimizedDataSource);

                if (!indexedColumnRelations.isEmpty()) {
                    //Index foreign key join
                    optimizedChildDataSource.setType(INDEX_SCAN);
                    optimizedChildDataSource.getColumns().clear();
                    IndexBooleanExpression indexBooleanExpression = indexScanBooleanExpressionCandidate.isPresent() ?
                            indexScanBooleanExpressionCandidate.get() :
                            new IndexBooleanExpression();
                    optimizedChildDataSource.setRightDataSource(null);
                    optimizedChildDataSource.setCondition(indexBooleanExpression);

                    if (
                            tailDataSource.getType() == INDEX_SCAN &&
                            optimizedChildDataSource.getJoinType().isPresent() &&
                            firstIndexScanDataSourceAdded.get()
                    ) {
                        indexBooleanExpression.getColumnRelations().addAll(indexedColumnRelations); //Column relations go to the second datasource in pair
                        tailDataSource.setRightDataSource(optimizedChildDataSource);
                        firstIndexScanDataSourceAdded.set(false);
                    } else {
                        firstIndexScanDataSourceAdded.set(true);
                        addToDataSource(optimizedDataSource, optimizedChildDataSource);
                    }
                } else if (indexScanBooleanExpressionCandidate.isPresent()) {
                    //Simple index scan
                    optimizedChildDataSource.setType(INDEX_SCAN);
                    optimizedChildDataSource.setCondition(indexScanBooleanExpressionCandidate.get());
                    addToDataSource(optimizedDataSource, optimizedChildDataSource);
                } else {
                    //Add table scan data source either to previous data source or as a separate tree leaf
                    firstIndexScanDataSourceAdded.set(false);
                    optimizedChildDataSource.setRightDataSource(null);
                    if (optimizedChildDataSource.getJoinType().isPresent()) {
                        Optional<BooleanExpression> joinCondition = optimizedChildDataSource.getCondition();
                        if (joinCondition.isPresent()) {
                            columnRelations.removeRelations(joinCondition.get());
                        }
                        Optional<BooleanExpression> booleanExpressionCandidate = fixedValuesToBooleanExpression(tableAlias, fixedValuesConditions.remove(tableAlias));
                        Optional<BooleanExpression> updatedJoinCondition = intersectConditions(joinCondition, booleanExpressionCandidate);
                        if (updatedJoinCondition.isPresent()) {
                            optimizedChildDataSource.setCondition(updatedJoinCondition.get());
                        }
                    }
                    addToDataSource(optimizedDataSource, optimizedChildDataSource);
                }
            }

        });
        return optimizedDataSource;
    } 
    
    private Optional<BooleanExpression> createOptimizedWhereCondition(OptimizationContext optimizationContext) {
        
        //Not used fixed values conditions should be moved to where clause, e.g. index fetch strategy does not support conditions

        Map<String, Map<String, Set<Object>>> fixedValuesConditions = optimizationContext.getFixedValuesConditions();
        ColumnRelationsStorage columnRelations = optimizationContext.getColumnRelations();
        List<BooleanExpression> restOfExpressions = optimizationContext.getRestOfExpressions();

        Set<String> fixedValuesTableAliases = new HashSet<>(fixedValuesConditions.keySet());
        fixedValuesTableAliases.forEach(tableAlias -> {
            Optional<BooleanExpression> booleanExpressionCandidate = fixedValuesToBooleanExpression(tableAlias, fixedValuesConditions.remove(tableAlias));
            if (booleanExpressionCandidate.isPresent()) {
                restOfExpressions.add(booleanExpressionCandidate.get());
            }
        });

        //Not used column relations should be moved to where clause
        Optional<BooleanExpression> columnRelationsAsBooleanExpression = columnRelations.getAllAsBooleanExpression();
        if (columnRelationsAsBooleanExpression.isPresent()) {
            restOfExpressions.add(columnRelationsAsBooleanExpression.get());
        }
        columnRelations.clear();

        Assert.isTrue(fixedValuesConditions.isEmpty(), "All fixed values conditions should be used");
        Assert.isTrue(columnRelations.isEmpty(), "All column relations should be used");

        return restOfExpressions.stream()
                .reduce((l, r) -> new BinaryBooleanExpression(l, AND, r));
    }
    
    private Set<String> getSimilarColumns(DataSource left, DataSource right, Map<String, String> tableAliases) {
        String leftTableName = tableAliases.get(left.getTableAlias().get());
        String rightTableName = tableAliases.get(right.getTableAlias().get());
        Set<String> leftColumnNames = new HashSet<>(columnsToNames(tablesAware.getColumns(leftTableName)));
        Set<String> rightColumnNames = new HashSet<>(columnsToNames(tablesAware.getColumns(rightTableName)));
        return intersection(leftColumnNames, rightColumnNames);
    }
    
    private boolean isSuitableForIndexFetch(DataSource ds, Map<String, String> tableAliases, Map<String, Set<String>> columnNamesToSelect) {
        boolean simpleChecksPassed =
                !ds.getJoinType().isPresent() && //Does not participate in join
                !ds.getRightDataSource().isPresent(); 
        if (simpleChecksPassed) {
            String tableAlias = ds.getTableAlias().get();
            Set<String> tableColumnNamesToSelect = new HashSet<>(columnNamesToSelect.get(tableAlias));
            if (columnNamesToSelect.containsKey(ANY_TABLE)) {
                tableColumnNamesToSelect.addAll(columnNamesToSelect.get(ANY_TABLE));
            }
            String tableName = tableAliases.get(tableAlias);
            IndexSignature indexSignature = new IndexSignature(tableName, tableColumnNamesToSelect);
            Optional<Index> indexCandidate = indexStorage.get(indexSignature);
            return indexCandidate.isPresent();
        }
        return false;
    }

    private Optional<IndexBooleanExpression> getIndexScanBooleanExpression(
            String tableAlias,
            Map<String, String> tableAliases,
            Map<String, Map<String, Set<Object>>> fixedValuesConditions
    ) {
        String tableName = tableAliases.get(tableAlias);
        preprocessFixedValueConditions(tableAlias, tableAliases, fixedValuesConditions);
        boolean hasFixedValuesConditions = fixedValuesConditions.containsKey(tableAlias);
        if (hasFixedValuesConditions) {
            Map<String, Set<Object>> tableFixedValuesConditions = fixedValuesConditions.get(tableAlias);
            Map<String, Set<Object>> matchingFixedValuesConditions = new HashMap<>();
            for (String columnName : tableFixedValuesConditions.keySet()) {
                Optional<Column> columnCandidate = tablesAware.getColumn(tableName, columnName);
                Assert.isTrue(columnCandidate.isPresent(), String.format("Column %s should be present in table %s", columnName, tableName));
                Column column = columnCandidate.get();
                for (IndexSignature indexSignature : column.getIndexes(indexStorage.getSignatures())) {
                    Set<String> allIndexColumns = indexSignature.getDesiredColumns().get(tableName);
                    allIndexColumns.removeAll(tableFixedValuesConditions.keySet());
                    if (allIndexColumns.isEmpty()) {
                        //I.e. all index columns of at least one index are present in fixed value conditions
                        matchingFixedValuesConditions.put(columnName, tableFixedValuesConditions.remove(columnName));
                    }
                }
            }
            if (fixedValuesConditions.get(tableAlias).isEmpty()) {
                fixedValuesConditions.remove(tableAlias);
            }
            if (!matchingFixedValuesConditions.isEmpty()) {
                return Optional.of(new IndexBooleanExpression(matchingFixedValuesConditions));
            }
        }
        return Optional.empty();
    }
    
    //Moves conditions with * table alias to respective real aliases if possible
    private void preprocessFixedValueConditions(
            String tableAlias,
            Map<String, String> tableAliases,
            Map<String, Map<String, Set<Object>>> fixedValuesConditions
    ) {
        if (fixedValuesConditions.containsKey(ANY_TABLE)) {
            HashSet<String> columnsWithAnyTable = new HashSet<>(fixedValuesConditions.get(ANY_TABLE).keySet());
            columnsWithAnyTable.forEach(columnName -> {
                Set<String> columnTableAliases = getColumnTableAliases(columnName, tableAliases);
                if (columnTableAliases.size() == 1) {
                    Map<String, Set<Object>> additionalValues = new HashMap<String, Set<Object>>(){
                        {
                            put(columnName, fixedValuesConditions.get(ANY_TABLE).remove(columnName));
                        }
                    };
                    fixedValuesConditions.merge(tableAlias, additionalValues, ExpressionUtils::mergeFixedValueConditions);
                }
            });

            if (fixedValuesConditions.get(ANY_TABLE).isEmpty()) {
                fixedValuesConditions.remove(ANY_TABLE);
            }
        }
    }
    
    private Set<String> getColumnTableAliases(String columnName, Map<String, String> tableAliases) {
        Set<String> ret = new HashSet<>();
        tableAliases.keySet().forEach(tableAlias -> {
            String tableName = tableAliases.get(tableAlias);
            if (tablesAware.getColumn(tableName, columnName).isPresent()) {
                ret.add(tableAlias);
            }
        });
        return ret;
    }

    //Currently we require that all columns from relation should be present in indexes.
    //Later this can be relaxed e.g. for inner joins.
    private List<ColumnRelation> getIndexedColumnRelations(String tableAlias, String foreignTableAlias, Map<String, String> tableAliases, ColumnRelationsStorage columnRelations) {
        List<ColumnRelation> ret = new ArrayList<>();
        List<ColumnRelation> matchedRelations = columnRelations.getRelations(tableAlias, foreignTableAlias);
        boolean hasColumnRelations = !matchedRelations.isEmpty();
        if (hasColumnRelations) {
            matchedRelations.forEach(matchedColumnRelation -> {
                for (ColumnRelation columnRelation : matchedColumnRelation.toList()) {
                    String leftTableName = tableAliases.get(columnRelation.getLeftTableAlias());
                    IndexSignature leftIndexSignature = new IndexSignature(leftTableName, Collections.singleton(columnRelation.getLeftColumn()));
                    String rightTableName = tableAliases.get(columnRelation.getRightTableAlias());
                    IndexSignature rightIndexSignature = new IndexSignature(rightTableName, Collections.singleton(columnRelation.getRightColumn()));
                    if (
                            !indexStorage.getSignatures().contains(leftIndexSignature) ||
                            !indexStorage.getSignatures().contains(rightIndexSignature)
                    ) {
                        return;
                    }
                }
                ret.add(matchedColumnRelation);
                columnRelations.removeRelation(tableAlias, foreignTableAlias, matchedColumnRelation);
            });
        }
        return ret;
    }
    
    
    private Map<String, Set<String>> getColumnNamesToSelect(
            Map<String, Object> selectionMap,
            Map<String, String> tableAliases,
            OptimizationContext optimizationContext
    ) {
        final Map<String, Set<String>> columnNamesToSelect = new HashMap<>();
        Set<String> allTableAliases = new HashSet<>(tableAliases.keySet());
        allTableAliases.add(ANY_TABLE);
        allTableAliases.forEach(tableAlias -> {
            columnNamesToSelect.putIfAbsent(tableAlias, new HashSet<>());
            columnNamesToSelect.get(tableAlias).addAll(getColumnNamesToSelect(tableAlias, selectionMap));
            columnNamesToSelect.get(tableAlias).addAll(optimizationContext.getColumnRelations().getColumnNames(tableAlias));
            columnNamesToSelect.get(tableAlias).addAll(getRestOfExpressionsColumnNamesToSelect(tableAlias, optimizationContext.getRestOfExpressions()));
            Map<String, Map<String, Set<Object>>> fixedValuesConditions = optimizationContext.getFixedValuesConditions();
            if (fixedValuesConditions.containsKey(tableAlias)) {
                columnNamesToSelect.get(tableAlias).addAll(fixedValuesConditions.get(tableAlias).keySet());
            }
        });
        return Collections.unmodifiableMap(columnNamesToSelect);
    }
    
    private Set<String> getColumnNamesToSelect(String tableAlias, Map<String, Object> selectionMap) {
        return selectionMap.values().stream()
                .flatMap(v -> {
                    Map<String, Set<String>> entryColumnNames = expressionEvaluator.getColumnNames(v);
                    return entryColumnNames.containsKey(tableAlias) ?
                            entryColumnNames.getOrDefault(tableAlias, Collections.emptySet()).stream() :
                            entryColumnNames.getOrDefault(ANY_TABLE, Collections.emptySet()).stream();
                })
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
        
        return fixedValueConditions.keySet().stream()
                .map(columnName -> {
                    Set<Object> values = fixedValueConditions.get(columnName);
                    Assert.isTrue(!values.isEmpty(), "Column values can't be empty on this stage");
                    return values.stream()
                            .map(v -> (BooleanExpression) new SimpleBooleanExpression(new ColumnExpression(columnName, tableAlias), BooleanRelation.EQUAL, v))
                            .reduce((l, r) -> new BinaryBooleanExpression(l, OR, r)).get();
                })
                .reduce((l, r) -> new BinaryBooleanExpression(l, AND, r));
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
    
    private static class OptimizationContext {
        private final Map<String, Map<String, Set<Object>>> fixedValuesConditions = new HashMap<>();
        private final ColumnRelationsStorage columnRelations = new ColumnRelationsStorage();
        private final List<BooleanExpression> restOfExpressions = new ArrayList<>();

        void addExpression(BooleanExpression booleanExpression) {
            getFixedValuesConsumer(fixedValuesConditions).accept(booleanExpression);
            getColumnRelationsConsumer(columnRelations).accept(booleanExpression);
            getRestOfExpressionsConsumer(restOfExpressions).accept(booleanExpression);
        }

        private static Consumer<BooleanExpression> getFixedValuesConsumer(Map<String, Map<String, Set<Object>>> fixedValuesConditions) {
            return be -> be.getTableAliases().forEach(ta -> {
                Map<String, Set<Object>> fvc = be.getFixedValueConditions(ta);
                if (!fvc.isEmpty()) {
                    fixedValuesConditions.put(ta, fvc);
                }
            });
        }

        private static Consumer<BooleanExpression> getColumnRelationsConsumer(ColumnRelationsStorage columnRelations) {
            return be -> columnRelations.addAll(be.getColumnRelations());
        }

        private static Consumer<BooleanExpression> getRestOfExpressionsConsumer(List<BooleanExpression> restOfExpressions) {
            return be -> {
                Optional<BooleanExpression> restOfExpressionCandidate = be.getRestOfExpression();
                if (restOfExpressionCandidate.isPresent()) {
                    restOfExpressions.add(restOfExpressionCandidate.get());
                }
            };
        }

        //Here we intentionally return fields by reference 
        Map<String, Map<String, Set<Object>>> getFixedValuesConditions() {
            return fixedValuesConditions;
        }

        ColumnRelationsStorage getColumnRelations() {
            return columnRelations;
        }

        List<BooleanExpression> getRestOfExpressions() {
            return restOfExpressions;
        }
    }
    
    private static class ColumnRelationsStorage {

        private final Map<String, List<ColumnRelation>> rawColumnRelations = new HashMap<>();

        Set<String> getColumnNames(String tableAlias){
            return getAllColumnRelationsStream()
                    .map(rcr -> columnRelationToColumns(rcr, tableAlias))
                    .reduce(new HashSet<>(), (l, r) -> {
                        l.addAll(r);
                        return l;
                    });
        }
        
        private Stream<ColumnRelation> getAllColumnRelationsStream() {
            return rawColumnRelations.values().stream()
                    .flatMap(Collection::stream);
        }
        
        List<ColumnRelation> getRelations(String tableAlias, String foreignTableAlias) {
            String key = getKey(tableAlias, foreignTableAlias);
            return rawColumnRelations.containsKey(key) ?
                    new ArrayList<>(rawColumnRelations.get(key)) :
                    Collections.emptyList();
        }

        private static Set<String> columnRelationToColumns(ColumnRelation columnRelation, String tableAlias) {
            return new LinkedHashSet<String>(){
                        {
                            if (!ANY_TABLE.equals(tableAlias)) {
                                columnRelation.toList()
                                    .forEach(cr -> {
                                        if (
                                                cr.getLeftTableAlias().equals(tableAlias) ||
                                                cr.getRightTableAlias().equals(tableAlias)
                                        ) {
                                            add(cr.getColumnName(tableAlias));
                                        }
                                    });
                            }
                        }
                    };
        }

        private static String getKey(String tableAlias, String foreignTableAlias) {
            return String.format("%s_%s", tableAlias, foreignTableAlias);
        }
        
        private List<String> getKeys(String tableAlias, String foreignTableAlias) {
            return Arrays.asList(getKey(tableAlias, foreignTableAlias), getKey(foreignTableAlias, tableAlias));
        }
        
        Optional<BooleanExpression> getAllAsBooleanExpression() {
            return getAllColumnRelationsStream()
                    .map(ColumnRelation::toBooleanExpression)
                    .reduce((l, r) -> new BinaryBooleanExpression(l, AND, r));
        }

        void removeRelation(String tableAlias, String foreignTableAlias, ColumnRelation columnRelation) {
            rawColumnRelations.get(getKey(tableAlias, foreignTableAlias)).remove(columnRelation);
        }
        
        void removeRelations(BooleanExpression booleanExpression) {
            if (booleanExpression == null || booleanExpression.getColumnRelations().isEmpty()) {
                return;
            }
            booleanExpression.getColumnRelations().forEach(cr -> {
                removeRelation(cr.getLeftTableAlias(), cr.getRightTableAlias(), cr);
                removeRelation(cr.getRightTableAlias(), cr.getLeftTableAlias(), cr);
            });
        }
        
        void clear() {
            rawColumnRelations.clear();
        }

        void addAll(Collection<ColumnRelation> columnRelations) {
            columnRelations.forEach(cr -> getKeys(cr.getLeftTableAlias(), cr.getRightTableAlias())
                    .forEach(k -> {
                        rawColumnRelations.putIfAbsent(k, new ArrayList<>());
                        rawColumnRelations.get(k).add(cr);
                    }));
        }
        
        boolean isEmpty(){
            return rawColumnRelations.isEmpty();
        }
        
    }

}
