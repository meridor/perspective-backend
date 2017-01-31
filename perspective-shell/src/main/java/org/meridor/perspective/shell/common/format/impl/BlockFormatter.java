package org.meridor.perspective.shell.common.format.impl;

import org.meridor.perspective.shell.common.format.DataFormatter;
import org.meridor.perspective.sql.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.underscoreToUpperCamel;

@Component
public class BlockFormatter implements DataFormatter {

    private static final String ENTRY_DELIMITER = "********************\n";

    @Override
    public String format(Data data) {
        List<String> entries = new ArrayList<>();
        List<String> columnNames = data.getColumnNames();
        data.getRows().forEach(r -> {
            StringBuilder sb = new StringBuilder(ENTRY_DELIMITER);
            columnNames.forEach(cn -> {
                int columnPosition = columnNames.indexOf(cn);
                String value = String.valueOf(r.getValues().get(columnPosition));
                sb.append(String.format(
                        "%s: %s\n",
                        underscoreToUpperCamel(cn),
                        value
                ));
            });
            entries.add(sb.toString());
        });
        return entries.stream().collect(Collectors.joining());
    }

    @Override
    public String getActivationDelimiter() {
        return "\\G";
    }

}
