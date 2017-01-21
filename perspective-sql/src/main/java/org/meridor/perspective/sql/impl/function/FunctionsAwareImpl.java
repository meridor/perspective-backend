package org.meridor.perspective.sql.impl.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FunctionsAwareImpl implements FunctionsAware {

    private final ApplicationContext applicationContext;
    
    private final Map<FunctionName, Function<?>> functions = new HashMap<>();

    @Autowired
    public FunctionsAwareImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        applicationContext.getBeansOfType(Function.class).values().forEach(f -> {
            FunctionName functionName = f.getName();
            if (functions.containsKey(functionName)) {
                throw new IllegalStateException(String.format(
                        "Duplicate function name %s in classes: [%s] and [%s]. This is a bug.",
                        functionName.name(),
                        f.getClass().getCanonicalName(),
                        functions.get(functionName).getClass().getCanonicalName()
                ));
            }
            functions.put(functionName, f);
        });
    }

    @Override
    public Optional<Function<?>> getFunction(String name) {
        Optional<FunctionName> functionNameCandidate = FunctionName.fromString(name);
        return functionNameCandidate.isPresent() ?
                Optional.ofNullable(functions.get(functionNameCandidate.get())) :
                Optional.empty();
    }

    @Override
    public List<Function<?>> getAllFunctions() {
        return functions.keySet().stream()
                .sorted(Comparator.comparing(Enum::name))
                .map(functions::get)
                .collect(Collectors.toList());
    }
}
