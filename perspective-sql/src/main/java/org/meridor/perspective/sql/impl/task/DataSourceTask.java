package org.meridor.perspective.sql.impl.task;


import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataSourceTask implements Task {
    
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataFetcher dataFetcher;

    @Autowired
    private TablesAware tablesAware;

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    private final DataSource dataSource;
    private final Map<String, String> tableAliases = new HashMap<>();

    public DataSourceTask(DataSource dataSource, Map<String, String> tableAliases) {
        this.dataSource = dataSource;
        this.tableAliases.putAll(tableAliases);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        try {
            DataContainer data = fetchData();
            DataContainer result = (dataSource.getJoinType().isPresent()) ?
                    join(
                            previousTaskResult.getData(),
                            data
                    ) :
                    data;
            ExecutionResult executionResult = new ExecutionResult() {
                {
                    setData(result);
                    setCount(result.getRows().size());
                }
            };
            if (dataSource.getNextDataSource().isPresent()) {
                DataSourceTask nextTask = applicationContext.getBean(
                        DataSourceTask.class,
                        dataSource.getNextDataSource().get(),
                        tableAliases
                );
                return nextTask.execute(executionResult);
            }
            return executionResult;
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }
    
    private DataContainer fetchData() throws SQLException {
        if (dataSource.getDataSource().isPresent()) {
            DataSourceTask childTask = applicationContext.getBean(
                    DataSourceTask.class,
                    dataSource.getDataSource().get(),
                    tableAliases
            );
            return childTask.execute(new ExecutionResult()).getData();
        } else if (dataSource.getTableAlias().isPresent()) {
            String tableAlias = dataSource.getTableAlias().get();
            return fetch(tableAlias, Collections.emptyList()); //TODO: change it!
        }
        throw new IllegalArgumentException("Datasource should either contain table name or another datasource");
    }
    
    //Here we assume that conditions contains only indexed columns. 
    //Query planner should guarantee that. 
    private DataContainer fetch(String tableAlias, List<Map<String, Object>> conditions) {
        String tableName = tableAliases.get(tableAlias);
        if (!conditions.isEmpty()) {
            Set<String> ids = getIdsFromIndex(tableName, conditions);
            return dataFetcher.fetch(tableName, tableAlias, ids, tablesAware.getColumns(tableName));
        }
        return dataFetcher.fetch(tableName, tableAlias, tablesAware.getColumns(tableName));
    }
    
    private Set<String> getIdsFromIndex(String tableName, List<Map<String, Object>> conditions) {
        return conditions.stream()
                .map(c -> getMatchedIds(tableName, c))
                .reduce(Collections.emptySet(), this::intersect);
    }
    
    private <T> Set<T> intersect(Set<T> first, Set<T> second) {
        if (first.isEmpty()) {
            return second;
        }
        first.retainAll(second);
        return first;
    }
    
    private Set<String> getMatchedIds(String tableName, Map<String, Object> condition) {
        Set<String> columnNames = condition.keySet();
        Map<String, Set<String>> desiredColumns = new HashMap<String, Set<String>>(){
            {
                put(tableName, columnNames);
            }
        };
        IndexSignature indexSignature = new IndexSignature(desiredColumns);
        Optional<Index> indexCandidate = tablesAware.getIndex(indexSignature);
        if (!indexCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "No index found for columns [%s]. This is probably a bug.", columnNames.stream().collect(Collectors.joining(","))
            ));
        }
        Index index = indexCandidate.get();
        Object[] values = condition.values().toArray(new Object[condition.values().size()]);
        Key key = Keys.create(index.getKeyLength(), values);
        return index.get(key).stream()
                .map(String.class::cast) //Simple string indexes always contain string ids
                .collect(Collectors.toSet());
    }
    
    private DataContainer join(DataContainer left, DataContainer right) {
        JoinType joinType = dataSource.getJoinType().get();
        List<String> joinColumns = dataSource.getJoinColumns();
        Optional<Object> joinCondition = dataSource.getCondition();
        boolean isNaturalJoin = dataSource.isNaturalJoin();
        if (isNaturalJoin) {
            List<String> similarColumns = getSimilarColumns(left, right);
            return joinByColumns(left, joinType, right, similarColumns);
        }
        return !joinColumns.isEmpty() ?
                joinByColumns(left, joinType, right, joinColumns) :
                joinByCondition(left, joinType, right, joinCondition);
    }
    
    private List<String> getSimilarColumns(DataContainer left, DataContainer right) {
        return left.getColumnNames().stream()
                .filter(cn -> right.getColumnNames().contains(cn))
                .collect(Collectors.toList());
    }
    
    private DataContainer joinByCondition(DataContainer left, JoinType joinType, DataContainer right, Optional<Object> joinCondition) {
        switch (joinType) {
            default:
            case INNER: return innerJoin(left, right, joinCondition);
            case LEFT:
            case RIGHT:{
                if (!joinCondition.isPresent()) {
                    throw new IllegalArgumentException("Join condition is mandatory for outer join");
                }
                return outerJoin(left, joinType, right, joinCondition.get());
            }
        }
    }
    
    private DataContainer joinByColumns(DataContainer left, JoinType joinType, DataContainer right, List<String> joinColumns) {
        String leftTableAlias = getTableAlias(left);
        String rightTableAlias = getTableAlias(right);
        Optional<Object> joinCondition = columnsToCondition(Optional.empty(), leftTableAlias, joinColumns, rightTableAlias);
        return joinByCondition(left, joinType, right, joinCondition);
    }
    
    private String getTableAlias(DataContainer dataContainer) {
        Set<String> tableAliases = dataContainer.getColumnsMap().keySet();
        if (tableAliases.size() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Data container should contain exactly one table alias but in fact it contains %d: %s",
                    tableAliases.size(),
                    tableAliases.stream().collect(Collectors.joining(", "))
            ));
        }
        return new ArrayList<>(tableAliases).get(0);
    }
    
    private Optional<Object> columnsToCondition(Optional<Object> joinCondition, String leftTableAlias, List<String> columnNames, String rightTableAlias) {
        if (columnNames.size() == 0) {
            return joinCondition;
        }
        return Optional.of(
                columnNames.stream()
                    .map(cn -> new SimpleBooleanExpression(
                            new ColumnExpression(cn, leftTableAlias),
                            BooleanRelation.EQUAL,
                            new ColumnExpression(cn, rightTableAlias)
                    ))
                    .reduce(
                            BinaryBooleanExpression.alwaysTrue(),
                            (f, s) -> new BinaryBooleanExpression(f, BinaryBooleanOperator.AND, s),
                            (f, s) -> new BinaryBooleanExpression(f, BinaryBooleanOperator.AND, s)
                    )
        );
    }
    
    //A naive implementation filtering cross join by condition
    private DataContainer innerJoin(DataContainer left, DataContainer right, Optional<Object> joinCondition) {
        return crossJoin(left, right, joinCondition, JoinType.INNER);
    }
    
    //We add a row with nulls to cross product, then relax join condition with is null clauses
    private DataContainer outerJoin(DataContainer left, JoinType joinType, DataContainer right, Object joinCondition) {
        return crossJoin(left, right, Optional.ofNullable(joinCondition), joinType);
    }

    //Based on http://stackoverflow.com/questions/9591561/java-cartesian-product-of-a-list-of-lists
    private DataContainer crossJoin(DataContainer left, DataContainer right, Optional<Object> joinCondition, JoinType joinType) {
        final List<DataRow> leftRows = left.getRows();
        final List<DataRow> rightRows = right.getRows();
        final int SIZE = leftRows.size() * rightRows.size();
        final Set<Integer> matchedIndexes = new HashSet<>();
        
        boolean isLeftJoin = joinType == JoinType.LEFT;
        boolean isRightJoin = joinType == JoinType.RIGHT;
        boolean isOuterJoin = isLeftJoin || isRightJoin;
        int leftColumnsCount = left.getColumnNames().size();
        int rightColumnsCount = right.getColumnNames().size();
        
        DataContainer dataContainer = mergeContainerColumns(left, right);
        for (int i = 0; i < SIZE; i++) {
            List<Object> newRowValues = new ArrayList<>();
            int j = 1;
            Integer currentIndex = null;
            boolean isLeftRow = true;
            for (List<DataRow> rowsList : new ArrayList<List<DataRow>>() {
                {
                    add(leftRows);
                    add(rightRows);
                }
            }) {
                final int index = ( i / j ) % rowsList.size();
                if (
                        (isLeftJoin && isLeftRow) ||
                        (isRightJoin && !isLeftRow)
                ) {
                    currentIndex = index;
                }
                DataRow dataRow = rowsList.get(index);
                List<Object> rowPart = dataRow.getValues();
                newRowValues.addAll(rowPart);
                j *= rowsList.size();
                isLeftRow = false;
            }
            
            DataRow dataRow = new DataRow(dataContainer, newRowValues);
            if (!joinCondition.isPresent() || expressionEvaluator.evaluateAs(joinCondition.get(), dataRow, Boolean.class)) {
                dataContainer.addRow(dataRow);
                if (isOuterJoin && (currentIndex != null) ) {
                    matchedIndexes.add(currentIndex);
                }
            }
        }
        
        if (isOuterJoin) {
            int rowsCount = isLeftJoin ? leftRows.size() : rightRows.size();
            for (int i = 0; i <= rowsCount - 1; i++) {
                if (!matchedIndexes.contains(i)) {
                    DataRow rowWithNulls = new DataRow(
                            dataContainer,
                            rowWithNullsValues(leftRows, leftColumnsCount, rightRows, rightColumnsCount, i, isLeftJoin)
                    );
                    dataContainer.addRow(rowWithNulls);
                }
            }
        }
        return dataContainer;
    }

    private List<Object> rowWithNullsValues(List<DataRow> leftRows, int leftColumnsCount, List<DataRow> rightRows, int rightColumnsCount, int index, boolean isLeftJoin) {
        List<Object> ret = new ArrayList<>();
        if (isLeftJoin) {
            ret.addAll(leftRows.get(index).getValues());
            ret.addAll(listWithNulls(rightColumnsCount));
        } else {
            ret.addAll(listWithNulls(leftColumnsCount));
            ret.addAll(rightRows.get(index).getValues());
        }
        return ret;
    }
    
    private List<Object> listWithNulls(int size) {
        Object[] array = new Object[size];
        Arrays.fill(array, null);
        return Arrays.asList(array);
    }
    
    private DataContainer mergeContainerColumns(DataContainer left, DataContainer right) {
        return new DataContainer(new LinkedHashMap<String, List<String>>(){
            {
                putAll(left.getColumnsMap());
                putAll(right.getColumnsMap());
            }
        });
    }
}
