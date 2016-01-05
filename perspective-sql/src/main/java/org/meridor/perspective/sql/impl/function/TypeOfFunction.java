package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.sql.impl.expression.Null;
import org.meridor.perspective.sql.impl.table.DataType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TypeOfFunction implements Function<Boolean> {
    
    @Override
    public Set<String> validateInput(List<Object> args) {
        if (args.size() != 2) {
            return Collections.singleton("Two arguments are required: value and type to check against");
        }
        Object desiredColumnType = args.get(1);
        Optional<DataType> dataTypeCandidate = getDataType(desiredColumnType);
        if (!dataTypeCandidate.isPresent()) {
            String enumeratedColumnTypes = Arrays.stream(DataType.values()).map(Enum::name).collect(Collectors.joining(", "));
            return Collections.singleton(String.format("Invalid data type \"%s\": should be one of {%s}", desiredColumnType, enumeratedColumnTypes));
        }
        return Collections.emptySet();
    }

    private Optional<DataType> getDataType(Object desiredColumnType) {
        if (desiredColumnType instanceof DataType) {
            return Optional.of((DataType) desiredColumnType);
        }
        return Arrays.stream(DataType.values())
                .filter(ct -> ct.name().equalsIgnoreCase(desiredColumnType.toString()))
                .findFirst();
    }
    
    @Override
    public Class<Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.TYPEOF;
    }

    @Override
    public Boolean apply(List<Object> objects) {
        Object value = objects.get(0);
        Object desiredDataType = objects.get(1);
        DataType dataType = getDataType(desiredDataType).get();
        switch (dataType) {
            case STRING: return value instanceof String;
            case INTEGER: return value != null && Integer.class.isAssignableFrom(value.getClass());
            case FLOAT: return value != null && Double.class.isAssignableFrom(value.getClass());
            case NULL: return value == null || value instanceof Null;
        }
        return false;
    }
}
