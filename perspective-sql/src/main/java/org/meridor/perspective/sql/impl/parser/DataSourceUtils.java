package org.meridor.perspective.sql.impl.parser;

import org.meridor.perspective.sql.impl.expression.BooleanExpression;
import org.meridor.perspective.sql.impl.expression.IndexBooleanExpression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class DataSourceUtils {

    public static DataSource getTail(DataSource dataSource) {
        if (dataSource.getRightDataSource().isPresent()) {
            return getTail(dataSource.getRightDataSource().get());
        }
        if (dataSource.getLeftDataSource().isPresent()) {
            return dataSource.getLeftDataSource().get();
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
            parent.setRightDatasource(child);
        } else {
            DataSource rightDataSource = parent.getRightDataSource().get();
            if (rightDataSource.getType() == DataSource.DataSourceType.PARENT) {
                addToDataSource(rightDataSource, child);
            } else {
                DataSource newRightDataSource = new DataSource(rightDataSource);
                parent.setRightDatasource(newRightDataSource);
                addToDataSource(newRightDataSource, child);
            }
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

    public static <T> Set<T> intersect(Set<T> first, Set<T> second) {
        if (first.isEmpty()) {
            return new LinkedHashSet<>(second);
        }
        Set<T> copyOfFirst = new LinkedHashSet<>(first);
        copyOfFirst.retainAll(second);
        return copyOfFirst;
    }

    public static <T> Set<T> difference(Set<T> first, Set<T> second) {
        Set<T> copyOfFirst = new LinkedHashSet<>(first);
        copyOfFirst.removeAll(second);
        return copyOfFirst;
    }

    private DataSourceUtils() {

    }
}
