package org.meridor.perspective.shell.noninteractive.commands;

import org.meridor.perspective.shell.common.format.DataFormatter;
import org.meridor.perspective.shell.common.format.DataFormatterAware;
import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

import java.util.Optional;
import java.util.stream.Collectors;

public class QueryCommand extends CommandWithDependencyInjection {
    
    private final String sql;

    public QueryCommand(String sql) {
        super();
        this.sql = sql;
    }

    @Override
    public void run() {
        executeQuery(sql);
    }

    private void executeQuery(String sql) {
        QueryRepository queryRepository = getApplicationContext().getBean(QueryRepository.class);
        DataFormatterAware dataFormatterAware = getApplicationContext().getBean(DataFormatterAware.class);
        Query query = new Query();
        query.setSql(dataFormatterAware.removeDelimiter(sql));
        QueryResult result = queryRepository.query(query);
        Optional<DataFormatter> dataFormatterCandidate = dataFormatterAware.getDataFormatter(sql);
        switch (result.getStatus()) {
            case SUCCESS: {
                if (dataFormatterCandidate.isPresent()) {
                    DataFormatter dataFormatter = dataFormatterCandidate.get();
                    System.out.print(dataFormatter.format(result.getData()));
                } else {
                    printData(result);
                }
                break;
            }
            case SYNTAX_ERROR:
            case MISSING_PARAMETERS:
            case EVALUATION_ERROR: {
                System.err.println(String.format("Error: %s", result.getMessage()));
                break;
            }
        }
    }
    
    private void printData(QueryResult result) {
        final String DELIMITER = "\t";
        
        Data data = result.getData();
        String columnsRow = data.getColumnNames()
                .stream()
                .collect(Collectors.joining(DELIMITER));
        System.out.println(columnsRow);
        
        data.getRows().stream()
                .map(r -> r.getValues()
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(DELIMITER))
                )
                .forEach(System.out::println);
        
    }
    
}
