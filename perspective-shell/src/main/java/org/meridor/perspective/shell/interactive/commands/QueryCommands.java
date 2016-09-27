package org.meridor.perspective.shell.interactive.commands;

import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.validator.Setting.SHOW_QUERY_STATS;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class QueryCommands extends BaseCommands {

    @Autowired
    private QueryRepository queryRepository;
    
    @Autowired
    private SettingsAware settingsAware;
    
    @CliCommand(value = "select", help = "Execute SELECT query")
    public void select(
            @CliOption(key = "", mandatory = true, help = "Query body") String sql
    ) {
        executeQuery("select " + sql);
    }
    
    @CliCommand(value = "explain", help = "Execute EXPLAIN query")
    public void explain(
            @CliOption(key = "", mandatory = true, help = "Query body") String sql
    ) {
        executeQuery("explain " + sql);
    }
    
    private void executeQuery(String sql) {
        Query query = new Query();
        query.setSql(sql);
        long queryStart = System.currentTimeMillis();
        QueryResult result = queryRepository.query(query);
        switch (result.getStatus()) {
            case SUCCESS: {
                if (showQueryStats()) {
                    long queryFinish = System.currentTimeMillis();
                    double seconds = (double) (queryFinish - queryStart) / 1000;
                    ok(String.format("Fetched %d rows in %.2f seconds.", result.getCount(), seconds));
                }
                pageData(result);
                break;
            }
            case SYNTAX_ERROR:
            case MISSING_PARAMETERS:
            case EVALUATION_ERROR: {
                error(String.format(
                        "SQL error: status = %s, message = %s",
                        result.getStatus().value(),
                        !isEmpty(result.getMessage()) ? 
                                result.getMessage() : "<empty>"
                ));
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
    
    private boolean showQueryStats() {
        return 
                settingsAware.hasSetting(SHOW_QUERY_STATS) &&
                settingsAware.getSettingAs(SHOW_QUERY_STATS, Boolean.class);
    }
}
