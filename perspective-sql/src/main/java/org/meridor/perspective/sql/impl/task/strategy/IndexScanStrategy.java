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
        return fetchByIds(tableName, tableAlias, ids);
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
        return crossProduct(new ArrayList<>(condition.values())).stream()
                    .map(v -> Keys.key(keyLength, v.toArray()))
                    .collect(Collectors.toSet());
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
        
        List<Pair<List<Pair<String, String>>, Pair<Set<String>, Set<String>>>> columnRelationsIds = columnRelations.stream()
                .map(cr -> columnRelationToIdPairs(cr, leftCondition, rightCondition, tableAliases, allColumnsInColumnRelations))
                .collect(Collectors.toList());

        Optional<Pair<Set<String>, Set<String>>> allMatchedIdsPair = columnRelationsIds.stream()
                .map(Pair::getSecond)
                .reduce(
                        (l, r) -> new Pair<>(
                                intersection(l.getFirst(), r.getFirst()),
                                intersection(l.getSecond(), r.getSecond())
                        )
                );
        
        Set<String> allMatchedLeftIds = allMatchedIdsPair.isPresent() ? allMatchedIdsPair.get().getFirst() : Collections.emptySet();
        Set<String> allMatchedRightIds = allMatchedIdsPair.isPresent() ? allMatchedIdsPair.get().getSecond() : Collections.emptySet();

        Collection<Column> leftTableColumns = tablesAware.getColumns(leftTableName); //These are all table columns
        Collection<Column> rightTableColumns = tablesAware.getColumns(rightTableName);

        Map<String, List<String>> resultingColumnsMap = new LinkedHashMap<String, List<String>>() {
            {
                put(leftTableAlias, columnsToNames(leftTableColumns));
                put(rightTableAlias, columnsToNames(rightTableColumns));
            }
        };
        DataContainer dataContainer = new DataContainer(resultingColumnsMap);

        //Always adding inner join results
        columnRelationsIds.stream()
                .flatMap(cri -> cri.getFirst().stream())
                .filter(p -> allMatchedLeftIds.contains(p.getFirst()) && allMatchedRightIds.contains(p.getSecond()))
                .forEach(pair -> {
            
                    DataContainer leftResults = fetchByIds(leftTableName, leftTableAlias, Collections.singleton(pair.getFirst()));
                    DataContainer rightResults = fetchByIds(rightTableName, rightTableAlias, Collections.singleton(pair.getSecond()));
            
                    crossJoin(leftResults, rightResults, Optional.empty(), INNER).getRows()
                            .forEach(dataContainer::addRow);
                });
        
        //Adding outer join rows if needed
        if (joinType == LEFT) {
            //We take first available index because any index should store all ids
            Index leftIndex = getIndex(leftTableName, columnRelations.get(0).getColumnName(leftTableAlias));
            addNotMatchingRows(
                    leftTableName,
                    leftTableAlias,
                    leftIndex,
                    allMatchedLeftIds,
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
                    allMatchedRightIds,
                    leftTableColumns.size(),
                    false,
                    dataContainer
            );
        }
        
        return dataContainer;
    }

    private Pair<List<Pair<String, String>>, Pair<Set<String>, Set<String>>> columnRelationToIdPairs(
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
        Set<String> allLeftMatchedIds = new HashSet<>();
        Set<String> allRightMatchedIds = new HashSet<>();
        List<Pair<String, String>> pairs = new ArrayList<>();
        innerJoinForeignKeys.forEach(
                mk -> {

                    Set<String> matchedLeftForeignKeyIds = leftIndex.get(mk);
                    Set<String> matchedLeftIds = getMatchedForeignKeyIds(leftTableName, leftTableAlias, matchedLeftForeignKeyIds, leftSupplementaryConditionCandidate);
                    Set<String> matchedRightForeignKeyIds = rightIndex.get(mk);
                    Set<String> matchedRightIds = getMatchedForeignKeyIds(rightTableName, rightTableAlias, matchedRightForeignKeyIds, rightSupplementaryConditionCandidate);

                    if (!matchedLeftIds.isEmpty() && !matchedRightIds.isEmpty()) {
                        crossProduct(
                                new ArrayList<>(matchedLeftIds),
                                new ArrayList<>(matchedRightIds),
                                Collections::singletonList,
                                (indexesPair, idsList) -> {
                                    Assert.isTrue(idsList.size() == 2, "Pair should contain exactly two items");
                                    String[] pairContents = idsList.toArray(new String[idsList.size()]);
                                    String leftId = pairContents[0];
                                    String rightId = pairContents[1];
                                    Pair<String, String> pair = new Pair<>(leftId, rightId);
                                    pairs.add(pair);
                                    allLeftMatchedIds.add(leftId);
                                    allRightMatchedIds.add(rightId);
                                });
                    }
                }
        );
        if (columnRelation.getNextRelation().isPresent()) {
            Pair<List<Pair<String, String>>, Pair<Set<String>, Set<String>>> nextRelationData = columnRelationToIdPairs(
                    columnRelation.getNextRelation().get(),
                    leftCondition,
                    rightCondition,
                    tableAliases,
                    allColumnsInColumnRelations
            );
            pairs.addAll(nextRelationData.getFirst());
            allLeftMatchedIds.addAll(nextRelationData.getSecond().getFirst());
            allRightMatchedIds.addAll(nextRelationData.getSecond().getSecond());
        }
        return new Pair<>(pairs, new Pair<>(allLeftMatchedIds, allRightMatchedIds));
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
