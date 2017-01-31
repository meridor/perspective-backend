package org.meridor.perspective.shell.common.format.impl;

import org.meridor.perspective.shell.common.format.DataFormatter;
import org.meridor.perspective.sql.Data;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ListFormatter implements DataFormatter {

    private static final String NEW_LINE = "\n";

    @Override
    public String format(Data data) {
        return data.getRows().stream()
                .flatMap(r -> r.getValues().stream())
                .map(String::valueOf)
                .collect(Collectors.joining(NEW_LINE));
    }

    @Override
    public String getActivationDelimiter() {
        return "\\L";
    }

}
