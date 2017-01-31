package org.meridor.perspective.shell.common.format.impl;

import org.meridor.perspective.shell.common.format.DataFormatter;
import org.meridor.perspective.sql.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EnumerationFormatter implements DataFormatter {

    private static final String NEW_LINE = "\n";
    private static final String COMMA = ",";

    @Override
    public String format(Data data) {
        Map<String, List<String>> rows = new HashMap<>();
        List<String> columnNames = data.getColumnNames();
        columnNames.forEach(cn -> {
            int columnPosition = columnNames.indexOf(cn);
            rows.putIfAbsent(cn, new ArrayList<>());
            data.getRows().forEach(r -> {
                String value = String.valueOf(r.getValues().get(columnPosition));
                rows.get(cn).add(value);
            });
        });
        return rows.values().stream()
                .map(
                        r -> r.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(COMMA))
                )
                .collect(Collectors.joining(NEW_LINE));
    }

    @Override
    public String getActivationDelimiter() {
        return "\\E";
    }

}
