package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.ExpressionEvaluator;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectTask implements Task {
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    
    @Autowired
    private TablesAware tablesAware;

    private Map<String, Object> selectionMap;

    private boolean selectAll;

    public SelectTask(Map<String, Object> selectionMap) {
        this.selectionMap = selectionMap;
    }

    @PostConstruct
    public void init() {
        selectionMap = processSelectionMap(selectionMap);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        try {
            if (selectAll) {
                return previousTaskResult;
            }

            DataContainer newData = new DataContainer(selectionMap.keySet());
            previousTaskResult.getData().getRows().stream()
                    .map(dr -> selectionMap.keySet().stream()
                            .map(alias -> expressionEvaluator.evaluate(selectionMap.get(alias), dr))
                            .collect(Collectors.toList()))
                    .forEach(newData::addRow);
            return new ExecutionResult(){
                {
                    setData(newData);
                    setCount(newData.getRows().size());
                }
            };
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    //Here we convert ColumnExpression(*) expressions to a list of ColumnExpression(columnName) 
    private Map<String, Object> processSelectionMap(Map<String, Object> selectionMap) {
        Map<String, Object> ret = new LinkedHashMap<>();
        selectionMap.keySet().forEach(alias -> {
            Object expression = selectionMap.get(alias);
            if (expression instanceof ColumnExpression) {
                ColumnExpression columnExpression = (ColumnExpression) expression;
                if (columnExpression.useAnyTable() && columnExpression.useAnyColumn()) {
                    this.selectAll = true;
                    return;
                } else if (columnExpression.useAnyColumn()) {
                    String tableName = columnExpression.getTableAlias();
                    tablesAware.getColumns(tableName).forEach(c ->
                            ret.put(c.getName(), new ColumnExpression(c.getName(), alias))
                    );
                    return;
                }
            }
            ret.put(alias, expression);
        });
        return ret;
    }

    public Map<String, Object> getSelectionMap() {
        return new LinkedHashMap<>(selectionMap);
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    @Override
    public String toString() {
        return "SelectTask{" +
                "selectionMap=" + selectionMap +
                ", selectAll=" + selectAll +
                '}';
    }
}
