package org.meridor.perspective.sql.impl.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class FunctionsAwareImpl implements FunctionsAware {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<FunctionName, Function<?>> functions = new HashMap<>();
    
    @PostConstruct
    public void init() {
        applicationContext.getBeansOfType(Function.class).values().forEach(f -> {
            FunctionName functionName = f.getName();
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
}
