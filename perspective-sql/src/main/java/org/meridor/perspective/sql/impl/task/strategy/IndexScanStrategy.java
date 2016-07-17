package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.expression.IndexBooleanExpression;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.DataSourceUtils;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToNames;
import static org.meridor.perspective.sql.impl.parser.DataSourceUtils.*;
import static org.meridor.perspective.sql.impl.parser.JoinType.*;

@Component
public class IndexScanStrategy extends ScanStrategy {

    @Autowired
    private DataFetcher dataFetcher;

    @Autowired
    private TablesAware tablesAware;

    @Override
    public DataContainer process(DataSource dataSource, Map<String, String> tableAliases) {
        checkLeftDataSource(dataSource);
        String tableAlias = dataSource.getTableAlias().get();
        IndexBooleanExpression condition = getCondition(dataSource);
        List<String> columns = dataSource.getColumns();
        if (dataSource.getRightDataSource().isPresent()) {
            DataSource nextDataSource = dataSource.getRightDataSource().get();
            checkRightDataSource(nextDataSource);
            String rightTableAlias = nextDataSource.getTableAlias().get();
            IndexBooleanExpression rightCondition = getCondition(nextDataSource);
            List<String> rightColumns = nextDataSource.getColumns();
            return foreignKeyJoin(tableAlias, columns, condition.getFixedValueConditions(tableAlias), rightTableAlias, rightColumns, rightCondition.getFixedValueConditions(rightTableAlias), nextDataSource.getJoinType().get(), tableAliases);
        } else {
            return fetch(tableAlias, condition, tableAliases);
        }
    }
    
    private IndexBooleanExpression getCondition(DataSource dataSource) {
        return (IndexBooleanExpression) dataSource.getCondition().get();
    } 

    //Here we assume that conditions contains only indexed columns. 
    //Query planner should guarantee that. 
    private DataContainer fetch(String tableAlias, IndexBooleanExpression condition, Map<String, String> tableAliases) {
        String tableName = tableAliases.get(tableAlias);
        Set<String> ids = getIdsFromIndex(tableName, tableAlias, condition);
        return ids.isEmpty() ?
                DataContainer.empty() :
                fetchByIds(tableName, tableAlias, ids);
    }
    
    private DataContainer fetchByIds(String tableName, String tableAlias, Set<String> ids) {
        return dataFetcher.fetch(tableName, tableAlias, ids, tablesAware.getColumns(tableName));
    }
    
    private Set<String> getIdsFromIndex(String tableName, String tableAlias, IndexBooleanExpression condition) {
        Map<IndexSignature, Map<String, Set<Object>>> conditions = splitConditionByIndexes(tableName, tableAlias, condition);
        return conditions.keySet().stream()
                .map(is -> getMatchedIndexIds(is, conditions.get(is)))
                .reduce(Collections.emptySet(), DataSourceUtils::intersect);
    }

    private Map<IndexSignature, Map<String, Set<Object>>> splitConditionByIndexes(String tableName, String tableAlias, IndexBooleanExpression condition) {
        Map<IndexSignature, Map<String, Set<Object>>> ret = new HashMap<>();
        Map<String, Set<Object>> conditionAsMap = condition.getFixedValueConditions(tableAlias);
        Set<String> conditionColumns = conditionAsMap.keySet();
        tablesAware.getIndexSignatures().forEach(is -> {
            Map<String, Set<String>> desiredColumns = is.getDesiredColumns();
            if (desiredColumns.containsKey(tableName)) {
                Set<String> indexColumns = desiredColumns.get(tableName);
                if (conditionColumns.containsAll(indexColumns)) {
                    Map<String, Set<Object>> indexCondition = new HashMap<>();
                    indexColumns.stream().forEach(
                            indexColumn -> {
                                indexCondition.put(indexColumn, conditionAsMap.get(indexColumn));
                                conditionAsMap.remove(indexColumn);
                            } 
                    );
                    ret.put(is, indexCondition);
                }
            }
        });
        if (!conditionAsMap.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "Failed to completely split condition by indexes. The following columns were not selected: %s",
                    conditionColumns
            ));
        }
        return ret;
    }

    private Set<String> getMatchedIndexIds(IndexSignature indexSignature, Map<String, Set<Object>> condition) {
        Index index = getIndex(indexSignature);
        Set<Key> keys = conditionToKeys(index.getKeyLength(), condition);
        return keys.stream()
                .flatMap(k -> index.get(k).stream())
                .collect(Collectors.toSet());
    }

    //Set of objects is used for OR conditions. Otherwise set contains only one value.
    private static Set<Key> conditionToKeys(int keyLength, Map<String, Set<Object>> condition) {
        List<Set<Object>> values = new ArrayList<>(condition.values());
        return setsCrossProduct(values).stream()
                .map(v -> Keys.key(keyLength, v.toArray()))
                .collect(Collectors.toSet());
    }

    //Based on http://stackoverflow.com/questions/714108/cartesian-product-of-arbitrary-sets-in-java 
    private static Set<Set<Object>> setsCrossProduct(List<Set<Object>> sets) {
        if (sets.size() < 2) {
            return new HashSet<>(sets);
        }
        return setsCrossProductImpl(0, sets);
    }

    private static Set<Set<Object>> setsCrossProductImpl(int index, List<Set<Object>> sets) {
        Set<Set<Object>> ret = new HashSet<>();
        if (index == sets.size()) {
            ret.add(new HashSet<>());
        } else {
            for (Object obj : sets.get(index)) {
                for (Set<Object> set : setsCrossProductImpl(index + 1, sets)) {
                    set.add(obj);
                    ret.add(set);
                }
            }
        }
        return ret;
    }

    private Index getIndex(IndexSignature indexSignature){
        Optional<Index> indexCandidate = tablesAware.getIndex(indexSignature);
        if (!indexCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "No index found for columns: [%s]. This is probably a bug.", indexSignature.getDesiredColumns()
            ));
        }
        return indexCandidate.get();
    }

    private DataContainer foreignKeyJoin(
            String leftTableAlias,
            List<String> leftColumns, //These are only columns present in index!
            Map<String, Set<Object>> leftCondition,
            String rightTableAlias,
            List<String> rightColumns,
            Map<String, Set<Object>> rightCondition,
            JoinType joinType,
            Map<String, String> tableAliases
    ) {
        String leftTable = tableAliases.get(leftTableAlias);
        Index leftIndex = getIndex(leftTable, leftColumns);
        String rightTable = tableAliases.get(rightTableAlias);
        Index rightIndex = getIndex(rightTable, rightColumns);

        Set<Key> leftForeignKeys = getForeignKeys(leftIndex, leftColumns, leftCondition);
        Set<Key> rightForeignKeys = getForeignKeys(rightIndex, rightColumns, rightCondition);

        Optional<IndexBooleanExpression> leftSupplementaryConditionCandidate = getSupplementaryCondition(leftTableAlias, leftColumns, leftCondition);
        Optional<IndexBooleanExpression> rightSupplementaryConditionCandidate = getSupplementaryCondition(rightTableAlias, rightColumns, rightCondition);

        Collection<Column> leftTableColumns = tablesAware.getColumns(leftTable); //These are all table columns
        Collection<Column> rightTableColumns = tablesAware.getColumns(rightTable);

        Map<String, List<String>> resultingColumnsMap = new LinkedHashMap<String, List<String>>() {
            {
                put(leftTableAlias, columnsToNames(leftTableColumns));
                put(rightTableAlias, columnsToNames(rightTableColumns));
            }
        }; 
        DataContainer dataContainer = new DataContainer(resultingColumnsMap);
        
        //Always adding inner join results
        Set<Key> innerJoinForeignKeys = DataSourceUtils.intersect(leftForeignKeys, rightForeignKeys);
        Set<String> matchedIds = new HashSet<>();
        innerJoinForeignKeys.stream()
                .flatMap(mk -> {
                    Set<String> matchedLeftForeignKeyIds = getMatchedForeignKeyIds(leftTable, leftTableAlias, mk, leftIndex, leftSupplementaryConditionCandidate);
                    DataContainer leftResults = fetchByIds(leftTable, leftTableAlias, matchedLeftForeignKeyIds);
                    Set<String> matchedRightForeignKeyIds = getMatchedForeignKeyIds(rightTable, rightTableAlias, mk, rightIndex, rightSupplementaryConditionCandidate);
                    DataContainer rightResults = fetchByIds(rightTable, rightTableAlias, matchedRightForeignKeyIds);
                    
                    if (!leftResults.getRows().isEmpty() && !rightResults.getRows().isEmpty()) {
                        if (joinType == LEFT) {
                            matchedIds.addAll(matchedLeftForeignKeyIds);
                        } else if (joinType == RIGHT) {
                            matchedIds.addAll(matchedRightForeignKeyIds);
                        }
                    }
                    
                    return crossJoin(leftResults, rightResults, Optional.empty(), INNER).getRows().stream();
                })
                .forEach(dataContainer::addRow);
        
        if (joinType == LEFT) {
            addNotMatchingRows(
                    leftTable,
                    leftTableAlias,
                    leftIndex,
                    matchedIds,
                    rightTableColumns.size(),
                    true,
                    dataContainer
            );
        } else if (joinType == RIGHT) {
            addNotMatchingRows(
                    rightTable,
                    rightTableAlias,
                    rightIndex,
                    matchedIds,
                    leftTableColumns.size(),
                    false,
                    dataContainer
            );
        }
        
        return dataContainer;
    }
    
    private void addNotMatchingRows(
            String tableName,
            String tableAlias,
            Index index,
            Set<String> matchedIds,
            int optionalColumnsSize,
            boolean isLeftJoin,
            DataContainer dataContainer
    ) {
        Set<String> allIds = index.getIds();
        Set<String> notMatchedIds = difference(allIds, matchedIds);
        DataContainer requiredResults = fetchByIds(tableName, tableAlias, notMatchedIds);
        requiredResults.getRows().forEach(dr -> {
                List<Object> values = new ArrayList<Object>() {
                    {
                        if (isLeftJoin) {
                            addAll(dr.getValues());
                            addAll(listWithNulls(optionalColumnsSize));
                        } else {
                            addAll(listWithNulls(optionalColumnsSize));
                            addAll(dr.getValues());
                        }
                    }
                };
                dataContainer.addRow(values);
        });
    }
    
    private Set<String> getMatchedForeignKeyIds(String tableName, String tableAlias, Key matchingForeignKey, Index index, Optional<IndexBooleanExpression> supplementaryConditionCandidate) {
        Set<String> matchedForeignKeyIds = index.get(matchingForeignKey);
        return supplementaryConditionCandidate.isPresent() ?
                DataSourceUtils.intersect(
                        matchedForeignKeyIds,
                        getIdsFromIndex(tableName, tableAlias, supplementaryConditionCandidate.get())
                ) :
                matchedForeignKeyIds;
    }
    
    private Set<Key> getForeignKeys(Index index, List<String> columns, Map<String, Set<Object>> condition) {
        Map<String, Set<Object>> foreignKeyCondition = condition.keySet().stream()
                .filter(columns::contains)
                .collect(Collectors.toMap(Function.identity(), condition::get));

        return foreignKeyCondition.isEmpty() ?
                index.getKeys() :
                conditionToKeys(index.getKeyLength(), foreignKeyCondition);
    }
    
    private Index getIndex(String tableName, List<String> columns) {
        IndexSignature indexSignature = new IndexSignature(Collections.singletonMap(tableName, new LinkedHashSet<>(columns)));
        return getIndex(indexSignature);
    }
    
    private Optional<IndexBooleanExpression> getSupplementaryCondition(String tableAlias, List<String> columns, Map<String, Set<Object>> condition) {
        Map<String, Set<Object>> fixedValuesMap = condition.keySet().stream()
                .filter(cn -> !columns.contains(cn))
                .collect(Collectors.toMap(Function.identity(), condition::get));
        return fixedValuesMap.isEmpty() ?
                Optional.empty() :
                Optional.of(new IndexBooleanExpression(tableAlias, fixedValuesMap));
    }

}
