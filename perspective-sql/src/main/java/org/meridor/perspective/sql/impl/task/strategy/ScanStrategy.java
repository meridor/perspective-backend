package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.beans.BooleanRelation.EQUAL;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.AND;

@Component
public abstract class ScanStrategy implements DataSourceStrategy {

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    protected DataContainer join(DataContainer left, DataSource rightDataSource, DataContainer right) {
        JoinType joinType = rightDataSource.getJoinType().get();
        List<String> joinColumns = rightDataSource.getColumns();
        Optional<BooleanExpression> joinCondition = rightDataSource.getCondition();
        boolean isNaturalJoin = rightDataSource.isNaturalJoin();
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

    private DataContainer joinByCondition(DataContainer left, JoinType joinType, DataContainer right, Optional<BooleanExpression> joinCondition) {
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
        Optional<BooleanExpression> joinCondition = columnsToCondition(Optional.empty(), leftTableAlias, joinColumns, rightTableAlias);
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

    private Optional<BooleanExpression> columnsToCondition(Optional<BooleanExpression> joinCondition, String leftTableAlias, List<String> columnNames, String rightTableAlias) {
        if (columnNames.size() == 0) {
            return joinCondition;
        }
        return Optional.of(
                columnNames.stream()
                        .map(cn -> new SimpleBooleanExpression(
                                new ColumnExpression(cn, leftTableAlias),
                                EQUAL,
                                new ColumnExpression(cn, rightTableAlias)
                        ))
                        .reduce(
                                BinaryBooleanExpression.alwaysTrue(),
                                (f, s) -> new BinaryBooleanExpression(f, AND, s),
                                (f, s) -> new BinaryBooleanExpression(f, AND, s)
                        )
        );
    }

    //A naive implementation filtering cross join by condition
    private DataContainer innerJoin(DataContainer left, DataContainer right, Optional<BooleanExpression> joinCondition) {
        return crossJoin(left, right, joinCondition, JoinType.INNER);
    }

    private DataContainer outerJoin(DataContainer left, JoinType joinType, DataContainer right, BooleanExpression joinCondition) {
        return crossJoin(left, right, Optional.ofNullable(joinCondition), joinType);
    }

    //Based on http://stackoverflow.com/questions/9591561/java-cartesian-product-of-a-list-of-lists
    DataContainer crossJoin(DataContainer left, DataContainer right, Optional<BooleanExpression> joinCondition, JoinType joinType) {
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

    private static List<Object> rowWithNullsValues(List<DataRow> leftRows, int leftColumnsCount, List<DataRow> rightRows, int rightColumnsCount, int index, boolean isLeftJoin) {
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

    static List<Object> listWithNulls(int size) {
        Object[] array = new Object[size];
        Arrays.fill(array, null);
        return Arrays.asList(array);
    }

    private static DataContainer mergeContainerColumns(DataContainer left, DataContainer right) {
        return new DataContainer(new LinkedHashMap<String, List<String>>(){
            {
                putAll(left.getColumnsMap());
                putAll(right.getColumnsMap());
            }
        });
    }

    void checkLeftDataSource(DataSource dataSource) {
        checkLeftDataSource(dataSource, true);
    }
    
    void checkLeftDataSource(DataSource dataSource, boolean conditionRequired) {
        if (!dataSource.getTableAlias().isPresent()) {
            throw new IllegalArgumentException("Scan strategy datasource should contain table alias");
        }
        Optional<BooleanExpression> conditionCandidate = dataSource.getCondition();
        if (
                conditionRequired &&
                ( !conditionCandidate.isPresent() || !(conditionCandidate.get() instanceof IndexBooleanExpression) )
        ) {
            throw new IllegalArgumentException("Scan strategy datasource should have condition of IndexBooleanExpression type");
        }
    }

    void checkRightDataSource(DataSource dataSource) {
        checkRightDataSource(dataSource, true);
    }
    
    void checkRightDataSource(DataSource dataSource, boolean conditionRequired) {
        checkLeftDataSource(dataSource, conditionRequired);
        if (dataSource.getRightDataSource().isPresent()) {
            throw new IllegalArgumentException("Scan strategy can only join two tables");
        }
    }

}
