package org.meridor.perspective.shell.interactive.commands;

import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueryCommands extends BaseCommands {

    @Autowired
    private QueryRepository queryRepository;
    
    @CliCommand(value = "select", help = "Execute SELECT query")
    public void select(
            @CliOption(key = "", mandatory = true, help = "Query body") String sql
    ) {
        Query query = new Query();
        query.setSql("select " + sql);
        QueryResult result = queryRepository.query(query);
        switch (result.getStatus()) {
            case SUCCESS: {
                pageData(result);
                break;
            }
            case SYNTAX_ERROR: 
            case MISSING_PARAMETERS:
            case EVALUATION_ERROR: {
                error(String.format("Error: %s", result.getMessage()));
                break;
            }
        }
    }
    
    private void pageData(QueryResult result) {
        Data data = result.getData();
        String[] columns = data.getColumnNames()
                .toArray(new String[data.getColumnNames().size()]);
        List<String[]> rows = data.getRows().stream()
                .map(r -> r.getValues()
                        .stream()
                        .map(String::valueOf)
                        .toArray(String[]::new)
                )
                .collect(Collectors.toList());
        tableOrNothing(columns, rows);
    }
    
}
