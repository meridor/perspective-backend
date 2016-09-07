package org.meridor.perspective.sql.impl.parser;

import org.meridor.perspective.sql.impl.expression.BinaryBooleanExpression;
import org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator;
import org.meridor.perspective.sql.impl.expression.BooleanExpression;
import org.meridor.perspective.sql.impl.expression.IndexBooleanExpression;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DataSourceUtils {

    public static DataSource getTail(DataSource dataSource) {
        if (dataSource.getRightDataSource().isPresent()) {
            return getTail(dataSource.getRightDataSource().get());
        }
        if (dataSource.getLeftDataSource().isPresent()) {
            return getTail(dataSource.getLeftDataSource().get());
        }
        return dataSource;
    }

    /**
     * Builds optimized data source as a tree
     * @param parent data source to add child to
     * @param child child data source
     */
    public static void addToDataSource(DataSource parent, DataSource child) {
        if (!parent.getLeftDataSource().isPresent()) {
            parent.setLeftDataSource(child);
        } else if (!parent.getRightDataSource().isPresent()) {
            parent.setRightDataSource(child);
        } else {
            DataSource newLeftDataSource = new DataSource(parent.getLeftDataSource().get());
            newLeftDataSource.setRightDataSource(parent.getRightDataSource().get());
            parent.setLeftDataSource(newLeftDataSource);
            parent.setRightDataSource(child);
        }
    }

    public static void checkLeftDataSource(DataSource dataSource) {
        checkLeftDataSource(dataSource, true);
    }

    public static void checkLeftDataSource(DataSource dataSource, boolean conditionRequired) {
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

    public static void checkRightDataSource(DataSource dataSource) {
        checkRightDataSource(dataSource, true);
    }

    public static void checkRightDataSource(DataSource dataSource, boolean conditionRequired) {
        checkLeftDataSource(dataSource, conditionRequired);
        if (dataSource.getRightDataSource().isPresent()) {
            throw new IllegalArgumentException("Scan strategy can only join two tables");
        }
    }

    public static <T> Set<T> intersection(Set<T> first, Set<T> second) {
        Set<T> copyOfFirst = new LinkedHashSet<>(first);
        copyOfFirst.retainAll(second);
        return copyOfFirst;
    }

    public static <T> Set<T> difference(Set<T> first, Set<T> second) {
        Set<T> copyOfFirst = new LinkedHashSet<>(first);
        copyOfFirst.removeAll(second);
        return copyOfFirst;
    }

    public static Optional<BooleanExpression> intersectConditions(Optional<BooleanExpression> left, Optional<BooleanExpression> right) {
        if (!left.isPresent()) {
            return right;
        }
        if (!right.isPresent()) {
            return left;
        }
        return Optional.of(new BinaryBooleanExpression(left.get(), BinaryBooleanOperator.AND, right.get()));
    }

    public static void iterateDataSource(DataSource dataSource, DataSourceAction action) {
        iterateDataSource(Optional.empty(), Optional.ofNullable(dataSource), dataSource.getRightDataSource(), action);
    }

    private static void iterateDataSource(
            Optional<DataSource> previousDataSourceCandidate,
            Optional<DataSource> dataSourceCandidate,
            Optional<DataSource> nextSourceCandidate,
            DataSourceAction action
    ) {
        if (!dataSourceCandidate.isPresent()) {
            return;
        }
        DataSource dataSource = dataSourceCandidate.get();
        action.apply(previousDataSourceCandidate, dataSource, nextSourceCandidate);
        Assert.isTrue(dataSource.getTableAlias().isPresent(), "Original query should have table alias");
        Assert.isTrue(!dataSource.getLeftDataSource().isPresent(), "Original data source should not contain nested data sources");
        Optional<DataSource> rightDataSourceCandidate = dataSource.getRightDataSource();
        Optional<DataSource> afterRightDataSourceCandidate = rightDataSourceCandidate.isPresent() ?
                rightDataSourceCandidate.get().getRightDataSource() :
                Optional.empty();
        iterateDataSource(dataSourceCandidate, rightDataSourceCandidate, afterRightDataSourceCandidate, action);
    }
    
    //Based on http://stackoverflow.com/questions/9591561/java-cartesian-product-of-a-list-of-lists
    public static <I, O> void crossProduct(List<I> left, List<I> right, Function<I, List<O>> transformer, BiConsumer<Pair<Integer, Integer>, List<O>> rowConsumer) {
        final int SIZE = left.size() * right.size();

        for (int i = 0; i < SIZE; i++) {
            List<O> newRowValues = new ArrayList<>();
            int j = 1;
            List<Integer> currentIndexes = new ArrayList<>();
            for (List<I> rowsList : new ArrayList<List<I>>() {
                {
                    add(left);
                    add(right);
                }
            }) {
                final int index = ( i / j ) % rowsList.size();
                currentIndexes.add(index);
                I row = rowsList.get(index);
                List<O> rowPart = transformer.apply(row);
                newRowValues.addAll(rowPart);
                j *= rowsList.size();
            }
            if (currentIndexes.size() == 2) {
                rowConsumer.accept(new Pair<>(currentIndexes.get(0), currentIndexes.get(1)), newRowValues);
            }
        }
    }
    
    public static <T> List<List<T>> crossProduct(List<Collection<T>> lists) {
        if (lists == null || lists.size() == 0) {
            return Collections.emptyList();
        }
        if (lists.size() == 1) {
            return lists.get(0).stream()
                    .map(Collections::singletonList)
                    .collect(Collectors.toList());
        }
        if (lists.size() == 2) {
            List<List<T>> leftRightCrossProduct = new ArrayList<>();
            crossProduct(
                    new ArrayList<>(lists.get(0)),
                    new ArrayList<>(lists.get(1)),
                    Collections::singletonList,
                    (indexesPair, list) -> leftRightCrossProduct.add(list));
            return leftRightCrossProduct;
        }
        List<T> left = new ArrayList<>(lists.get(0));
        List<List<T>> rightCrossProduct = crossProduct(lists.subList(1, lists.size()));
        List<List<T>> ret = new ArrayList<>();
        left.forEach(entry -> rightCrossProduct.forEach(list -> ret.add(new ArrayList<T>(){
            {
                add(entry);
                addAll(list);
            }
        })));
        return ret;
    }

    public interface DataSourceAction {
        void apply(Optional<DataSource> previousDataSourceCandidate, DataSource dataSource, Optional<DataSource> nextDataSourceCandidate);
    }
    
}
