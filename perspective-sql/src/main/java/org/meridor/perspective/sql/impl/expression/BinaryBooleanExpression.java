package org.meridor.perspective.sql.impl.expression;

import java.util.*;

public class BinaryBooleanExpression implements BooleanExpression {
    
    private final Object left;
    private final BinaryBooleanOperator binaryBooleanOperator;
    private final Object right;

    public BinaryBooleanExpression(Object left, BinaryBooleanOperator binaryBooleanOperator, Object right) {
        this.left = left;
        this.binaryBooleanOperator = binaryBooleanOperator;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public BinaryBooleanOperator getBinaryBooleanOperator() {
        return binaryBooleanOperator;
    }

    public Object getRight() {
        return right;
    }
    
    public static BinaryBooleanExpression alwaysTrue() {
        return new BinaryBooleanExpression(true, BinaryBooleanOperator.OR, true);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, binaryBooleanOperator.getText(), right);
    }

    @Override
    public Set<String> getTableAliases() {
        Set<String> ret = new LinkedHashSet<>();
        Optional<BooleanExpression> leftAsBooleanExpression = asBooleanExpression(left);
        Optional<BooleanExpression> rightAsBooleanExpression = asBooleanExpression(right);
        if (leftAsBooleanExpression.isPresent()) {
            ret.addAll(leftAsBooleanExpression.get().getTableAliases());
        }
        if (rightAsBooleanExpression.isPresent()) {
            ret.addAll(rightAsBooleanExpression.get().getTableAliases());
        }
        return ret;
    }

    private Optional<BooleanExpression> asBooleanExpression(Object value) {
        return value instanceof BooleanExpression ?
                Optional.of((BooleanExpression) value) : Optional.empty();
    }
    
    @Override
    public Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        Map<String, Set<Object>> ret = new HashMap<>();
        Optional<BooleanExpression> leftAsBooleanExpression = asBooleanExpression(left);
        Optional<BooleanExpression> rightAsBooleanExpression = asBooleanExpression(right);
        if (leftAsBooleanExpression.isPresent()) {
            ret.putAll(leftAsBooleanExpression.get().getFixedValueConditions(tableAlias));
        }
        if (rightAsBooleanExpression.isPresent()) {
            Map<String, Set<Object>> rightFixedValueConditions = rightAsBooleanExpression.get().getFixedValueConditions(tableAlias);
            rightFixedValueConditions.keySet().forEach(columnName -> {
                Set<Object> newValues = rightFixedValueConditions.get(columnName);
                if (newValues != null) {
                    ret.merge(columnName, newValues, (l, r) -> new HashSet<Object>(){
                        {
                            addAll(l);
                            addAll(r);
                        }
                    });
                }
            });
        }
        return ret;
    }

    @Override
    public Map<String, Set<String>> getColumnRelations() {
        Map<String, Set<String>> ret = new HashMap<>();
        Optional<BooleanExpression> leftAsBooleanExpression = asBooleanExpression(left);
        Optional<BooleanExpression> rightAsBooleanExpression = asBooleanExpression(right);
        if (leftAsBooleanExpression.isPresent()) {
            ret.putAll(leftAsBooleanExpression.get().getColumnRelations());
        }
        if (rightAsBooleanExpression.isPresent()) {
            ret.putAll(rightAsBooleanExpression.get().getColumnRelations());
        }
        return ret;
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        Optional<BooleanExpression> leftAsBooleanExpression = asBooleanExpression(left);
        Optional<BooleanExpression> rightAsBooleanExpression = asBooleanExpression(right);
        List<BooleanExpression> restOfExpressions = new ArrayList<>();
        if (leftAsBooleanExpression.isPresent() && leftAsBooleanExpression.get().getRestOfExpression().isPresent()) {
            restOfExpressions.add(leftAsBooleanExpression.get().getRestOfExpression().get());
        }
        if (rightAsBooleanExpression.isPresent() && rightAsBooleanExpression.get().getRestOfExpression().isPresent()) {
            restOfExpressions.add(rightAsBooleanExpression.get().getRestOfExpression().get());
        }
        if (restOfExpressions.isEmpty()) {
            return Optional.empty();
        }
        return (restOfExpressions.size() == 2) ?
                Optional.of(new BinaryBooleanExpression(restOfExpressions.get(0), BinaryBooleanOperator.AND, restOfExpressions.get(1))) :
                Optional.of(restOfExpressions.get(0));
    }
}
