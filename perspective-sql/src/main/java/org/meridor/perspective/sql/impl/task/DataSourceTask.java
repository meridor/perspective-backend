package org.meridor.perspective.sql.impl.task;


import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.function.FunctionName;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.DataType;
import org.meridor.perspective.sql.impl.table.TableName;
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
            TableName tableName = TableName.fromString(tableAliases.get(tableAlias));
            return dataFetcher.fetch(tableName, tableAlias, tablesAware.getColumns(tableName));
        }
        throw new IllegalArgumentException("Datasource should either contain table name or another datasource");
    }
    
    private DataContainer join(DataContainer left, DataContainer right) {
        JoinType joinType = dataSource.getJoinType().get();
        List<String> joinColumns = dataSource.getJoinColumns();
        Optional<Object> joinCondition = dataSource.getJoinCondition();
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
            throw new IllegalArgumentException(String.format("Data container should contain exactly one table alias but in fact it contains %d", tableAliases.size()));
        }
        return new ArrayList<>(tableAliases).get(0);
    }
    
    private Optional<Object> columnsToCondition(Optional<Object> joinCondition, String leftTableAlias, List<String> columnNames, String rightTableAlias) {
        if (columnNames.size() == 0) {
            return joinCondition;
        }
        String columnName = columnNames.remove(0);
        SimpleBooleanExpression simpleBooleanExpression = new SimpleBooleanExpression(
                new ColumnExpression(columnName, leftTableAlias),
                BooleanRelation.EQUAL,
                new ColumnExpression(columnName, rightTableAlias)
        );
        BinaryBooleanExpression nextJoinCondition = joinCondition.isPresent() ?
                new BinaryBooleanExpression(joinCondition.get(), BinaryBooleanOperator.AND, simpleBooleanExpression) :
                new BinaryBooleanExpression(true, BinaryBooleanOperator.AND, simpleBooleanExpression);
        return columnsToCondition(Optional.of(nextJoinCondition), leftTableAlias, columnNames, rightTableAlias);
    }
    
    //A naive implementation filtering cross join by condition
    private DataContainer innerJoin(DataContainer left, DataContainer right, Optional<Object> joinCondition) {
        DataContainer crossJoin = crossJoin(left, right);
        return joinCondition.isPresent() ?
                new DataContainer(
                        crossJoin,
                        rows -> rows.stream()
                                .filter(row -> expressionEvaluator.evaluateAs(joinCondition.get(), row, Boolean.class))
                                .collect(Collectors.toList())
                ) : crossJoin;
    }
    
    //We add a row with nulls to cross product, then relax join condition with is null clauses
    private DataContainer outerJoin(DataContainer left, JoinType joinType, DataContainer right, Object joinCondition) {
        DataContainer containerToAddNullRow = joinType.equals(JoinType.LEFT) ?
                right : left;
        List<Object> rowWithNulls = containerToAddNullRow.getColumnNames().stream()
                .map(cn -> null)
                .collect(Collectors.toList());
        containerToAddNullRow.addRow(rowWithNulls);
        String tableAlias = getTableAlias(containerToAddNullRow);
        Object newJoinCondition = convertJoinCondition(joinCondition, containerToAddNullRow.getColumnNames(), tableAlias);
        return innerJoin(left, right, Optional.of(newJoinCondition));
    }
    
    //Converts join condition like a.col = b.col to a.col = b.col or b.col is null 
    private Object convertJoinCondition(Object joinCondition, List<String> columnNames, String rightTableAlias) {
        if (columnNames.isEmpty()) {
            return joinCondition;
        }
        String columnName = columnNames.remove(0);
        ColumnExpression columnExpression = new ColumnExpression(columnName, rightTableAlias);
        FunctionExpression isNullExpression = new FunctionExpression(FunctionName.TYPEOF.name(), Arrays.asList(columnExpression, DataType.NULL));
        Object newJoinCondition = new BinaryBooleanExpression(joinCondition, BinaryBooleanOperator.OR, isNullExpression);
        return convertJoinCondition(newJoinCondition, columnNames, rightTableAlias);
    }
    
    //Based on http://stackoverflow.com/questions/9591561/java-cartesian-product-of-a-list-of-lists
    private DataContainer crossJoin(DataContainer left, DataContainer right) {
        final List<DataRow> leftRows = left.getRows();
        final List<DataRow> rightRows = right.getRows();
        final int SIZE = leftRows.size() * rightRows.size();
        DataContainer dataContainer = mergeContainerColumns(left, right);
        for (int i = 0; i < SIZE; i++) {
            List<Object> newRow = new ArrayList<>();
            int j = 1;
            for (List<DataRow> rowsList : new ArrayList<List<DataRow>>() {
                {
                    add(leftRows);
                    add(rightRows);
                }
            }) {
                final int index = ( i / j ) % rowsList.size();
                DataRow dataRow = rowsList.get(index);
                newRow.addAll(dataRow.getValues());
                j *= rowsList.size();
            }
            dataContainer.addRow(newRow);
        }
        return dataContainer;
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
