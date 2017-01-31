package org.meridor.perspective.shell.common.format.impl;

import org.meridor.perspective.shell.common.format.DataFormatter;
import org.meridor.perspective.shell.common.format.DataFormatterAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DataFormatterAwareImpl implements DataFormatterAware {

    private final Map<String, DataFormatter> dataFormatters = new HashMap<>();

    private final ApplicationContext applicationContext;

    @Autowired
    public DataFormatterAwareImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        applicationContext.getBeansOfType(DataFormatter.class).values().forEach(
                df -> dataFormatters.put(df.getActivationDelimiter(), df)
        );
    }

    @Override
    public Optional<DataFormatter> getDataFormatter(String sql) {
        if (sql == null) {
            return Optional.empty();
        }
        Optional<String> matchingDelimiter = getMatchingDelimiter(sql);

        return matchingDelimiter.isPresent() ?
                Optional.ofNullable(dataFormatters.get(matchingDelimiter.get()))
                : Optional.empty();
    }

    private Optional<String> getMatchingDelimiter(String sql) {
        return dataFormatters.keySet().stream()
                .filter(sql::endsWith)
                .findFirst();
    }

    @Override
    public String removeDelimiter(String sql) {
        if (sql == null) {
            return "";
        }
        Optional<String> matchingDelimiter = getMatchingDelimiter(sql);
        return matchingDelimiter.isPresent() ?
                sql.substring(0, sql.length() - matchingDelimiter.get().length()) : sql;
    }
}
