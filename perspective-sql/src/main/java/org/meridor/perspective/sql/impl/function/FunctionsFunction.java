package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.sql.DataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.beans.BooleanRelation.LESS_THAN_EQUAL;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class FunctionsFunction implements Function<DataContainer> {

    private static final String FUNCTION_NAME = "function_name";
    private static final String DESCRIPTION = "description";

    private final FunctionsAware functionsAware;

    @Autowired
    public FunctionsFunction(FunctionsAware functionsAware) {
        this.functionsAware = functionsAware;
    }

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(LESS_THAN_EQUAL, 1)
        );
    }

    @Override
    public Class<DataContainer> getReturnType() {
        return DataContainer.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.FUNCTIONS;
    }

    @Override
    public DataContainer apply(List<Object> objects) {
        Optional<String> functionName = objects.size() == 1 && objects.get(0) != null ?
                Optional.of(String.valueOf(objects.get(0))) : Optional.empty();
        List<Function<?>> functions = new ArrayList<>();
        if (functionName.isPresent()) {
            Optional<Function<?>> functionCandidate = functionsAware.getFunction(functionName.get());
            if (!functionCandidate.isPresent()) {
                throw new IllegalArgumentException(String.format("Function %s does not exist", functionName));
            }
            functions.add(functionCandidate.get());
        } else {
            functions.addAll(functionsAware.getAllFunctions());
        }
        DataContainer dataContainer = new DataContainer(Arrays.asList(FUNCTION_NAME, DESCRIPTION));
        functions.forEach(f -> {
            dataContainer.addRow(Arrays.asList(
                    f.getSignature(),
                    f.getDescription()
            ));
        });
        return dataContainer;
    }

    @Override
    public String getSignature() {
        return "FUNCTIONS([NAME])";
    }

    @Override
    public String getDescription() {
        return "Returns a list of available functions. When NAME is specified shows only one function corresponding to this name.";
    }

}
