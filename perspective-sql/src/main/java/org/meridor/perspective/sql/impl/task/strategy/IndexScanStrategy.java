package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.expression.ColumnRelation;
import org.meridor.perspective.sql.impl.expression.IndexBooleanExpression;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.DataSourceUtils;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.meridor.perspective.sql.impl.parser.Pair;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
    
    @Autowired
    private IndexStorage indexStorage;

    @Override
    public DataContainer process(DataSource dataSource, Map<String, String> tableAliases) {
        checkLeftDataSource(dataSource);
        String tableAlias = dataSource.getTableAlias().get();
        IndexBooleanExpression condition = getCondition(dataSource);
        if (dataSource.getRightDataSource().isPresent()) {
            DataSource nextDataSource = dataSource.getRightDataSource().get();
            checkRightDataSource(nextDataSource);
            String rightTableAlias = nextDataSource.getTableAlias().get();
            IndexBooleanExpression rightCondition = getCondition(nextDataSource);
            List<ColumnRelation> columnRelations = rightCondition.getColumnRelations();
            Assert.isTrue(columnRelations.size() > 0, "At least one column relation should be present");
            return foreignKeyJoin(tableAlias, columnRelations, condition.getFixedValueConditions(tableAlias), rightTableAlias, rightCondition.getFixedValueConditions(rightTableAlias), nextDataSource.getJoinType().get(), tableAliases);
        } else {
            return fetch(tableAlias, condition, tableAliases);
        }
    }
    
    private static IndexBooleanExpression getCondition(DataSource dataSource) {
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
        Optional<Set<String>> idsCandidate = conditions.keySet().stream()
                .map(is -> getMatchedIndexIds(is, conditions.get(is)))
                .reduce(DataSourceUtils::intersection);
        return idsCandidate.isPresent() ? idsCandidate.get() : Collections.emptySet();
    }

    private Map<IndexSignature, Map<String, Set<Object>>> splitConditionByIndexes(String tableName, String tableAlias, IndexBooleanExpression condition) {
        Map<IndexSignature, Map<String, Set<Object>>> ret = new HashMap<>();
        Map<String, Set<Object>> conditionAsMap = condition.getFixedValueConditions(tableAlias);
        Set<String> conditionColumns = conditionAsMap.keySet();
        indexStorage.getSignatures().forEach(is -> {
            Map<String, Set<String>> desiredColumns = is.getDesiredColumns();
            if (desiredColumns.containsKey(tableName)) {
                Set<String> indexColumns = desiredColumns.get(tableName);
                if (conditionColumns.containsAll(indexColumns)) {
                    Map<String, Set<Object>> indexCondition = new HashMap<>();
                    indexColumns.forEach(
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

    private Index getIndex(String tableName, String columnName) {
        IndexSignature indexSignature = new IndexSignature(tableName, Collections.singleton(columnName));
        return getIndex(indexSignature);
    }

    private Index getIndex(IndexSignature indexSignature){
        Optional<Index> indexCandidate = indexStorage.get(indexSignature);
        if (!indexCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "No index found for columns: [%s]. This is probably a bug.", indexSignature.getDesiredColumns()
            ));
        }
        return indexCandidate.get();
    }

    private DataContainer foreignKeyJoin(
            String leftTableAlias,
            List<ColumnRelation> columnRelations,
            Map<String, Set<Object>> leftCondition,
            String rightTableAlias,
            Map<String, Set<Object>> rightCondition,
            JoinType joinType,
            Map<String, String> tableAliases
    ) {
        
        String leftTableName = tableAliases.get(leftTableAlias);
        String rightTableName = tableAliases.get(rightTableAlias);

        Set<String> allColumnsInColumnRelations = columnRelations.stream()
                .flatMap(cr -> cr.toMap().values().stream())
                .reduce(new HashSet<>(), (l, r) -> {
                    l.addAll(r);
                    return l;
                });
        
        Optional<Pair<Set<String>, Set<String>>> matchedIdsPair = columnRelations.stream()
                .map(cr -> columnRelationToIds(cr, leftCondition, rightCondition, tableAliases, allColumnsInColumnRelations))
                .reduce(
                        (l, r) -> new Pair<>(
                                intersection(l.getFirst(), r.getFirst()),
                                intersection(l.getSecond(), r.getSecond())
                        )
                );
        
        Set<String> matchedLeftIds = matchedIdsPair.isPresent() ? matchedIdsPair.get().getFirst() : Collections.emptySet();
        Set<String> matchedRightIds = matchedIdsPair.isPresent() ? matchedIdsPair.get().getSecond() : Collections.emptySet();
        
        Set<String> matchedIds =
                (joinType == LEFT) ? 
                        matchedLeftIds :
                        (joinType == RIGHT) ? matchedRightIds : Collections.emptySet();

        Collection<Column> leftTableColumns = tablesAware.getColumns(leftTableName); //These are all table columns
        Collection<Column> rightTableColumns = tablesAware.getColumns(rightTableName);

        Map<String, List<String>> resultingColumnsMap = new LinkedHashMap<String, List<String>>() {
            {
                put(leftTableAlias, columnsToNames(leftTableColumns));
                put(rightTableAlias, columnsToNames(rightTableColumns));
            }
        }; 
        DataContainer dataContainer = new DataContainer(resultingColumnsMap);
        
        DataContainer leftResults = fetchByIds(leftTableName, leftTableAlias, matchedLeftIds);
        DataContainer rightResults = fetchByIds(rightTableName, rightTableAlias, matchedRightIds);

        //Always adding inner join results
        crossJoin(leftResults, rightResults, Optional.empty(), INNER).getRows()
                .forEach(dataContainer::addRow);
        
        //Adding outer join rows if needed
        if (joinType == LEFT) {
            //We take first available index because any index should store all ids
            Index leftIndex = getIndex(leftTableName, columnRelations.get(0).getColumnName(leftTableAlias));
            addNotMatchingRows(
                    leftTableName,
                    leftTableAlias,
                    leftIndex,
                    matchedIds,
                    rightTableColumns.size(),
                    true,
                    dataContainer
            );
        } else if (joinType == RIGHT) {
            Index rightIndex = getIndex(rightTableName, columnRelations.get(0).getColumnName(rightTableAlias));
            addNotMatchingRows(
                    rightTableName,
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

    private Pair<Set<String>, Set<String>> columnRelationToIds(
            ColumnRelation columnRelation,
            Map<String, Set<Object>> leftCondition,
            Map<String, Set<Object>> rightCondition,
            Map<String, String> tableAliases,
            Set<String> allColumnsInColumnRelations) {
        String leftTableAlias = columnRelation.getLeftTableAlias();
        String leftTableName = tableAliases.get(leftTableAlias);
        String leftColumnName = columnRelation.getColumnName(leftTableAlias);
        Index leftIndex = getIndex(leftTableName, leftColumnName);
        Set<Key> leftForeignKeys = getForeignKeys(leftIndex, leftColumnName, leftCondition);
        
        String rightTableAlias = columnRelation.getRightTableAlias();
        String rightTableName = tableAliases.get(rightTableAlias);
        String rightColumnName = columnRelation.getColumnName(rightTableAlias);
        Index rightIndex = getIndex(rightTableName, rightColumnName);
        Set<Key> rightForeignKeys = getForeignKeys(rightIndex, rightColumnName, rightCondition);

        Optional<IndexBooleanExpression> leftSupplementaryConditionCandidate = getSupplementaryCondition(allColumnsInColumnRelations, leftCondition);
        Optional<IndexBooleanExpression> rightSupplementaryConditionCandidate = getSupplementaryCondition(allColumnsInColumnRelations, rightCondition);

        Set<Key> innerJoinForeignKeys = intersection(leftForeignKeys, rightForeignKeys);
        Set<String> leftIds = new HashSet<>();
        Set<String> rightIds = new HashSet<>();
        innerJoinForeignKeys.forEach(
                mk -> {

                    Set<String> matchedLeftForeignKeyIds = leftIndex.get(mk);
                    Set<String> matchedLeftIds = getMatchedForeignKeyIds(leftTableName, leftTableAlias, matchedLeftForeignKeyIds, leftSupplementaryConditionCandidate);
                    Set<String> matchedRightForeignKeyIds = rightIndex.get(mk);
                    Set<String> matchedRightIds = getMatchedForeignKeyIds(rightTableName, rightTableAlias, matchedRightForeignKeyIds, rightSupplementaryConditionCandidate);

                    if (!matchedLeftIds.isEmpty() && !matchedRightIds.isEmpty()) {
                        leftIds.addAll(matchedLeftIds);
                        rightIds.addAll(matchedRightIds);
                    }
                }
        );
        if (columnRelation.getNextRelation().isPresent()) {
            Pair<Set<String>, Set<String>> nextColumnRelationIds = columnRelationToIds(
                    columnRelation.getNextRelation().get(),
                    leftCondition,
                    rightCondition,
                    tableAliases,
                    allColumnsInColumnRelations);
            leftIds.addAll(nextColumnRelationIds.getFirst());
            rightIds.addAll(nextColumnRelationIds.getSecond());
        }
        return new Pair<>(leftIds, rightIds);
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

    private Set<String> getMatchedForeignKeyIds(String tableName, String tableAlias, Set<String> matchedForeignKeyIds, Optional<IndexBooleanExpression> supplementaryConditionCandidate) {
        return supplementaryConditionCandidate.isPresent() ?
                intersection(
                        matchedForeignKeyIds,
                        getIdsFromIndex(tableName, tableAlias, supplementaryConditionCandidate.get())
                ) :
                matchedForeignKeyIds;
    }
    
    private Set<Key> getForeignKeys(Index index, String columnName, Map<String, Set<Object>> condition) {
        Map<String, Set<Object>> foreignKeyCondition = condition.keySet().stream()
                .filter(cn -> cn.equals(columnName))
                .collect(Collectors.toMap(Function.identity(), condition::get));

        return foreignKeyCondition.isEmpty() ?
                index.getKeys() :
                conditionToKeys(index.getKeyLength(), foreignKeyCondition);
    }

    //Any indexed columns from the same table not contained in column relations
    //E.g. (a.id = b.id and a.id = '2') and a.name = 'test' <- the last one is supplementary condition
    private Optional<IndexBooleanExpression> getSupplementaryCondition(Set<String> allColumnsInColumnRelations, Map<String, Set<Object>> condition) {
        Map<String, Set<Object>> fixedValuesMap = condition.keySet().stream()
                .filter(cn -> !allColumnsInColumnRelations.contains(cn))
                .collect(Collectors.toMap(Function.identity(), condition::get));
        return fixedValuesMap.isEmpty() ?
                Optional.empty() :
                Optional.of(new IndexBooleanExpression(fixedValuesMap));
    }

}
